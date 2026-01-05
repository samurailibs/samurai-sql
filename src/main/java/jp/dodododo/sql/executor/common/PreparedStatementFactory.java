package jp.dodododo.sql.executor.common;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.exception.InvalidSQLException;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.env.SqlLogger;
import jp.dodododo.sql.env.SqlNodeCache;
import jp.dodododo.sql.log.ExecuteType;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.sql.SqlContext;
import jp.dodododo.sql.sql.node.Node;
import jp.dodododo.sql.util.ConnectionUtil;
import jp.dodododo.sql.util.PreparedStatementUtil;
import jp.dodododo.sql.util.TypesUtil;
import jp.dodododo.sql.value.ParameterValue;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PreparedStatementFactory {

    protected SqlNodeCache sqlNodeCache;

    protected SqlLogger sqlLogger;

    protected SqlLogRegistry sqlLogRegistry;

    protected SqlConfig config;

    protected int queryTimeout;

    public PreparedStatementFactory(SqlNodeCache sqlNodeCache, SqlLogger sqlLogger, SqlLogRegistry sqlLogRegistry, SqlConfig config, int queryTimeout) {
        this.sqlNodeCache = sqlNodeCache;
        this.sqlLogger = sqlLogger;
        this.sqlLogRegistry= sqlLogRegistry;
        this.config = config;
        this.queryTimeout = queryTimeout;
    }

    public PreparedStatement createPreparedStatement(Connection connection, ExecuteType executeType, Dialect dialect, CommandContext ctx, Node node) {
        node.accept(ctx);
        List<Object> bindVariables = ctx.getBindVariables();
        List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
        String executableSql = ctx.getSql();
        PreparedStatement ps = createPreparedStatement(connection, executableSql, dialect);
        bindArgs(ps, bindVariables, bindVariableTypes, executableSql, executeType, dialect);
        return ps;
    }

    public PreparedStatement createPreparedStatement(Connection connection, String sql, Dialect dialect) {
        PreparedStatement ps;
        try {
            ps = ConnectionUtil.prepareStatement(connection, sql);
        } catch (SQLRuntimeException e) {
            throw new InvalidSQLException(sql, e);
        }
        PreparedStatementUtil.setQueryTimeout(ps, queryTimeout);
        ps = dialect.preparedStatement(ps);
        return ps;
    }

    public PreparedStatement createPreparedStatement(Connection connection, String sql, Map<String, ParameterValue> values,
                                                     ExecuteType executeType, Dialect dialect, SqlContext sqlContext) {
        CommandContext ctx = createCommandContext(sqlContext, dialect, values);
        Node node = sqlNodeCache.getOrParse(sql);
        return createPreparedStatement(connection, executeType, dialect, ctx, node);
    }

    public void bindArgs(PreparedStatement ps, List<Object> bindVariables, List<Integer> bindVariableTypes, String sql, ExecuteType executeType,
                         Dialect dialect) {
        sqlLogger.logSql(sql, bindVariables, bindVariableTypes, dialect, executeType);
        if (bindVariables == null || bindVariables.isEmpty()) {
            return;
        }
        for (int i = 0; i < bindVariables.size(); ++i) {
            Object value = null;
            try {
                value = bindVariables.get(i);
                if (value == null) {
                    ps.setNull(i + 1, bindVariableTypes.get(i));
                } else if (value instanceof InputStream) {
                    InputStream is = (InputStream) value;
                    dialect.setBinaryStream(ps, i + 1, is);
                } else {
                    int bindVariableType = bindVariableTypes.get(i);
                    ps.setObject(i + 1, TypesUtil.getSQLType(bindVariableType).convert(value, config.getFormats()), bindVariableType);
                }
            } catch (SQLException e) {
                throw new SQLRuntimeException("value=" + value, e);
            }
        }
    }

    public CommandContext createCommandContext(SqlContext sqlContext, Dialect dialect, Map<String, ParameterValue> values) {
        CommandContext ctx = new CommandContext(dialect);
        ctx.addArgs(values);
        if (sqlContext != null) {
            ctx.addValues(sqlContext.getVals());
        }
        return ctx;
    }

    public void bindArgs(PreparedStatement ps, List<Object> bindVariables, List<Integer> bindVariableTypes, ExecuteType executeType,
                         Dialect dialect) {
        String sql = this.sqlLogRegistry.getLast().getRawSql();
        bindArgs(ps, bindVariables, bindVariableTypes, sql, executeType, dialect);
    }

}
