package jp.dodododo.sql.handler.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.util.CaseInsensitiveMap;
import jp.dodododo.sql.util.ResultSetUtil;

public class MapResultSetHandler extends AbstractResultSetHandler<Map<String, Object>> {

	protected Map<String, Object> arg;

	protected MapResultSetHandler() {
	}

	public MapResultSetHandler(IterationCallback<Map<String, Object>> callback) {
		this(callback, null);
	}

	public MapResultSetHandler(IterationCallback<Map<String, Object>> callback, Map<String, Object> arg) {
		super(callback);
		this.arg = arg;
	}

	@Override
	protected Map<String, Object> createRow(final ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {
		Map<String, Object> row = newInstance();

		resultSetColumnList.forEach(column -> {
			String name = column.getName();
			Object value = ResultSetUtil.getObject(rs, name);
			row.put(name, value);
		});
		return row;
	}

	protected Map<String, Object> newInstance() {
		CaseInsensitiveMap<Object> ret = new CaseInsensitiveMap<>();
		ret.putAll(arg);
		return ret;
	}
}
