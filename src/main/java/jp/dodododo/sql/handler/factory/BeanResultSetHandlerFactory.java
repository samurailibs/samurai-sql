package jp.dodododo.sql.handler.factory;

import java.sql.Connection;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.BeanResultSetHandler;
import jp.dodododo.sql.types.JavaType;

public class BeanResultSetHandlerFactory<BEAN> extends ResultSetHandlerFactory<BEAN> {

	@SuppressWarnings("rawtypes")
	public static final BeanResultSetHandlerFactory<?> INSTANCE = new BeanResultSetHandlerFactory();

	@Override
	public ResultSetHandler<BEAN> create(Class<BEAN> type, JavaType<?> javaType, IterationCallback<BEAN> callback, Dialect dialect,
			Map<String, Object> arg, Connection connection) {

		return new BeanResultSetHandler<BEAN>(callback, arg, type, connection);
	}

}
