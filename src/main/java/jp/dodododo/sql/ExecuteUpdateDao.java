package jp.dodododo.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.impl.RdbDao;
import jp.dodododo.sql.sql.Sql;

public interface ExecuteUpdateDao {
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
}
