package jp.dodododo.sql.handler.factory;

import java.sql.Connection;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.MapResultSetHandler;
import jp.dodododo.sql.types.JavaType;

public class MapResultSetHandlerFactory extends ResultSetHandlerFactory<Map<String, Object>> {

	public static final MapResultSetHandlerFactory INSTANCE = new MapResultSetHandlerFactory();

	@Override
	public ResultSetHandler<Map<String, Object>> create(Class<Map<String, Object>> type, JavaType<?> javaType,
			IterationCallback<Map<String, Object>> callback, Dialect dialect, Map<String, Object> arg, Connection connection) {

		return new MapResultSetHandler(callback, arg);
	}

}
