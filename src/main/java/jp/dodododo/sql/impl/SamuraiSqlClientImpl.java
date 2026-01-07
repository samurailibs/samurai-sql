package jp.dodododo.sql.impl;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.*;
import jp.dodododo.sql.bootstrap.ClientBootstrap;
import jp.dodododo.sql.columns.NoPersistentColumns;
import jp.dodododo.sql.columns.PersistentColumns;
import jp.dodododo.sql.env.SqlEnvironment;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.executor.common.PreparedStatementFactory;
import jp.dodododo.sql.executor.crud.CrudExecutor;
import jp.dodododo.sql.executor.query.QueryExecutor;
import jp.dodododo.sql.handler.ResultSetHandler;
import jp.dodododo.sql.lock.Locking;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.mapping.EntityIntrospector;
import jp.dodododo.sql.metadata.MetaDataService;
import jp.dodododo.sql.metadata.TableNameResolver;
import jp.dodododo.sql.script.Each;
import jp.dodododo.sql.sql.Sql;
import jp.dodododo.sql.sql.SqlContext;
import jp.dodododo.sql.value.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.Consumer;

public class SamuraiSqlClientImpl implements SamuraiSqlClient {
    protected static final Logger logger = LoggerFactory.getLogger(SamuraiSqlClientImpl.class);

    protected ClientBootstrap clientBootstrap;

    protected SqlEnvironment sqlEnvironment;

    protected PreparedStatementFactory statementFactory;

    protected MetaDataService metaDataService;

    protected TableNameResolver tableNameResolver = new TableNameResolver();

    protected EntityIntrospector entityIntrospector;

    protected CrudExecutor crudExecutor;

    protected QueryExecutor queryExecutor;

    {
        this.sqlEnvironment = new SqlEnvironment();
        this.clientBootstrap = new ClientBootstrap(sqlEnvironment);
        this.statementFactory = new PreparedStatementFactory(
                sqlEnvironment.sqlNodeCache(),
                sqlEnvironment.sqlLogger(),
                sqlEnvironment.sqlLogRegistry(),
                sqlEnvironment.sqlConfig(),
                sqlEnvironment.sqlConfig().getQueryTimeout()
        );
    }

    public SamuraiSqlClientImpl(Object obj) {
        if (obj instanceof String jndiName) {
            clientBootstrap.bootstrap(jndiName);
        } else if (obj instanceof Connection connection) {
            clientBootstrap.bootstrap(connection);
        } else if (obj instanceof DataSource dataSource) {
            clientBootstrap.bootstrap(dataSource);
        } else {
            throw new UnsupportedOperationException("arg is " + obj.getClass().getName());
        }
        initAfterBootstrap();
    }

    public SamuraiSqlClientImpl(String jndiName) {
        clientBootstrap.bootstrap(jndiName);
        initAfterBootstrap();
    }

    public SamuraiSqlClientImpl(Connection connection) {
        clientBootstrap.bootstrap(connection);
        initAfterBootstrap();
    }

    public SamuraiSqlClientImpl(DataSource dataSource) {
        clientBootstrap.bootstrap(dataSource);
        initAfterBootstrap();
    }

    protected void initAfterBootstrap() {
        this.metaDataService = new MetaDataService(clientBootstrap.connectionProvider());
        this.entityIntrospector = new EntityIntrospector(metaDataService);

        this.crudExecutor = new CrudExecutor(
                metaDataService,
                clientBootstrap.connectionProvider(),
                clientBootstrap.dialectProvider(),
                sqlEnvironment,
                tableNameResolver,
                entityIntrospector,
                statementFactory
        );
        this.queryExecutor = new QueryExecutor(
                clientBootstrap.connectionProvider(),
                metaDataService,
                clientBootstrap.dialectProvider(),
                sqlEnvironment,
                tableNameResolver,
                entityIntrospector,
                statementFactory
        );
    }

    @Override
    public int insert(String tableName, Object... entity) {
        return crudExecutor.insert(tableName, entity);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity) {
        return crudExecutor.insert(entity);
    }

    @Override
    public <ENTITY> int delete(String tableName, ENTITY entity) {
        return crudExecutor.delete(tableName, entity);
    }

    @Override
    public <ENTITY> int delete(ENTITY entity) {
        return crudExecutor.delete(entity);
    }

    @Override
    public int update(String tableName, Object... entity) {
        return crudExecutor.update(tableName, entity);
    }

    @Override
    public <ENTITY> int update(ENTITY entity) {
        return crudExecutor.update(entity);
    }

    @Override
    public <ENTITY> int[] delete(Collection<ENTITY> entities) {
        return crudExecutor.delete(entities);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities) {
        return crudExecutor.insert(entities);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities) {
        return crudExecutor.update(entities);
    }

    @Override
    public <ROW> Optional<ROW> selectOne(Class<ROW> returnType, Object... args) {
        return queryExecutor.selectOne(returnType, args);
    }

    @Override
    public <ROW> List<ROW> select(Class<ROW> returnType, Object... args) {
        return queryExecutor.select(returnType, args);
    }

    @Internal
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, IterationCallback<ROW> callback, ResultSetHandler<?> handler, boolean isDynamic) {
        return queryExecutor.select(sql, arg, callback, handler, isDynamic);
    }

    @Override
    public <ROW> Optional<ROW> selectOne(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
        return queryExecutor.selectOne(sql, arg, entityClass);
    }

    @Override
    public <ROW> Optional<ROW> selectOne(String sql, Class<ROW> entityClass) {
        return queryExecutor.selectOne(sql, entityClass);
    }

    @Override
    public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc) {
        return crudExecutor.insert(tableName, entity, npc);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc) {
        return crudExecutor.insert(entity, npc);
    }

    @Override
    public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc) {
        return crudExecutor.insert(tableName, entity, pc);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity, PersistentColumns pc) {
        return crudExecutor.insert(entity, pc);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc) {
        return crudExecutor.insert(entities, npc);
    }

    @Override
    public <ENTITY> int insert(String tableName, ENTITY entity, Locking locking) {
        return crudExecutor.insert(tableName, entity, locking);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity, Locking locking) {
        return crudExecutor.insert(entity, locking);
    }

    @Override
    public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.insert(tableName, entity, npc, locking);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.insert(entity, npc, locking);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc) {
        return crudExecutor.insert(entities, pc);
    }

    @Override
    public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
        return crudExecutor.insert(tableName, entity, pc, locking);
    }

    @Override
    public <ENTITY> int insert(ENTITY entity, PersistentColumns pc, Locking locking) {
        return crudExecutor.insert(entity, pc, locking);
    }

    @Override
    public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc) {
        return crudExecutor.update(tableName, entity, npc);
    }

    @Override
    public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc) {
        return crudExecutor.update(entity, npc);
    }

    @Override
    public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc) {
        return crudExecutor.update(tableName, entity, pc);
    }

    @Override
    public <ENTITY> int update(ENTITY entity, PersistentColumns pc) {
        return crudExecutor.update(entity, pc);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc) {
        return crudExecutor.update(entities, npc);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc) {
        return crudExecutor.update(entities, pc);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities, Locking locking) {
        return crudExecutor.insert(entities, locking);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.insert(entities, npc, locking);
    }

    @Override
    public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
        return crudExecutor.insert(entities, pc, locking);
    }

    @Override
    public <ENTITY> int update(String tableName, ENTITY entity, Locking locking) {
        return crudExecutor.update(tableName, entity, locking);
    }

    @Override
    public <ENTITY> int update(ENTITY entity, Locking locking) {
        return crudExecutor.update(entity, locking);
    }

    @Override
    public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.update(tableName, entity, npc, locking);
    }

    @Override
    public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.update(entity, npc, locking);
    }

    @Override
    public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
        return crudExecutor.update(tableName, entity, pc, locking);
    }

    @Override
    public <ENTITY> int update(ENTITY entity, PersistentColumns pc, Locking locking) {
        return crudExecutor.update(entity, pc, locking);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities, Locking locking) {
        return crudExecutor.update(entities, locking);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
        return crudExecutor.update(entities, npc, locking);
    }

    @Override
    public <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
        return crudExecutor.update(entities, pc, locking);
    }

    /**
     * Unsupported next list.
     * <ul>
     * <li>@Id
     * <li>@Timestamp
     * <li>@VersionNo
     * <li>Locking
     * </ul>
     */
    @Override
    public int[] executeBatch(String sql, List<Object> list) {
        return crudExecutor.executeBatch(sql, list);
    }

    @Override
    public <ENTITY> int delete(ENTITY entity, Locking locking) {
        return crudExecutor.delete(entity, locking);
    }

    @Override
    public <ENTITY> int delete(String tableName, ENTITY entity, Locking locking) {
        return crudExecutor.delete(tableName, entity, locking);
    }

    @Override
    public <ENTITY> int[] delete(Collection<ENTITY> entities, Locking locking) {
        return crudExecutor.delete(entities, locking);
    }

    @Override
    public int executeInsert(String sql) {
        return crudExecutor.executeInsert(sql);
    }

    @Override
    public int executeInsert(String sql, Map<String, Object> arg) {
        return crudExecutor.executeInsert(sql, arg);
    }

    @Override
    public int executeUpdate(String sql) {
        return crudExecutor.executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, Map<String, Object> arg) {
        return crudExecutor.executeUpdate(sql, arg);
    }

    @Override
    public int executeDelete(String sql) {
        return crudExecutor.executeDelete(sql);
    }

    @Override
    public int executeDelete(String sql, Map<String, Object> arg) {
        return crudExecutor.executeDelete(sql, arg);
    }

    @Override
    public int execute(Sql sql, Object... entity) throws SQLRuntimeException {
        return crudExecutor.execute(sql, entity);
    }

    @Override
    public Optional<Map<String, Object>> selectOneMap(String sql, Map<String, Object> arg) {
        return queryExecutor.selectOneMap(sql, arg);
    }

    @Override
    public Optional<Map<String, Object>> selectOneMap(String sql) {
        return queryExecutor.selectOneMap(sql);
    }

    @Override
    public Optional<BigDecimal> selectOneNumber(String sql, Map<String, Object> arg) {
        return queryExecutor.selectOneNumber(sql, arg);
    }

    @Override
    public Optional<BigDecimal> selectOneNumber(String sql) {
        return queryExecutor.selectOneNumber(sql);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
        return queryExecutor.select(sql, arg, entityClass);
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, Map<String, Object> arg, Class<ROW> entityClass) throws SQLRuntimeException {
        return queryExecutor.select(sql, arg, entityClass);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, Each callback) {
        return queryExecutor.select(sql, arg, entityClass, callback);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, IterationCallback<ROW> callback) {
        return queryExecutor.select(sql, arg, entityClass, callback);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass) {
        return queryExecutor.select(sql, entityClass);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, Each callback) {
        return queryExecutor.select(sql, entityClass, callback);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, IterationCallback<ROW> callback) {
        return queryExecutor.select(sql, entityClass, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg) {
        return queryExecutor.selectMap(sql, arg);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, IterationCallback<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, arg, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Each callback) {
        return queryExecutor.selectMap(sql, arg, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql) {
        return queryExecutor.selectMap(sql);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, IterationCallback<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Each callback) {
        return queryExecutor.selectMap(sql, callback);
    }

    @Override
    public SqlLogRegistry getSqlLogRegistry() {
        return sqlEnvironment.sqlLogRegistry();
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, Class<ROW> entityClass) {
        return queryExecutor.select(sql, entityClass);
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, ROW query) {
        return queryExecutor.select(sql, query);
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, ROW query, Each callback) {
        return queryExecutor.select(sql, query, callback);
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, ROW query, IterationCallback<ROW> callback) {
        return queryExecutor.select(sql, query, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query) {
        return queryExecutor.selectMap(sql, query);
    }

    @Override
    public List<Map<String, Object>> selectMap(Object... query) {
        return queryExecutor.selectMap(query);
    }

    @Override
    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, IterationCallback<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, query, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Each callback) {
        return queryExecutor.selectMap(sql, query, callback);
    }

    @Override
    public <ROW> Optional<ROW> selectOne(Sql sql, ROW query) {
        return queryExecutor.selectOne(sql, query);
    }

    @Override
    public Optional<Map<String, Object>> selectOneMap(Sql sql, Map<String, Object> query) {
        return queryExecutor.selectOneMap(sql, query);
    }

    @Override
    public Optional<Map<String, Object>> selectOneMap(Object... query) {
        return queryExecutor.selectOneMap(query);
    }

    @Override
    public Optional<BigDecimal> selectOneNumber(Sql sql, Map<String, Object> query) {
        return queryExecutor.selectOneNumber(sql, query);
    }

    @Override
    public <QUERY> Optional<BigDecimal> selectOneNumber(Sql sql, QUERY query) {
        return queryExecutor.selectOneNumber(sql, query);
    }

    @Override
    public boolean existsRecord(String sql) {
        return queryExecutor.existsRecord(sql);
    }

    @Override
    public boolean existsRecord(String sql, Map<String, Object> query) {
        return queryExecutor.existsRecord(sql, query);
    }

    @Override
    public boolean existsRecord(Sql sql) throws SQLRuntimeException {
        return queryExecutor.existsRecord(sql);
    }

    @Override
    public <QUERY> boolean existsRecord(Sql sql, QUERY query) {
        return queryExecutor.existsRecord(sql, query);
    }

    @Override
    public boolean existsRecord(Object... query) {
        return queryExecutor.existsRecord(query);
    }

    @Override
    public <T> T getLastInsertId(Class<T> returnType) {
        return crudExecutor.getLastInsertId(returnType);
    }

    @Override
    public <ENTITY> boolean exists(ENTITY entity) {
        return queryExecutor.exists(entity);
    }

    @Override
    public boolean exists(String tableName, Object... entities) {
        return queryExecutor.exists(tableName, entities);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Class<ROW> as, Consumer<ROW> callback) {
        return queryExecutor.select(sql, as, callback);
    }

    @Override
    public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, Consumer<ROW> callback) {
        return queryExecutor.select(sql, arg, as, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Consumer<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, arg, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(String sql, Consumer<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, callback);
    }

    @Override
    public <ROW> List<ROW> select(Sql sql, ROW query, Consumer<ROW> callback) {
        return queryExecutor.select(sql, query, callback);
    }

    @Override
    public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Consumer<Map<String, Object>> callback) {
        return queryExecutor.selectMap(sql, query, callback);
    }

    @Internal
    public CrudExecutor.Triple<Integer, Connection, PreparedStatement> executeUpdate(Map<String, ParameterValue> values, String sql, SqlContext context, boolean isDynamic) {
        return crudExecutor.executeUpdate( values,  sql,  context,  isDynamic);
    }
}
