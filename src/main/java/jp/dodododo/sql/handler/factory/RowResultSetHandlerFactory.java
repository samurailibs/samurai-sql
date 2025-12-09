package jp.dodododo.sql.handler.factory;

import java.sql.Connection;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.RowResultSetHandler;
import jp.dodododo.sql.row.Row;
import jp.dodododo.sql.types.JavaType;

public class RowResultSetHandlerFactory extends ResultSetHandlerFactory<Row> {

	public static final RowResultSetHandlerFactory INSTANCE = new RowResultSetHandlerFactory();

	@Override
	public ResultSetHandler<Row> create(Class<Row> type, JavaType<?> javaType, IterationCallback<Row> callback, Dialect dialect,
			Map<String, Object> arg, Connection connection) {

		return new RowResultSetHandler(callback);
	}

}
