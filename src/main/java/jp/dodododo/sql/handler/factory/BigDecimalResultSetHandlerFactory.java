package jp.dodododo.sql.handler.factory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.impl.BigDecimalResultSetHandler;
import jp.dodododo.sql.types.JavaType;

public class BigDecimalResultSetHandlerFactory extends ResultSetHandlerFactory<BigDecimal> {

	public static final BigDecimalResultSetHandlerFactory INSTANCE = new BigDecimalResultSetHandlerFactory();

	@Override
	public ResultSetHandler<BigDecimal> create(Class<BigDecimal> type, JavaType<?> javaType, IterationCallback<BigDecimal> callback, Dialect dialect,
			Map<String, Object> arg, Connection connection) {

		return new BigDecimalResultSetHandler(callback, dialect);
	}

}
