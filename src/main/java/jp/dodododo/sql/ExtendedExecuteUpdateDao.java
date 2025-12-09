package jp.dodododo.sql;

import java.util.Collection;

import jp.dodododo.sql.columns.NoPersistentColumns;
import jp.dodododo.sql.columns.PersistentColumns;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.lock.Locking;

public interface ExtendedExecuteUpdateDao extends ExecuteUpdateDao {
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
