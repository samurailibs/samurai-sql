package jp.dodododo.dao;

import jp.dodododo.dao.columns.NoPersistentColumns;
import jp.dodododo.dao.columns.PersistentColumns;
import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.lock.Locking;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.script.Each;
import jp.dodododo.dao.sql.Sql;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface SamuraiSqlClient {
    SqlLogRegistry getSqlLogRegistry();

    void setQueryTimeout(int seconds);

    DaoConfig getConfig();

    <ROW> List<ROW> select(Class<ROW> returnType, Object... args);

    <ROW> Optional<ROW> selectOne(Class<ROW> returnType, Object... args);

    <ROW> List<ROW> select(String sql, Class<ROW> as);

    <ROW> List<ROW> select(String sql, Class<ROW> as, jp.dodododo.dao.script.Each callback);

    <ROW> List<ROW> select(String sql, Class<ROW> as, Consumer<ROW> callback);

    <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, jp.dodododo.dao.script.Each callback);

    <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, Consumer<ROW> callback);

    <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as);

    <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, IterationCallback<ROW> callback);

    <ROW> List<ROW> select(String sql, Class<ROW> as, IterationCallback<ROW> callback);

    <ROW> Optional<ROW> selectOne(String sql, Map<String, Object> arg, Class<ROW> as);

    <ROW> Optional<ROW> selectOne(String sql, Class<ROW> as);

    List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg);

    List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, IterationCallback<Map<String, Object>> callback);

    List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, jp.dodododo.dao.script.Each callback);

    List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Consumer<Map<String, Object>> callback);

    List<Map<String, Object>> selectMap(String sql);

    List<Map<String, Object>> selectMap(String sql, IterationCallback<Map<String, Object>> callback);

    List<Map<String, Object>> selectMap(String sql, jp.dodododo.dao.script.Each callback);

    List<Map<String, Object>> selectMap(String sql, Consumer<Map<String, Object>> callback);

    Optional<Map<String, Object>> selectOneMap(String sql, Map<String, Object> arg);

    Optional<Map<String, Object>> selectOneMap(String sql);

    Optional<BigDecimal> selectOneNumber(String sql, Map<String, Object> arg);

    Optional<BigDecimal> selectOneNumber(String sql);

    <ROW> List<ROW> select(Sql sql, Class<ROW> as);

    <ROW> List<ROW> select(Sql sql, ROW query);

    <ROW> List<ROW> select(Sql sql, ROW query, jp.dodododo.dao.script.Each callback);

    <ROW> List<ROW> select(Sql sql, ROW query, IterationCallback<ROW> callback);

    <ROW> List<ROW> select(Sql sql, ROW query, Consumer<ROW> callback);

    <ROW> List<ROW> select(Sql sql, Map<String, Object> arg, Class<ROW> as);

    <ROW> Optional<ROW> selectOne(Sql sql, ROW query);

    List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query);

    List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, IterationCallback<Map<String, Object>> callback);

    List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Each callback);

    List<Map<String, Object>> selectMap(Object... query);

    List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Consumer<Map<String, Object>> callback);

    Optional<Map<String, Object>> selectOneMap(Sql sql, Map<String, Object> query);

    Optional<Map<String, Object>> selectOneMap(Object... query);

    Optional<BigDecimal> selectOneNumber(Sql sql, Map<String, Object> query);

    <QUERY> Optional<BigDecimal> selectOneNumber(Sql sql, QUERY query);

    boolean existsRecord(String sql);

    boolean existsRecord(String sql, Map<String, Object> query);

    boolean existsRecord(Sql sql);

    <QUERY> boolean existsRecord(Sql sql, QUERY query);

    boolean existsRecord(Object... query);

    boolean exists(String tableName, Object... entities);

    <ENTITY> boolean exists(ENTITY entity);

    <ENTITY> int insert(ENTITY entity) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity) throws SQLRuntimeException;

    <ENTITY> int delete(ENTITY entity) throws SQLRuntimeException;

    int insert(String tableName, Object... entity) throws SQLRuntimeException;

    int update(String tableName, Object... entity) throws SQLRuntimeException;

    <ENTITY> int delete(String tableName, ENTITY entity) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities) throws SQLRuntimeException;

    <ENTITY> int[] delete(Collection<ENTITY> entities) throws SQLRuntimeException;

    int executeInsert(String sql) throws SQLRuntimeException;

    int executeInsert(String sql, Map<String, Object> arg) throws SQLRuntimeException;

    int executeUpdate(String sql) throws SQLRuntimeException;

    int executeUpdate(String sql, Map<String, Object> arg) throws SQLRuntimeException;

    int executeDelete(String sql) throws SQLRuntimeException;

    int executeDelete(String sql, Map<String, Object> arg) throws SQLRuntimeException;

    int[] executeBatch(String sql, List<Object> arg) throws SQLRuntimeException;

    int execute(Sql sql, Object... entity) throws SQLRuntimeException;

    <T> T getLastInsertId(Class<T> returnType);

    <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int insert(ENTITY entity, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int insert(ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int insert(ENTITY entity, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int insert(String tableName, ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(ENTITY entity, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int update(String tableName, ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int delete(ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int delete(String tableName, ENTITY entity, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) throws SQLRuntimeException;

    <ENTITY> int[] delete(Collection<ENTITY> entities, Locking locking) throws SQLRuntimeException;

}
