package jp.dodododo.dao.sql;

import static jp.dodododo.dao.sql.GenericSql.*;
import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.Assert.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.DialectManager;
import jp.dodododo.dao.dialect.MySQL;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.impl.Emp;
import jp.dodododo.dao.unit.DbTestExtension;
import jp.dodododo.dao.value.ParameterValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class GenericSqlTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testGetSql_ALL() {
		String tableName = "emp";
		List<ParameterValue> columns = null;
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.ALL.getSql(context);
		assertEquals("SELECT * FROM emp /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/", sql);
	}

	@Test
	public void testGetSql_SIMPLE_WHERE_0() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.SIMPLE_WHERE.getSql(context);
		assertEquals("SELECT * FROM emp /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/", sql);
	}

	@Test
	public void testGetSql_SIMPLE_WHERE_1() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.SIMPLE_WHERE.getSql(context);
		assertEquals("SELECT * FROM emp WHERE emp_id = /*emp_id*/'dummy' /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/", sql);
	}

	@Test
	public void testGetSql_SIMPLE_WHERE_2() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.SIMPLE_WHERE.getSql(context);
		assertEquals(
				"SELECT * FROM emp WHERE emp_id = /*emp_id*/'dummy' AND emp_name = /*emp_name*/'dummy' /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/",
				sql);
	}

	@Test
	public void testGetSql_INSERT() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.INSERT.getSql(context);
		assertEquals("INSERT INTO emp (emp_id ,emp_name) VALUES (/*emp_id*/'dummy' ,/*emp_name*/'dummy')", sql);
	}

	@Test
	public void testGetSql_UPDATE() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		List<ParameterValue> pks = new ArrayList<ParameterValue>();
		pks.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, pks, columns, queryClass, dialect);
		String sql = GenericSql.UPDATE.getSql(context);
		assertEquals("UPDATE emp SET emp_id = /*emp_id*/'dummy' ,emp_name = /*emp_name*/'dummy' WHERE emp_id = /*emp_id*/'dummy'", sql);
	}

	@Test
	public void testGetSql_DELETE() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		List<ParameterValue> pks = new ArrayList<ParameterValue>();
		pks.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, pks, columns, queryClass, dialect);
		String sql = GenericSql.DELETE.getSql(context);
		assertEquals("DELETE FROM emp WHERE emp_id = /*emp_id*/'dummy'", sql);
	}

	@Test
	public void testINSERT_BATCH_Basic() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		List<Emp> beans = new ArrayList<Emp>();
		beans.add(new Emp("1", "MIKE"));
		beans.add(new Emp("2", "ROD"));
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		Class<?> queryClass = null;
		Dialect dialect = null;
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		context.setValues(beans);
		String sql = GenericSql.INSERT_BATCH.getSql(context);
		assertEquals(
				"INSERT INTO emp (emp_id ,emp_name) VALUES (/*vals[0].emp_id*/'dummy' ,/*vals[0].emp_name*/'dummy') , (/*vals[1].emp_id*/'dummy' ,/*vals[1].emp_name*/'dummy')",
				sql);
	}

	@Test
	public void testINSERT_BATCH_DaoMap() {
		if (DialectManager.getDialect(getDataSource()).isSupportMultiRowInsert() == false) {
			return;
		}
		dao = newTestDao(getDataSource());

		int beforeCount = dao.select(SIMPLE_COUNT_WHERE, args(TABLE_NAME, "emp"), Integer.class).get(0);

		List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
		objects.add(map("EMPNO", 1, "ENAME", null));
		objects.add(map("EMPNO", 2, "ENAME", "ROD"));
		int count = dao.execute(GenericSql.INSERT_BATCH, into("emp"), values(objects));
		assertEquals(2, count);

		int afterCount = dao.select(SIMPLE_COUNT_WHERE, args(TABLE_NAME, "emp"), Integer.class).get(0);

		assertEquals(beforeCount + 2, afterCount);
		Emp one = dao.selectOne(SIMPLE_WHERE, new Emp("1", null)).get();
		Emp two = dao.selectOne(SIMPLE_WHERE, new Emp("2", null)).get();
		assertNull(one.getENAME());
		assertEquals("ROD", two.getENAME());
	}

	@Test
	public void testINSERT_BATCH_DaoBean() {
		if (DialectManager.getDialect(getDataSource()).isSupportMultiRowInsert() == false) {
			return;
		}
		dao = newTestDao(getDataSource());

		int beforeCount = dao.select(SIMPLE_COUNT_WHERE, args(TABLE_NAME, "emp"), Integer.class).get(0);

		List<Emp> beans = new ArrayList<Emp>();
		beans.add(new Emp("1", null));
		beans.add(new Emp("2", "ROD"));
		int count = dao.execute(GenericSql.INSERT_BATCH, into("emp"), values(beans));
		assertEquals(2, count);

		int afterCount = dao.select(SIMPLE_COUNT_WHERE, args(TABLE_NAME, "emp"), Integer.class).get(0);

		assertEquals(beforeCount + 2, afterCount);
		Emp one = dao.selectOne(SIMPLE_WHERE, new Emp("1", null)).get();
		Emp two = dao.selectOne(SIMPLE_WHERE, new Emp("2", null)).get();
		assertNull(one.getENAME());
		assertEquals("ROD", two.getENAME());
	}

	@Test
	public void testGetSql_REPLACE() {
		String tableName = "emp";
		List<ParameterValue> columns = new ArrayList<ParameterValue>();
		columns.add(new ParameterValue("emp_id", Types.INTEGER, 10, true));
		columns.add(new ParameterValue("emp_name", Types.VARCHAR, "name", true));
		Class<?> queryClass = null;
		Dialect dialect = new MySQL();
		SqlContext context = new SqlContext(tableName, columns, queryClass, dialect);
		String sql = GenericSql.REPLACE.getSql(context);
		assertEquals("INSERT INTO emp (emp_id ,emp_name) VALUES (/*emp_id*/'dummy' ,/*emp_name*/'dummy') ON DUPLICATE KEY UPDATE", sql);

		dialect = new SQLite();
		context = new SqlContext(tableName, columns, queryClass, dialect);
		sql = GenericSql.REPLACE.getSql(context);
		assertEquals("INSERT OR REPLACE INTO emp (emp_id ,emp_name) VALUES (/*emp_id*/'dummy' ,/*emp_name*/'dummy')", sql);
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
