package jp.dodododo.sql.value;

import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Compress;
import jp.dodododo.sql.compress.TmpDataMode;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.util.OgnlUtil;

public class ValueProxy {
	protected static enum Objects {
		UNDEFINE
	}

	protected PropertyDesc pd;
	protected Object target;
	protected Object value = Objects.UNDEFINE;
	protected OperatorAndValues operatorAndValues;

	public ValueProxy(Object target) {
		this.value = target;
	}

	public ValueProxy(OperatorAndValues operatorAndValues) {
		this.operatorAndValues = operatorAndValues;
		this.value = operatorAndValues.getValue();
	}

	public ValueProxy(PropertyDesc pd, Object target) {
		this.pd = pd;
		this.target = target;
	}

	public Object getValue() {
		if (this.value != Objects.UNDEFINE) {
			return this.value;
		}
		Object value = pd.getValue(target);
		if (value == null) {
			this.value = null;
			return this.value;
		}
		Compress compress = pd.getAnnotation(Compress.class);
		if (compress != null) {
			int bufferSize = compress.bufferSize();
			TmpDataMode tmpDataMode = compress.tmpDataMode();
			value = compress.compressType().compressedInputStream(value, bufferSize, tmpDataMode);
		}
		Bean bean = pd.getAnnotation(Bean.class);
		if (bean != null && bean.property().isEmpty() == false) {
			Map<String, Object> root = new HashMap<>();
			root.put("data", value);
			this.value = OgnlUtil.getValue("data." + bean.property(), root);
			return this.value;
		} else {
			this.value = value;
			return this.value;
		}
	}

	public Object getTarget() {
		return target;
	}

	public PropertyDesc getPropertyDesc() {
		return pd;
	}


	public OperatorAndValues getOperatorAndValues() {
		return operatorAndValues;
	}

	@Override
	public String toString() {
		if (pd != null) {
			return "" + getValue() + " : " + target.getClass().getName() + "#" + pd.getPropertyName();
		}
		if (target != null) {
			return "" + getValue() + " : " + target.getClass().getName();
		}
		return "" + getValue();
	}
}
