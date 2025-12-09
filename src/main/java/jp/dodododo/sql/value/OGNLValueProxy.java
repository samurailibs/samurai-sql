package jp.dodododo.sql.value;

import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.util.OgnlUtil;

public class OGNLValueProxy extends ValueProxy {

	protected String expression;

	public OGNLValueProxy(Object data, String expression) {
		super(Objects.UNDEFINE);
		this.target = data;
		this.expression = expression;
	}

	@Override
	public Object getValue() {
		if (this.value != Objects.UNDEFINE) {
			return this.value;
		}
		Map<String, Object> root = new HashMap<>();
		root.put("data", this.target);
		this.value = OgnlUtil.getValue(expression, root);
		return this.value;
	}
}
