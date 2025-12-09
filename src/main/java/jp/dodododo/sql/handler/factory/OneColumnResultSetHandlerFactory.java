package jp.dodododo.sql.handler.factory;

import java.sql.Connection;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.OneColumnResultSetHandler;
import jp.dodododo.sql.types.JavaType;

public class OneColumnResultSetHandlerFactory<TYPE> extends ResultSetHandlerFactory<TYPE> {

	@SuppressWarnings("rawtypes")
	public static final OneColumnResultSetHandlerFactory<?> INSTANCE = new OneColumnResultSetHandlerFactory();

	@Override
	public ResultSetHandler<TYPE> create(Class<TYPE> type, JavaType<?> javaType, IterationCallback<TYPE> callback, Dialect dialect,
			Map<String, Object> arg, Connection connection) {

		return new OneColumnResultSetHandler<TYPE>(callback, type, javaType);
	}

}
