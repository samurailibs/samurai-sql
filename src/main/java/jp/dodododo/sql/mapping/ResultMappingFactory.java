package jp.dodododo.sql.mapping;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.handler.factory.ResultSetHandlerFactory;
import jp.dodododo.sql.types.JavaType;
import jp.dodododo.sql.util.TypesUtil;

import java.sql.Connection;
import java.util.Map;

public class ResultMappingFactory {
    public <ROW> ResultSetHandler<?> createResultSetHandler(Class<ROW> entityClass, IterationCallback<ROW> callback, Dialect dialect,
                                                            Map<String, Object> arg, Connection connection) {

        ResultSetHandlerFactory<ROW> resultSetHandlerFactory = ResultSetHandlerFactory.getResultSetHandlerFactory(entityClass);
        JavaType<?> javaType = TypesUtil.getJavaType(entityClass);
        return resultSetHandlerFactory.create(entityClass, javaType, callback, dialect, arg, connection);
    }
}
