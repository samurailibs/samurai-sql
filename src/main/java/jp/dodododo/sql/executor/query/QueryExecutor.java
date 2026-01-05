package jp.dodododo.sql.executor.query;

import jp.dodododo.sql.CRUD;
import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.SqlConstants;
import jp.dodododo.sql.annotation.*;
import jp.dodododo.sql.callback.DBListIterationCallback;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.env.SqlEnvironment;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.executor.common.PreparedStatementFactory;
import jp.dodododo.sql.executor.crud.CrudSqlBuilder;
import jp.dodododo.sql.function.ConsumerWrapper;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.lock.Locking;
import jp.dodododo.sql.log.ExecuteType;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.mapping.EntityIntrospector;
import jp.dodododo.sql.mapping.ResultMappingFactory;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.metadata.ColumnMetaData;
import jp.dodododo.sql.metadata.MetaDataService;
import jp.dodododo.sql.metadata.TableMetaData;
import jp.dodododo.sql.metadata.TableNameResolver;
import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.object.PropertyDescCacheScope;
import jp.dodododo.sql.paging.LimitOffset;
import jp.dodododo.sql.paging.PagingResultSet;
import jp.dodododo.sql.provider.ConnectionProvider;
import jp.dodododo.sql.provider.DialectProvider;
import jp.dodododo.sql.script.Each;
import jp.dodododo.sql.sql.Sql;
import jp.dodododo.sql.sql.SqlContext;
import jp.dodododo.sql.util.*;
import jp.dodododo.sql.value.CandidateValue;
import jp.dodododo.sql.value.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

import static jp.dodododo.sql.lock.OptimisticLocking.OPTIMISTIC_LOCKING;
import static jp.dodododo.sql.sql.GenericSql.SIMPLE_COUNT_WHERE;
import static jp.dodododo.sql.sql.GenericSql.SIMPLE_WHERE;
import static jp.dodododo.sql.util.SqlUtil.*;

public class QueryExecutor {
    protected static final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    protected ConnectionProvider connectionProvider;
    protected DialectProvider dialectProvider;
    protected SqlEnvironment sqlEnvironment;
    protected TableNameResolver tableNameResolver;
    protected EntityIntrospector entityIntrospector;
    protected PreparedStatementFactory statementFactory;
    protected MetaDataService metaDataService;
    protected ResultMappingFactory resultMappingFactory = new ResultMappingFactory();
    protected CrudSqlBuilder sqlBuilder = new CrudSqlBuilder();

    public QueryExecutor(ConnectionProvider connectionProvider, MetaDataService metaDataService,
                         DialectProvider dialectProvider, SqlEnvironment sqlEnvironment, TableNameResolver tableNameResolver, EntityIntrospector entityIntrospector,
                         PreparedStatementFactory statementFactory) {
        this.connectionProvider = connectionProvider;
        this.metaDataService = metaDataService;
        this.dialectProvider = dialectProvider;
        this.sqlEnvironment = sqlEnvironment;
        this.tableNameResolver = tableNameResolver;
        this.entityIntrospector = entityIntrospector;
        this.statementFactory = statementFactory;
    }

    protected Connection getConnection() {
        return connectionProvider.connection();
    }

    public <ROW> Optional<ROW> selectOne(Class<ROW> returnType, Object... args) {
        List<ROW> list = select(returnType, args);
        return getOne(list, args);
    }

    public <ROW> List<ROW> select(Class<ROW> returnType, Object... args) {
        Map<String, Object> query = query(args);
        if (!query.containsKey(SqlConstants.TABLE_NAME)) {
            query.put(SqlConstants.TABLE_NAME, SqlUtil.getTableName(returnType));
        }
        return select(SIMPLE_WHERE, query, returnType);
    }

    protected <ROW> List<ROW> select(String sql, Map<String, Object> arg, IterationCallback<ROW> callback, ResultSetHandler<?> handler) {
        return select(sql, arg, callback, handler, true);
    }

    @Internal
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, IterationCallback<ROW> callback, ResultSetHandler<?> handler, boolean isDynamic) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Dialect dialect = dialectProvider.dialect();
        try {
            connection = getConnection();
            if (sql.contains(" |\n|\r|\t") == false) {
                sql = sqlEnvironment.sqlResourceLoader().getSql(sql, dialect);
            }
            LimitOffset limitOffset = null;
            if (arg != null && arg.containsKey(LimitOffset.KEYWORD)) {
                limitOffset = (LimitOffset) arg.get(LimitOffset.KEYWORD);
                try {
                    sql = dialect.limitOffsetSql(sql, limitOffset);
                    limitOffset = null;
                } catch (UnsupportedOperationException ignore) {
                }
            }
            Map<String, ParameterValue> values = entityIntrospector.createParameterValues(arg);
            if (isDynamic) {
                ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.QUERY, dialect, null);
            } else {
                sqlEnvironment.sqlLogger().logSql(sql, null, null, dialect, ExecuteType.QUERY);
                ps = statementFactory.createPreparedStatement(connection, sql, dialect);
            }
            rs = PreparedStatementUtil.executeQuery(ps, sqlEnvironment.sqlLogRegistry());
            if (limitOffset != null) {
                rs = new PagingResultSet(rs, limitOffset, sql);
            }
            rs = dialect.resultSet(rs);
            handler.handle(rs);
            return callback.getResult();
        } catch (SQLException e) {
            throw new SQLRuntimeException(sqlEnvironment.sqlLogRegistry().getLast().getCompleteSql(), e);
        } finally {
            try {
                ResultSetUtil.close(rs);
            } finally {
                try {
                    StatementUtil.close(ps);
                } finally {
                    ConnectionUtil.close(connection);
                }
            }
        }
    }


    public <ROW> Optional<ROW> selectOne(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
        List<ROW> rows = select(sql, arg, entityClass);
        return getOne(rows, arg);
    }


    public <ROW> Optional<ROW> selectOne(String sql, Class<ROW> entityClass) {
        return selectOne(sql, null, entityClass);
    }

    protected ParameterValue createParameterValue(String columnName, int dataType, CandidateValue value) {
        return new ParameterValue(columnName, dataType, value.value, value.matchTableName);
    }

    public Optional<Map<String, Object>> selectOneMap(String sql, Map<String, Object> arg) {
        List<Map<String, Object>> rows = selectMap(sql, arg);
        return getOne(rows, arg);
    }

    public Optional<Map<String, Object>> selectOneMap(String sql) {
        return selectOneMap(sql, null);
    }

    public Optional<BigDecimal> selectOneNumber(String sql, Map<String, Object> arg) {
        Dialect dialect = dialectProvider.dialect();
        IterationCallback<BigDecimal> callback = new DBListIterationCallback<>();
        ResultSetHandler<?> handler = resultMappingFactory.createResultSetHandler(BigDecimal.class, callback, dialect, arg, getConnection());

        List<BigDecimal> rows = select(sql, arg, callback, handler);
        return getOne(rows, arg);
    }

    protected <T> Optional<T> getOne(List<T> rows, Object arg) {
        if (rows.isEmpty()) {
            return Optional.empty();
        } else if (rows.size() == 1) {
            return Optional.of(rows.get(0));
        } else {
            throw new IllegalStateException(Message.getMessage("00003", ToStringer.toString(arg)));
        }
    }


    public Optional<BigDecimal> selectOneNumber(String sql) {
        return selectOneNumber(sql, null);
    }


    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
        return select(sql, arg, entityClass, new DBListIterationCallback<ROW>());
    }


    public <ROW> List<ROW> select(Sql sql, Map<String, Object> arg, Class<ROW> entityClass) throws SQLRuntimeException {
        IterationCallback<ROW> callback = new DBListIterationCallback<ROW>();
        SqlContext context = createSqlContext(arg);
        String sqlString = sql.getSql(context);
        return select(sqlString, context.getParameters(), entityClass, callback);
    }


    @SuppressWarnings("unchecked")
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, Each callback) {
        return select(sql, arg, entityClass, (IterationCallback<ROW>) callback);
    }


    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, IterationCallback<ROW> callback) {
        Dialect dialect = dialectProvider.dialect();
        ResultSetHandler<?> handler = resultMappingFactory.createResultSetHandler(entityClass, callback, dialect, arg, getConnection());
        return select(sql, arg, callback, handler);
    }


    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass) {
        return select(sql, null, entityClass);
    }


    @SuppressWarnings("unchecked")
    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, Each callback) {
        return select(sql, entityClass, (IterationCallback<ROW>) callback);
    }


    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, IterationCallback<ROW> callback) {
        return select(sql, null, entityClass, callback);
    }


    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg) {
        return selectMap(sql, arg, new DBListIterationCallback<>());
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, IterationCallback<Map<String, Object>> callback) {
        Dialect dialect = dialectProvider.dialect();
        IterationCallback<Map> callback2 = (IterationCallback<Map>) ((IterationCallback<?>) callback);
        ResultSetHandler<?> handler = resultMappingFactory.createResultSetHandler(Map.class, callback2, dialect, arg, getConnection());
        List<Map<String, Object>> ret = select(sql, arg, callback, handler);
        return ret;
    }


    @SuppressWarnings({"unchecked"})
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Each callback) {
        return selectMap(sql, arg, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
    }


    public List<Map<String, Object>> selectMap(String sql) {
        return selectMap(sql, new DBListIterationCallback<>());
    }


    public List<Map<String, Object>> selectMap(String sql, IterationCallback<Map<String, Object>> callback) {
        return selectMap(sql, null, callback);
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectMap(String sql, Each callback) {
        return selectMap(sql, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
    }


    public SqlLogRegistry getSqlLogRegistry() {
        return sqlEnvironment.sqlLogRegistry();
    }


    public <ROW> List<ROW> select(Sql sql, Class<ROW> entityClass) {
        return select(sql, args(TABLE_NAME, getTableName(entityClass)), entityClass);
    }


    public <ROW> List<ROW> select(Sql sql, ROW query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return select(sqlString, context.getParameters(), getClass(query));
    }

    protected <QUERY_OR_ENTITY> SqlContext createSqlContext(QUERY_OR_ENTITY queryOrEntity) {
        return createSqlContext(new Object[]{queryOrEntity}, null, true);
    }

    @SuppressWarnings("unchecked")
    protected <QUERY_OR_ENTITY> SqlContext createSqlContext(Object[] queryOrEntity, List<?> values, boolean isSelect) {
        String tableName = tableNameResolver.getTableName(queryOrEntity[0], getConnection());
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        for (int i = 0; i < queryOrEntity.length; i++) {
            Object obj = queryOrEntity[i];
            if (obj instanceof CaseInsensitiveMap) {
                queryOrEntity[i] = obj;
            } else if (obj instanceof Map) {
                CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();
                map.putAll((Map<String, Object>) obj);
                queryOrEntity[i] = map;
            } else {
                queryOrEntity[i] = entityIntrospector.toMap(obj, tableName, tableMetaData);
            }
        }
        List<ParameterValue> pks = null;
        List<ParameterValue> columns = entityIntrospector.getColumns(tableMetaData, queryOrEntity);
        if (isSelect == false) {
            pks = getPks(tableName, columns);
        }
        Dialect dialect = dialectProvider.dialect();
        SqlContext context = new SqlContext(tableName, pks, columns, Map.class, dialect);
        context.setValues(values);
        for (Object obj : queryOrEntity) {
            context.addParameter((Map<String, Object>) obj);
        }
        return context;
    }


    protected List<ParameterValue> getPks(String tableName, List<ParameterValue> columns) {
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        List<String> pkColumnNames = tableMetaData.getPkColumnNames();
        List<ParameterValue> pks = new ArrayList<>(pkColumnNames.size());
        for (String pkColumnName : pkColumnNames) {
            for (ParameterValue column : columns) {
                if (StringUtil.equalsIgnoreCase(pkColumnName, column.getName())) {
                    pks.add(column);
                }
            }
        }
        return pks;
    }


    @SuppressWarnings("unchecked")
    public <ROW> List<ROW> select(Sql sql, ROW query, Each callback) {
        return select(sql, query, (IterationCallback<ROW>) callback);
    }


    public <ROW> List<ROW> select(Sql sql, ROW query, IterationCallback<ROW> callback) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return select(sqlString, context.getParameters(), getClass(query), callback);
    }


    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectMap(sqlString, context.getParameters());
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectMap(Object... query) {
        IterationCallback<Map<String, Object>> callback = new DBListIterationCallback<>();
        for (Object o : query) {
            if (o instanceof IterationCallback) {
                callback = (IterationCallback<Map<String, Object>>) o;
            }
        }
        return selectMap(SIMPLE_WHERE, query(query), callback);
    }


    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, IterationCallback<Map<String, Object>> callback) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectMap(sqlString, context.getParameters(), callback);
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Each callback) {
        return selectMap(sql, query, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
    }


    public <ROW> Optional<ROW> selectOne(Sql sql, ROW query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectOne(sqlString, context.getParameters(), getClass(query));
    }


    public Optional<Map<String, Object>> selectOneMap(Sql sql, Map<String, Object> query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectOneMap(sqlString, context.getParameters());
    }


    public Optional<Map<String, Object>> selectOneMap(Object... query) {
        return selectOneMap(SIMPLE_WHERE, query(query));
    }


    public Optional<BigDecimal> selectOneNumber(Sql sql, Map<String, Object> query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectOneNumber(sqlString, context.getParameters());
    }


    public <QUERY> Optional<BigDecimal> selectOneNumber(Sql sql, QUERY query) {
        SqlContext context = createSqlContext(query);
        String sqlString = sql.getSql(context);
        return selectOneNumber(sqlString, context.getParameters());
    }

    protected <T> Class<T> getClass(T t) {
        return ClassUtil.getClass(t);
    }


    public boolean existsRecord(String sql) {
        return existsRecord(sql, null);
    }


    public boolean existsRecord(String sql, Map<String, Object> query) {
        List<Map<String, Object>> result = selectMap(sql, query);
        return result.isEmpty() == false;
    }


    public boolean existsRecord(Sql sql) throws SQLRuntimeException {
        @SuppressWarnings("rawtypes")
        List<Map> result = this.select(sql, Map.class);
        return result.isEmpty() == false;
    }


    public <QUERY> boolean existsRecord(Sql sql, QUERY query) {
        List<QUERY> result = this.select(sql, query);
        return result.isEmpty() == false;
    }


    public boolean existsRecord(Object... query) {
        return existsRecord(SIMPLE_WHERE, query(query));
    }




    public <ENTITY> boolean exists(ENTITY entity) {
        return exists(tableNameResolver.getTableName(entity, getConnection()), entity);
    }


    public boolean exists(String tableName, Object... entities) {
        if (entities == null) {
            throw new IllegalArgumentException(Message.getMessage("00001"));
        }
        if (entities[0] == null) {
            throw new IllegalArgumentException(Message.getMessage("00001"));
        }
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        Connection connection = null;
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            Map<String, ParameterValue> values = new HashMap<>();
            List<String> whereColumnNames = getWhereColumnNames(tableMetaData, null, null, CRUD.UPDATE);
            for (Object entity : entities) {
                addWhereValues(entity, whereColumnNames, tableMetaData, values);
            }
            Map<String, Object> pks = toPkValues(values);
            pks.put(TABLE_NAME, tableName);

            BigDecimal count = selectOneNumber(SIMPLE_COUNT_WHERE, pks).orElse(BigDecimal.ZERO);
            if (count.longValue() == 1) {
                return true;
            }
            return false;
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    protected <ENTITY> List<String> getWhereColumnNames(TableMetaData tableMetaData, ENTITY entity, Locking locking, CRUD crud) {
        List<String> columnNames = tableMetaData.getPkColumnNames();
        if (columnNames.isEmpty() == true) {
            if (crud.equals(CRUD.DELETE) == false) {
                throw new IllegalStateException(Message.getMessage("00002"));
            } else {
                columnNames = tableMetaData.getColumnNames();
            }
        }
        List<String> whereColumnNames = new ArrayList<>(columnNames);

        if (locking == OPTIMISTIC_LOCKING) {
            addVersionNoColumnNames(entity, tableMetaData, whereColumnNames);
            addTimestampColumnNames(entity, tableMetaData, whereColumnNames);
        }

        return whereColumnNames;
    }

    protected <ENTITY> void addVersionNoColumnNames(ENTITY entity, TableMetaData tableMetaData, List<String> whereColumnNames) {
        addWhereColumnNames(entity, tableMetaData, VersionNo.class, whereColumnNames);
    }

    protected <ENTITY> void addTimestampColumnNames(ENTITY entity, TableMetaData tableMetaData, List<String> whereColumnNames) {
        addWhereColumnNames(entity, tableMetaData, Timestamp.class, whereColumnNames);
    }

    protected <ENTITY> void addWhereColumnNames(ENTITY entity, TableMetaData tableMetaData, Class<? extends Annotation> annotationClass,
                                                List<String> whereColumnNames) {

        ObjectDesc<ENTITY> objectDesc = ObjectDescFactory.getObjectDesc(entity);
        List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(annotationClass);
        propertyDescs.forEach(propertyDesc -> {
            String propertyName = getColumnName(propertyDesc);
            ColumnMetaData columnMetaData = metaDataService.getColumnMetaData(tableMetaData, propertyName);
            whereColumnNames.add(columnMetaData.getColumnName());
        });
    }

    protected Map<String, Object> toPkValues(Map<String, ParameterValue> values) {
        Map<String, Object> ret = new CaseInsensitiveMap<>();
        values.keySet().forEach(key -> ret.put(key.replaceFirst(sqlBuilder.whereColumnPrefix(), ""), values.get(key).getValue()));
        return ret;
    }

    public <ROW> List<ROW> select(String sql, Class<ROW> as, Consumer<ROW> callback) {
        return select(sql, as, new ConsumerWrapper<>(callback));
    }


    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, Consumer<ROW> callback) {
        return select(sql, arg, as, new ConsumerWrapper<>(callback));
    }


    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Consumer<Map<String, Object>> callback) {
        return selectMap(sql, arg, new ConsumerWrapper<>(callback));
    }


    public List<Map<String, Object>> selectMap(String sql, Consumer<Map<String, Object>> callback) {
        return selectMap(sql, new ConsumerWrapper<>(callback));
    }


    public <ROW> List<ROW> select(Sql sql, ROW query, Consumer<ROW> callback) {
        return select(sql, query, new ConsumerWrapper<>(callback));
    }


    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Consumer<Map<String, Object>> callback) {
        return selectMap(sql, query, new ConsumerWrapper<>(callback));
    }

    protected <ENTITY> void addWhereValues(ENTITY entity, List<String> whereColumnNames, TableMetaData tableMetaData,
                                           Map<String, ParameterValue> values) {
        String tableName = tableMetaData.getTableName();
        for (String columnName : whereColumnNames) {
            String name = sqlBuilder.whereColumnPrefix() + columnName;
            if (values.containsKey(name) == true && values.get(name).getValue() != null) {
                continue;
            }
            List<CandidateValue> vals = new ArrayList<>();
            entityIntrospector.gatherValue(new Object[]{entity}, tableName, columnName, vals);
            CandidateValue value = CandidateValue.getValue(vals, tableName, columnName);
            int dataType = tableMetaData.getColumnMetaData(columnName).getDataType();
            value.pullRealValue();
            ParameterValue pv = createParameterValue(name, dataType, value);
            values.put(name, pv);
        }
    }
}
