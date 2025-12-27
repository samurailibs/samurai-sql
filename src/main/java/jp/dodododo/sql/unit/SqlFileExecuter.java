package jp.dodododo.sql.unit;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.MapResultSetHandler;
import jp.dodododo.sql.impl.EmptyIterationCallback;
import jp.dodododo.sql.impl.SamuraiSqlClientImpl;
import jp.dodododo.sql.util.SqlUtil;
import jp.dodododo.sql.value.ParameterValue;

public class SqlFileExecuter {
	private Connection connection;

	public SqlFileExecuter(Connection connection) {
		this.connection = connection;
	}

	public List<Map<String, Object>> executeSelect(String sqlFilePath) {
		SamuraiSqlClientImpl client = new SamuraiSqlClientImpl(connection);
		IterationCallback<Map<String, Object>> callback = EmptyIterationCallback.getInstance();
		ResultSetHandler<Map<String, Object>> handler = new MapResultSetHandler(callback);
		Map<String, Object> arg = SqlUtil.args();
		return client.select(sqlFilePath, arg, callback, handler, false);
	}

	public void executeUpdate(String sqlFilePath) {
		SamuraiSqlClientImpl client = new SamuraiSqlClientImpl(connection);
		Map<String, ParameterValue> values = new HashMap<>();
		client.executeUpdate(values, sqlFilePath, null, false);
	}

	public void executeInsert(String sqlFilePath) {
		executeUpdate(sqlFilePath);
	}

	public void executeDelete(String sqlFilePath) {
		executeUpdate(sqlFilePath);
	}
}
