package jp.dodododo.sql.value;

import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.sql.Operator;
import jp.dodododo.sql.util.FreeMarkerUtil;

/**
 *
 * @author Satoshi Kimura
 */
public class ParameterValue {

	private final static String DEFAULT_OPERATOR = Operator.eq(null).getOperator();

	private int dataType;

	private String name;

	private ValueProxy value;

	private String operator = DEFAULT_OPERATOR;

	private boolean matchTableName;

	public ParameterValue(String name, int dataType, ValueProxy value, boolean matchTableName) {
		this.name = name;
		this.dataType = dataType;
		this.value = value;
		this.matchTableName = matchTableName;
	}

	public ParameterValue(String name, int dataType, CandidateValue value, boolean matchTableName) {
		this(name, dataType, value.value, matchTableName);
	}

	public ParameterValue(String name, int dataType, Object value, boolean matchTableName) {
		this(name, dataType, toValueProxy(value), matchTableName);
		this.name = name;
		this.dataType = dataType;
		this.matchTableName = matchTableName;
		setupOperator(value);
	}

	protected void setupOperator(Object value) {
		if (value instanceof OperatorAndValues) {
			this.operator = ((OperatorAndValues) value).getOperator();
		}
	}

	protected static ValueProxy toValueProxy(Object value) {
		if (value instanceof ValueProxy) {
			return (ValueProxy) value;
		} else if (value instanceof Enum) {
			return new EnumValueProxy((Enum<?>) value);
		} else if (value instanceof OperatorAndValues) {
			return new ValueProxy((OperatorAndValues) value);
		} else {
			return new ValueProxy(Operator.eq(value));
		}
	}

	public int getDataType() {
		return dataType;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value.getValue();
	}

	public String getOperator() {
		Map<String, Object> context = new HashMap<>();
		context.put("name", name);
		return FreeMarkerUtil.process(operator, context).trim();
	}

	public boolean isMatchTableName() {
		return matchTableName;
	}

	@Override
	public String toString() {
		return "" + value.getValue();
	}
}
