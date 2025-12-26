package jp.dodododo.sql.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.SqlConstants;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.util.SqlUtil;
import jp.dodododo.sql.value.ParameterValue;

public class SqlContext implements Serializable {
	private static final long serialVersionUID = 9205994213162637655L;

	private String tableName;
	private List<ParameterValue> pks;
	private List<ParameterValue> columns;
	private List<Object> values;
	private Class<?> queryClass;
	private Dialect dialect;
	private Map<String, Object> parameters = new HashMap<>();

	public SqlContext(String tableName, List<ParameterValue> columns, Class<?> queryClass, Dialect dialect) {
		this(tableName, null, columns, queryClass, dialect);
	}

	public SqlContext(String tableName, List<ParameterValue> pks, List<ParameterValue> columns, Class<?> queryClass,
			Dialect dialect) {
		this.tableName = tableName;
		this.pks = pks == null ? new ArrayList<>() : pks;
		this.columns = columns == null ? new ArrayList<>() : columns;
		this.queryClass = queryClass;
		this.dialect = dialect;
		init();
	}

	private void init() {
		addParameter(SqlConstants.ORDER_BY, Collections.EMPTY_LIST);
		columns.forEach(parameterValue -> {
			String name = parameterValue.getName();
			Object value = parameterValue.getValue();
			parameters.put(name, value);

		});
	}

	public String getTableName() {
		return tableName;
	}

	public List<ParameterValue> getPks() {
		return pks;
	}

	public List<ParameterValue> getColumns() {
		return columns;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void addParameter(Map<String, Object> parameters) {
		this.parameters.putAll(parameters);
	}

	public void addParameter(String name, Object value) {
		this.parameters.put(name, value);
	}

	public Class<?> getQueryClass() {
		return queryClass;
	}

	public List<?> getVals() {
		return values;
	}

	public void setValues(List<?> values) {
		this.values = new ArrayList<>();
		if (values == null || values.isEmpty()) {
			return;
		}
		addParameter(SqlUtil.VALUES, values);
		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(values.get(0));
		values.forEach(object -> {
			Object map = objectDesc.toMap(object);
			this.values.add(map);
		});
	}
}
