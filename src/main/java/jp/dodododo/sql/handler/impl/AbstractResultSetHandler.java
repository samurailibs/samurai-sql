package jp.dodododo.sql.handler.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.types.SQLType;
import jp.dodododo.sql.util.CacheUtil;
import jp.dodododo.sql.util.DbArrayList;
import jp.dodododo.sql.util.TypesUtil;
import jp.dodododo.sql.wrapper.ResultSetWrapper;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class AbstractResultSetHandler<T> implements ResultSetHandler<T> {
	private IterationCallback<T> callback;
	private static final Map<String, List<ResultSetColumn>> RESULT_SET_COLUMN_CACHE = CacheUtil
			.cacheMap(new WeakHashMap<>());

	protected AbstractResultSetHandler() {
	}

	public AbstractResultSetHandler(IterationCallback<T> callback) {
		this.callback = callback;
	}

    @Override
	public void handle(ResultSet rs) throws SQLException {
		List<ResultSetColumn> resultSetColumnList = getResultSetColumnList(rs);
		List<T> resultList = callback.getResult();
		DbArrayList<T> dbArrayList = null;
		if (resultList != null && resultList instanceof DbArrayList<?>) {
			dbArrayList = (DbArrayList<T>)resultList;
		}
		if (dbArrayList != null) {
			dbArrayList.setHeader(resultSetColumnList);
		}
		while (rs.next()) {
			T row = createRow(rs, resultSetColumnList);
			if (dbArrayList != null) {
				List<Object> datas = getRowData(rs, resultSetColumnList);
				dbArrayList.addRow(datas);
			}
			if (row != null) {
				callback.iterate(row);
			}
		}
	}

	private List<Object> getRowData(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {
		List<Object> ret = new ArrayList<>(resultSetColumnList.size());
		for (ResultSetColumn resultSetColumn : resultSetColumnList) {
			SQLType sqlType = TypesUtil.getSQLType(resultSetColumn.getDataType());
			Object value = sqlType.toJavaType().getValue(rs, resultSetColumn.getName());
			ret.add(value);
		}
		return ret;
	}

	private List<ResultSetColumn> getResultSetColumnList(ResultSet rs) throws SQLException {
		if (rs instanceof ResultSetWrapper == false) {
			return getResultSetColumnList(rs.getMetaData());
		}
		ResultSetWrapper wrapper = (ResultSetWrapper) rs;
		String sql = wrapper.getSql();
		if (sql == null) {
			return getResultSetColumnList(rs.getMetaData());
		}
		List<ResultSetColumn> resultSetColumnList = RESULT_SET_COLUMN_CACHE.get(sql);
		if (resultSetColumnList == null) {
			resultSetColumnList = getResultSetColumnList(rs.getMetaData());
			RESULT_SET_COLUMN_CACHE.put(sql, resultSetColumnList);
		}
		return resultSetColumnList;
	}

	protected List<ResultSetColumn> getResultSetColumnList(ResultSetMetaData rsmd) throws SQLException {
		int count = rsmd.getColumnCount();
		List<ResultSetColumn> ret = new ArrayList<>(count);

		for (int i = 0; i < count; ++i) {
			String columnName = rsmd.getColumnLabel(i + 1);
			int columnType = rsmd.getColumnType(i + 1);
			int columnDisplaySize = getColumnDisplaySize(rsmd, i + 1, 10);
			ret.add(new ResultSetColumn(columnName, columnType, columnDisplaySize));
		}
		return ret;
	}

	private int getColumnDisplaySize(ResultSetMetaData rsmd, int index, int defaultSize) throws SQLException {
		try {
			int columnDisplaySize = rsmd.getColumnDisplaySize(index);
			return columnDisplaySize;
		} catch (SQLException e) {
			return defaultSize; // for mysql
		}
	}

	protected abstract T createRow(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException;

	protected IterationCallback<T> getIterationCallback() {
		return callback;
	}
}
