package jp.dodododo.sql.env;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.executor.common.PreparedStatementFactory;
import jp.dodododo.sql.executor.crud.CrudSqlBuilder;
import jp.dodododo.sql.log.SqlLogRegistry;

import java.sql.Connection;

public class SqlEnvironment {
    protected SqlConfig sqlConfig;

    protected SqlLogRegistry sqlLogRegistry;

    protected SqlLogger sqlLogger;
    protected PreparedStatementFactory preparedStatementFactory;
    protected SqlResourceLoader sqlResourceLoader;
    protected SqlNodeCache sqlNodeCache;
    protected CrudSqlBuilder crudSqlBuilder;

    public SqlEnvironment() {
        sqlConfig = new SqlConfig();
        sqlResourceLoader = new SqlResourceLoader(sqlConfig);
        sqlNodeCache = new SqlNodeCache();
        sqlLogRegistry = SqlLogRegistry.getInstance();
        sqlLogRegistry.setConfig(sqlConfig);
        sqlLogger = new SqlLogger(sqlLogRegistry, sqlConfig);
    }

    public void init(Connection connection) {
    }

    public CrudSqlBuilder crudSqlBuilder() {
        return crudSqlBuilder;
    }

    public PreparedStatementFactory preparedStatementFactory() {
        return preparedStatementFactory;
    }

    public SqlConfig sqlConfig() {
        return sqlConfig;
    }

    public SqlLogger sqlLogger() {
        return sqlLogger;
    }

    public SqlLogRegistry sqlLogRegistry() {
        return sqlLogRegistry;
    }

    public SqlNodeCache sqlNodeCache() {
        return sqlNodeCache;
    }

    public SqlResourceLoader sqlResourceLoader() {
        return sqlResourceLoader;
    }
}
