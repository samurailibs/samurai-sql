package jp.dodododo.sql.value;

import java.io.Serializable;

public class OperatorAndValues implements Serializable {
	private static final long serialVersionUID = 1L;

	protected String operator;

	protected Object[] values;

	protected boolean isSingleValue;

	public OperatorAndValues(String operator, Object[] values, boolean isSingleValue) {
		this.operator = operator;
		this.values = values;
		this.isSingleValue = isSingleValue;
	}

	public OperatorAndValues(String operator, Object[] values) {
		this(operator, values, true);
	}

	public OperatorAndValues(String operator, Object value) {
		this(operator, new Object[] { value });
	}

	public String getOperator() {
		return operator;
	}

	public Object[] getValues() {
		return values;
	}

	public Object getValue() {
		if (isSingleValue == true) {
			return values[0];
		} else {
			return values;
		}
	}

	public String getValuesStr() {
		StringBuilder builder = new StringBuilder();
		for (Object value : values) {
			builder.append(value).append(", ");
		}
		builder.setLength(builder.length() - 2);
		return builder.toString();
	}

	@Override
	public String toString() {
		return operator + " : " + getValuesStr();
	}
}
