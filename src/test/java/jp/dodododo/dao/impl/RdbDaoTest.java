package jp.dodododo.dao.impl;

import static jp.dodododo.dao.sql.GenericSql.*;
import static jp.dodododo.dao.sql.Operator.*;
import static jp.dodododo.dao.sql.orderby.SortType.*;
import static jp.dodododo.dao.unit.Assert.*;
import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.Each;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Columns;
import jp.dodododo.dao.annotation.Dialects;
import jp.dodododo.dao.annotation.Rel;
import jp.dodododo.dao.annotation.Relations;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.DialectManager;
import jp.dodododo.dao.dialect.HSQL;
import jp.dodododo.dao.dialect.Oracle;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.exception.NoParameterizedException;
import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.impl.EmpConstructorHasBeanAnnotatedDept.TestDept;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.metadata.TableMetaData;
import jp.dodododo.dao.paging.LimitOffset;
import jp.dodododo.dao.paging.Paging;
import jp.dodododo.dao.row.Row;
import jp.dodododo.dao.unit.DbTestExtension;
import jp.dodododo.dao.util.ReaderUtil;
import jp.dodododo.dao.util.StringUtil;
import jp.dodododo.dao.value.CandidateValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RdbDaoTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@BeforeEach
	public void createSequence() throws SQLException {
		try (Connection connection = dbTestExtension.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("CREATE SEQUENCE IF NOT EXISTS sequence START WITH 1");
		}
	}

	@Test
	public void testInsertENTITY() {
		dao = newTestDao(getDataSource());
		Emp emp = new Emp();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setEMPNO("1");
		emp.setENAME("ename");
		int count = dao.insert(emp);
		assertEquals(1, count);

		try {
			dao.insert(emp);
			fail();
		} catch (SQLRuntimeException success) {
		}
	}

	@Test
	public void testSelectClassMaps() {
		dao = newTestDao(getDataSource());
		List<Row> list = dao.select(Row.class, from("EMP"), where("JOB", "CLERK"), orderBy("ENAME", DESC));
		assertEquals(4, list.size());
		String sql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE JOB = 'CLERK' ORDER BY ENAME DESC", sql);
		assertEquals("SMITH", list.get(0).getString("ename"));
		assertEquals("MILLER", list.get(1).getString("ename"));
		assertEquals("JAMES", list.get(2).getString("ename"));
		assertEquals("ADAMS", list.get(3).getString("ename"));

		List<Emp> empList = dao.select(Emp.class, from("EMP"), where("JOB", "CLERK"), orderBy("EMPNO"));
		assertEquals(4, empList.size());
		sql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE JOB = 'CLERK' ORDER BY EMPNO", sql);
	}

	@Test
	public void testSelectEntityNotDefaultConstructor() {
		dao = newTestDao(getDataSource());
		List<EmpHasNotDefaultConstructor> list = dao.select("select * from EMP order by EMPNO desc", EmpHasNotDefaultConstructor.class);

		EmpHasNotDefaultConstructor emp = list.get(0);
		assertEquals("7934", emp.getEMPNO());
		assertNull(emp.getENAME());
		assertNull(emp.getTSTAMP());
	}

	@Test
	public void testSelectEntityConstructorHasBean() {
		dao = newTestDao(getDataSource());
		List<EmpConstructorHasDept> list = dao
				.select("select EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, EMP.TSTAMP, DEPT.DEPTNO, DNAME from EMP, DEPT where EMP.DEPTNO = DEPT.DEPTNO order by EMPNO desc, DEPT.DEPTNO desc",
						EmpConstructorHasDept.class);

		EmpConstructorHasDept emp = list.get(0);
		Dept dept = emp.getDept();
		Dept dept2 = emp.getDept2();
		assertEquals("7934", emp.getEMPNO());
		assertEquals("MILLER", emp.getENAME());
		assertNotNull(emp.getTSTAMP());
		assertEquals("10", dept.getDEPTNO());
		assertEquals("ACCOUNTING", dept.getDNAME());
		assertEquals("10", dept2.getDEPTNO());
		assertEquals("ACCOUNTING", dept2.getDNAME());
	}

	@Test
	public void testSelectEntityConstructorHasBeanAnnotatedDept() {
		dao = newTestDao(getDataSource());
		List<EmpConstructorHasBeanAnnotatedDept> list = dao
				.select("select EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, EMP.TSTAMP, DEPT.DEPTNO, DNAME from EMP, DEPT where EMP.DEPTNO = DEPT.DEPTNO order by EMPNO desc, DEPT.DEPTNO desc",
						EmpConstructorHasBeanAnnotatedDept.class);

		EmpConstructorHasBeanAnnotatedDept emp = list.get(0);
		Dept dept = emp.getDept();
		Dept dept2 = emp.getDept2();
		assertEquals("7934", emp.getEMPNO());
		assertEquals("MILLER", emp.getENAME());
		assertNotNull(emp.getTSTAMP());
		assertEquals("10", dept.getDEPTNO());
		assertEquals("ACCOUNTING", dept.getDNAME());
		assertEquals("10", dept2.getDEPTNO());
		assertEquals("ACCOUNTING", dept2.getDNAME());
		assertEquals(Dept.class, dept.getClass());
		assertEquals(TestDept.class, dept2.getClass());
	}

	@Test
	public void testSelectEntityEmpHasWritableField() {
		dao = newTestDao(getDataSource());
		List<EmpHasWritableField> list = dao.select("select * from EMP order by EMPNO desc", EmpHasWritableField.class);

		EmpHasWritableField emp = list.get(0);
		assertEquals("7934", emp.getEMPNO());
		assertEquals("MILLER", emp.getENAME());
		assertNotNull("value=" + emp.getTSTAMP(), emp.getTSTAMP());
	}

	@Test
	public void testSelectEntityEmpHasAlias() {
		dao = newTestDao(getDataSource());
		List<EmpHasAlias> list = dao.select("select * from EMP order by EMPNO desc", EmpHasAlias.class);

		EmpHasAlias emp = list.get(0);
		assertEquals("7934", emp.NO);
		assertEquals("MILLER", emp.ENAME);
		assertNotNull("value=" + emp.TSTAMP, emp.TSTAMP);
	}

	@Test
	public void testSelectIn() {
		dao = newTestDao(getDataSource());
		List<Emp> list = dao.select("SELECT * FROM EMP WHERE empno IN /*IN empNoList*/(1, 2)/*END*/ order by empno",
				args("empNoList", list(7369, 7499)), Emp.class);

		assertEquals(2, list.size());
		assertEquals("7369", list.get(0).getEMPNO());
		assertEquals("7499", list.get(1).getEMPNO());

		list = dao.select("SELECT * FROM EMP WHERE empno NOT IN /*IN empNoList*/(1, 2)/*END*/ order by empno", args("empNoList", list(7369, 7499)),
				Emp.class);

		assertEquals(12, list.size());
		assertEquals("7521", list.get(0).getEMPNO());
		assertEquals("7566", list.get(1).getEMPNO());
		assertEquals("7934", list.get(11).getEMPNO());
	}

	@Test
	public void testSelectSql() {
		dao = newTestDao(getDataSource());
		List<Emp> list = dao.select(SIMPLE_WHERE, args(TABLE_NAME, "emp", "EMPNO", 7934), Emp.class);

		assertEquals(1, list.size());
		Emp emp = list.get(0);
		assertEquals("7934", emp.getEMPNO());
		assertEquals("MILLER", emp.getENAME());
	}

	@Test
	public void testInsertBinary() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite) {
			return;
		}
		dao = newTestDao(getDataSource());
		BinaryTable binaryTable = new BinaryTable();
		InputStream binary = getClass().getClassLoader().getResourceAsStream("jp/dodododo/dao/dialect.properties");
		binaryTable.setBinary(binary);
		int count = dao.insert(binaryTable);
		assertEquals(1, count);

		binaryTable = dao.selectOne("select * from BINARY_TABLE where id=/*id*/0", args("id", binaryTable.getId()), BinaryTable.class).get();
		String expected = ReaderUtil.readText(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
				"jp/dodododo/dao/dialect.properties"), "UTF-8"));
		assertEquals(expected, ReaderUtil.readText(new InputStreamReader(binaryTable.getBinary(), "UTF-8")));
	}

	@Test
	public void testInsertENTITYHasIdAnnotation() {
		dao = newTestDao(getDataSource());
		EmpHasIdAnnotation emp = new EmpHasIdAnnotation();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setENAME("ename");
		int count = dao.insert(emp);
		assertEquals(1, count);
		String completeSql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertMatches(
				"INSERT INTO .* \\( EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO, TSTAMP \\) VALUES \\( .* , 'ename' , NULL , NULL , NULL , NULL , 2 , 10 , NULL \\)",
				completeSql);

		emp = new EmpHasIdAnnotation();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setENAME("ename");
		count = dao.insert(emp);
		assertEquals(1, count);
		completeSql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertMatches(
				"INSERT INTO .* \\( EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO, TSTAMP \\) VALUES \\( .* , 'ename' , NULL , NULL , NULL , NULL , 2 , 10 , NULL \\)",
				completeSql);
	}

	@Test
	public void testDeleteENTITY() {
		dao = newTestDao(getDataSource());
		Emp emp = new Emp();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setEMPNO("1");
		emp.setENAME("ename");
		int count = dao.delete(emp);
		assertEquals(0, count);

		count = dao.insert(emp);
		assertEquals(1, count);
		count = dao.delete(emp);
		assertEquals(1, count);
	}

	@Test
	public void testUpdateENTITY() {
		dao = newTestDao(getDataSource());
		Emp emp = new Emp();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setEMPNO("1");
		emp.setENAME("ename");
		int count = dao.update(emp);
		assertEquals(0, count);

		count = dao.insert(emp);
		assertEquals(1, count);
		count = dao.update(emp);
		assertEquals(1, count);
	}

	@Test
	public void testUpdateENTITYHasVersionNo() throws Exception {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		EmpHasVersionNoAnnotation emp = new EmpHasVersionNoAnnotation();
		emp.setEMPNO("1");
		emp.setDEPTNO("10");
		emp.setENAME("ename");
		int count = dao.insert(emp);
		assertEquals(1, count);
		assertTrue(StringUtil.equalsIgnoreCase("INSERT INTO EMP ( EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO, TSTAMP ) VALUES ( 1 , 'ename' , NULL , NULL , NULL , NULL , 1 , 10 , NULL )", logRegistry.getLast().getCompleteSql()));

		TableMetaData tableMetaData = new TableMetaData(getDataSource().getConnection(), "emp");
		String empnoColumnName = tableMetaData.getColumnMetaData("empno").getColumnName();
		String commColumnName = tableMetaData.getColumnMetaData("COMM").getColumnName();
		count = dao.update(emp);
		assertEquals(1, count);
		assertEqualsIgnoreCase(
				"UPDATE EMP SET EMPNO = 1 , ENAME = 'ename' , JOB = NULL , MGR = NULL , HIREDATE = NULL , SAL = NULL , COMM = 2 , DEPTNO = 10 , TSTAMP = NULL WHERE "
						+ empnoColumnName + " = 1 AND " + commColumnName + " = 1",
				dao.getSqlLogRegistry().getLast().getCompleteSql());
		count = dao.update(emp);
		assertEquals(1, count);
		assertEqualsIgnoreCase(
				"UPDATE EMP SET EMPNO = 1 , ENAME = 'ename' , JOB = NULL , MGR = NULL , HIREDATE = NULL , SAL = NULL , COMM = 3 , DEPTNO = 10 , TSTAMP = NULL WHERE "
						+ empnoColumnName + " = 1 AND " + commColumnName + " = 2",
				logRegistry.getLast().getCompleteSql());
		String sql = logRegistry.getLast().getCompleteSql();
		assertMatches("UPDATE .* SET.*COMM = 3.*WHERE.*COMM = 2.*", sql);
	}

	@Test
	public void testInsertCollectionOfENTITY() {
		dao = newTestDao(getDataSource());

		List<Emp> entityList = new ArrayList<Emp>();
		Emp emp1 = new Emp();
		emp1.setEMPNO("1");
		emp1.setCOMM("2");
		emp1.setDEPTNO("10");
		emp1.setENAME("ename");
		entityList.add(emp1);

		Emp emp2 = new Emp();
		emp2.setEMPNO("2");
		emp2.setCOMM("2");
		emp2.setDEPTNO("10");
		emp2.setENAME("ename");
		entityList.add(emp2);

		int[] counts = dao.insert(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);

		int count = dao.update(emp1);
		assertEquals(1, count);

		count = dao.update(emp2);
		assertEquals(1, count);
	}

	@Test
	public void testUpdateCollectionOfENTITY() {
		dao = newTestDao(getDataSource());

		List<Emp> entityList = new ArrayList<Emp>();
		Emp emp1 = new Emp();
		emp1.setEMPNO("1");
		emp1.setCOMM("2");
		emp1.setDEPTNO("10");
		emp1.setENAME("ename");
		entityList.add(emp1);

		Emp emp2 = new Emp();
		emp2.setEMPNO("2");
		emp2.setCOMM("2");
		emp2.setDEPTNO("10");
		emp2.setENAME("ename");
		entityList.add(emp2);

		int[] counts = dao.insert(entityList);

		counts = dao.update(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(2, counts.length);
	}

	@Test
	public void testUpdateCollectionOfENTITYHasVersionNo() {
		dao = newTestDao(getDataSource());

		List<EmpHasVersionNoAnnotation> entityList = new ArrayList<EmpHasVersionNoAnnotation>();
		EmpHasVersionNoAnnotation emp1 = new EmpHasVersionNoAnnotation();
		emp1.setEMPNO("1");
		emp1.setDEPTNO("10");
		emp1.setENAME("ename");
		entityList.add(emp1);

		EmpHasVersionNoAnnotation emp2 = new EmpHasVersionNoAnnotation();
		emp2.setEMPNO("2");
		emp2.setDEPTNO("10");
		emp2.setENAME("ename");
		entityList.add(emp2);

		int[] counts = dao.insert(entityList);

		counts = dao.update(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(2, counts.length);

		String sql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertMatches("UPDATE .* SET.*COMM = 2.*WHERE.*COMM = 1.*", sql);

		counts = dao.update(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(2, counts.length);

		sql = dao.getSqlLogRegistry().getLast().getCompleteSql();
		assertMatches("UPDATE .* SET.*COMM = 3.*WHERE.*COMM = 2.*", sql);
	}

	@Test
	public void testDeleteCollectionOfENTITY() {
		dao = newTestDao(getDataSource());

		List<Emp> entityList = new ArrayList<Emp>();
		Emp emp1 = new Emp();
		emp1.setEMPNO("1");
		emp1.setCOMM("2");
		emp1.setDEPTNO("10");
		emp1.setENAME("ename");
		entityList.add(emp1);

		Emp emp2 = new Emp();
		emp2.setEMPNO("2");
		emp2.setCOMM("2");
		emp2.setDEPTNO("10");
		emp2.setENAME("ename");
		entityList.add(emp2);

		int[] counts = dao.insert(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);

		counts = dao.delete(entityList);
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(2, counts.length);
	}

	@Test
	public void testExecuteUpdate() {
		dao = newTestDao(getDataSource());
		String sql = "UPDATE EMP SET ENAME = /*name*/'SMITH' WHERE EMPNO = /*no*/1";

		dao.executeUpdate(sql, args("name", "foo", "no", 7369));
		Emp emp = dao.selectOne("select * from EMP where EMPNO = 7369", Emp.class).get();
		assertEquals("foo", emp.getENAME());
	}

	@Test
	public void testExecuteBatch() {
		dao = newTestDao(getDataSource());
		String sql = "UPDATE EMP SET ENAME = /*name*/'SMITH' WHERE EMPNO = /*no*/1";

		List<Object> list = list(args("name", "foo", "no", 7369), args("name", "bar", "no", 7499), args("name", "baz", "no", -1));
		int[] counts = dao.executeBatch(sql, list);

		Emp emp = dao.selectOne("select * from EMP where EMPNO = 7369", Emp.class).get();
		assertEquals(1, counts[0]);
		assertEquals("foo", emp.getENAME());
		emp = dao.selectOne("select * from EMP where EMPNO = 7499", Emp.class).get();
		assertEquals(1, counts[1]);
		assertEquals("bar", emp.getENAME());
		assertEquals(0, counts[2]);
	}

	@Test
	public void testSelect() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		Emp emp = new Emp();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setEMPNO("1");
		emp.setENAME("ename");
		int count = dao.insert(emp);
		assertEquals(1, count);

		Emp selectEmp = dao.selectOne("SELECT * FROM EMP WHERE EMPNO = /*no*/0", args("no", 1), Emp.class).get();
		assertEquals("1", selectEmp.getEMPNO());
		assertEquals("ename", selectEmp.getENAME());
		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO = 1", completeSql);

		selectEmp = dao.selectOne("SELECT * FROM EMP WHERE EMPNO = 1", Emp.class).get();
		assertEquals("1", selectEmp.getEMPNO());
		assertEquals("ename", selectEmp.getENAME());
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO = 1", completeSql);

		Map<String, Object> empMap = dao.selectOneMap("SELECT * FROM EMP WHERE EMPNO = 1").get();
		assertEquals("1", empMap.get("EMPNO").toString());
		assertEquals("1", empMap.get("empNo").toString());
		assertEquals("1", empMap.get("EmPnO").toString());
		assertEquals("1", empMap.get("eMpNo").toString());
		assertEquals("ename", empMap.get("ENAME"));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO = 1", completeSql);

		List<Emp> result = null;
		result = dao.select("SELECT * FROM EMP WHERE EMPNO = /*no*/0 AND ENAME = /*en*/'hoge'", args("en", "hoge", "no", 1), Emp.class);
		assertEquals(0, result.size());
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO = 1 AND ENAME = 'hoge'", completeSql);

		result = dao.select("jp/dodododo/dao/impl/if.sql", args("no", 1), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertSql(getText("if_result_1.sql"), completeSql);

		result = dao.select("jp/dodododo/dao/impl/if.sql", args("no", null), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertSql(getText("if_result_2.sql"), completeSql);

		result = dao.select("jp/dodododo/dao/impl/if_else.sql", args("no", null), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertSql(getText("if_else_result_1.sql"), completeSql);

		Map<String, Object> selectCount = dao.selectOneMap("SELECT COUNT(*) AS CNT FROM EMP").get();
		assertEquals(15, ((Number) selectCount.get("cnt")).intValue());

		BigDecimal decimal = dao.selectOneNumber("SELECT COUNT(*) AS CNT FROM EMP").get();
		assertEquals(15, decimal.intValue());
	}

	@Test
	public void testSelectMap() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		Emp emp = new Emp();
		emp.setCOMM("2");
		emp.setDEPTNO("10");
		emp.setEMPNO("1");
		emp.setENAME("ename");
		int count = dao.insert(emp);
		assertEquals(1, count);
		String sql = "SELECT * FROM EMP WHERE EMPNO = /*no*/0";
		List<Map<String, Object>> selectMap = dao.selectMap(sql, args("no", 1));
		Map<String, Object> selectEmp = selectMap.get(0);
		assertEquals("1", selectEmp.get("EMPNO").toString());
		assertEquals("ename", selectEmp.get("ENAME"));
		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO = 1", completeSql);
	}

	@Test
	public void testExistsRecord() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		boolean existsRecord = dao.existsRecord(from("emp"), where("empno", ge(0)));
		String sql = logRegistry.getLast().getCompleteSql();
		assertTrue(StringUtil.equalsIgnoreCase("SELECT * FROM EMP WHERE EMPNO >= 0", sql));
		assertTrue(existsRecord);
	}

	private void assertSql(String exp, String act) {
		// assertEquals(exp.replaceAll("\r", "\n").replaceAll("\n\n", "\n"),
		// act);
		assertEqualsIgnoreCase(exp.replaceAll("\r", "\n").replaceAll("\n\n", "\n").trim(), act.replaceAll("\r", "\n").replaceAll("\n\n", "\n").trim());
	}

	private String getText(String fileName) {
		try (InputStream stream = RdbDaoTest.class.getClassLoader().getResourceAsStream("jp/dodododo/dao/impl/" + fileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			StringBuffer ret = new StringBuffer(128);
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) >= 0) {
				ret.append(buf, 0, n);
			}
			return ret.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	public void testSelectManyToOne() {
		dao = newTestDao(getDataSource());

		List<EmpHasDept> empList = dao.select("jp/dodododo/dao/impl/many_to_one.sql", EmpHasDept.class);
		assertEquals(14, empList.size());
		assertEquals("RESEARCH", empList.get(0).getDept().getDNAME());
		for (EmpHasDept empHasDept : empList) {
			assertNotNull(empHasDept.getDept().getDNAME());
		}
	}

	@Test
	public void testSelectOneToMany() {
		dao = newTestDao(getDataSource());

		String sql = "jp/dodododo/dao/impl/one_to_many.sql";
		List<DeptHasEmpList> deptList = dao.select(sql, DeptHasEmpList.class);
		assertEquals(3, deptList.size());

		assertEquals(3, deptList.get(0).getEmpList().size());
		assertEquals(5, deptList.get(1).getEmpList().size());
		assertEquals(6, deptList.get(2).getEmpList().size());

		assertEquals("MILLER", deptList.get(0).getEmpList().get(0).getENAME());
		assertEquals("KING", deptList.get(0).getEmpList().get(1).getENAME());
		assertEquals("CLARK", deptList.get(0).getEmpList().get(2).getENAME());
	}

	@Test
	public void testNoParameterizedList() {
		dao = newTestDao(getDataSource());

		try {
			dao.select("jp/dodododo/dao/impl/one_to_many.sql", DeptHasNoParameterizedList.class);
			fail();
		} catch (NoParameterizedException success) {
		}
	}

	@Test
	public void testRelationArray() {
		dao = newTestDao(getDataSource());

		List<DeptHasEnameArray> list = dao.select("jp/dodododo/dao/impl/one_to_many.sql", DeptHasEnameArray.class);
		assertEquals(3, list.size());

		DeptHasEnameArray dept = list.get(0);
		String[] ename = dept.getEname();
		assertEquals(3, ename.length);
		assertEquals("MILLER", ename[0]);
		assertEquals("KING", ename[1]);
		assertEquals("CLARK", ename[2]);

		List<Emp> empList = dept.getEmpList();
		assertEquals(3, empList.size());
		assertEquals("MILLER", empList.get(0).getENAME());
		assertEquals("KING", empList.get(1).getENAME());
		assertEquals("CLARK", empList.get(2).getENAME());
	}

	@Test
	public void testRelationArray2() {
		dao = newTestDao(getDataSource());

		List<DeptHasEmpArray> list = dao.select("jp/dodododo/dao/impl/one_to_many.sql", DeptHasEmpArray.class);
		assertEquals(3, list.size());

		DeptHasEmpArray dept = list.get(0);

		Emp[] emps = dept.getEmps();
		assertEquals(3, emps.length);
		assertEquals("MILLER", emps[0].getENAME());
		assertEquals("KING", emps[1].getENAME());
		assertEquals("CLARK", emps[2].getENAME());
	}

	@Test
	public void testMixRelation() {
		dao = newTestDao(getDataSource());

		List<DeptHasEmpList2> deptList2 = dao.select("jp/dodododo/dao/impl/one_to_many.sql", DeptHasEmpList2.class);
		assertEquals(3, deptList2.size());

		assertEquals(3, deptList2.get(0).getEmpList().size());
		assertNotNull(deptList2.get(0).getDept());
		assertEquals("ACCOUNTING", deptList2.get(0).getDNAME());
		assertEquals("ACCOUNTING", deptList2.get(0).getDept().getDNAME());
		assertEquals(5, deptList2.get(1).getEmpList().size());
		assertNotNull(deptList2.get(1).getDept());
		assertEquals(6, deptList2.get(2).getEmpList().size());
		assertNotNull(deptList2.get(2).getDept());
	}

	@Test
	public void testMixRelation2() {
		dao = newTestDao(getDataSource());

		List<DeptHasManager> deptList = dao.select("jp/dodododo/dao/impl/dept_manager_employees.sql", DeptHasManager.class);
		assertEquals(3, deptList.size());

		DeptHasManager dept0 = deptList.get(0);
		assertEquals("ACCOUNTING", dept0.getDNAME());
		assertEquals(2, dept0.getManager().size());

		assertEquals(1, dept0.getManager().get(0).getEmpList().size());
		assertEquals(3, dept0.getManager().get(1).getEmpList().size());

		DeptHasManager dept1 = deptList.get(1);
		assertEquals("RESEARCH", dept1.getDNAME());
		assertEquals(3, dept1.getManager().size());

		assertEquals(2, dept1.getManager().get(0).getEmpList().size());
		assertEquals(1, dept1.getManager().get(1).getEmpList().size());
		assertEquals(1, dept1.getManager().get(2).getEmpList().size());

		DeptHasManager dept2 = deptList.get(2);
		assertEquals("SALES", dept2.getDNAME());
		assertEquals(1, dept2.getManager().size());

		assertEquals(5, dept2.getManager().get(0).getEmpList().size());

	}

	@Test
	public void testMixRelation3() {
		dao = newTestDao(getDataSource());

		List<DeptHasManager> deptList = dao.select("jp/dodododo/dao/impl/dept_manager_employees2.sql", DeptHasManager.class);
		assertEquals(3, deptList.size());

		DeptHasManager dept0 = deptList.get(0);
		assertEquals("ACCOUNTING", dept0.getDNAME());
		assertEquals(2, dept0.getManager().size());

		assertEquals(3, dept0.getManager().get(0).getEmpList().size());
		assertEquals(1, dept0.getManager().get(1).getEmpList().size());

		DeptHasManager dept1 = deptList.get(1);
		assertEquals("RESEARCH", dept1.getDNAME());
		assertEquals(4, dept1.getManager().size());

		assertEquals(1, dept1.getManager().get(0).getEmpList().size());
		assertEquals(1, dept1.getManager().get(1).getEmpList().size());
		assertEquals(1, dept1.getManager().get(2).getEmpList().size());
		assertEquals(1, dept1.getManager().get(3).getEmpList().size());

		DeptHasManager dept2 = deptList.get(2);
		assertEquals("SALES", dept2.getDNAME());
		assertEquals(1, dept2.getManager().size());

		assertEquals(5, dept2.getManager().get(0).getEmpList().size());

	}

	@Test
	public void testEmbeddedValue() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof HSQL == false) {
			return;
		}
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		String sql = "select /*$bean.sysDate*/SYSDATE from /*$bean.dual*/DUAL ";
		String actualSql = "select CURRENT_TIMESTAMP from INFORMATION_SCHEMA.SYSTEM_TABLES WHERE table_name = 'SYSTEM_TABLES'";

		@SuppressWarnings("unused")
		List<Map<String, Object>> result = dao.selectMap(sql, args("bean", new Embedded()));
		assertEquals(actualSql, logRegistry.getLast().getRawSql());

		result = dao.selectMap(sql, args("bean", new Embedded2()));
		assertEquals(actualSql, logRegistry.getLast().getRawSql());

	}

	public static class Embedded {
		private String sysDate = "CURRENT_TIMESTAMP";

		private String dual = "INFORMATION_SCHEMA.SYSTEM_TABLES WHERE table_name = 'SYSTEM_TABLES'";

		public String getDual() {
			return dual;
		}

		public String getSysDate() {
			return sysDate;
		}
	}

	// "public field" & "@Dialects Annotation"
	public static class Embedded2 {
		@Dialects(dialect = { HSQL.class, Oracle.class }, value = { "CURRENT_TIMESTAMP", "SYSDATE" })
		public String sysDate;

		@Dialects(
		//
		dialect = { HSQL.class, Oracle.class },
		//
		value = { "INFORMATION_SCHEMA.SYSTEM_TABLES WHERE table_name = 'SYSTEM_TABLES'", "DUAL" })
		public String dual;

	}

	@Test
	public void testSelectOrderBy() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		@SuppressWarnings("unused")
		List<Emp> empList = dao.select("SELECT * FROM EMP /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY EMPNO /*END*/",
				orderBy("ENAME", ASC, "EMPNO", DESC), Emp.class);
		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP ORDER BY ENAME ASC, EMPNO DESC", completeSql);

		empList = dao.select("SELECT * FROM EMP /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY EMPNO /*END*/", orderBy(), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP", completeSql.trim());

		empList = dao.select(BY, query(table("EMP"), orderBy("EMPNO", ASC)), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertTrue(StringUtil.equalsIgnoreCase("SELECT * FROM EMP ORDER BY EMPNO ASC", completeSql.trim()));

		empList = dao.select(BY, query(table("EMP"), "ENAME", "mike", orderBy("EMPNO", ASC)), Emp.class);
		completeSql = logRegistry.getLast().getCompleteSql();
		assertTrue(StringUtil.equalsIgnoreCase("SELECT * FROM EMP WHERE ENAME = 'mike' ORDER BY EMPNO ASC", completeSql.trim()));
	}

	@Test
	public void testSelectEnum() throws Exception {
		dao = newTestDao(getDataSource());

		EmpHasEnumDept emp = dao.selectOne("SELECT EMPNO, ENAME, DEPTNO, DEPTNO AS DEPT FROM EMP WHERE EMPNO=7369", EmpHasEnumDept.class).get();
		assertEquals("7369", emp.getEMPNO());
		assertEquals("SMITH", emp.getENAME());
		assertEquals(EnumDept.RESEARCH, emp.getDept());
		assertEquals("20", emp.getDEPTNO());
	}

	@Test
	public void testNestIfSql() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		@SuppressWarnings("unused")
		List<Map<String, Object>> result = dao.selectMap("jp/dodododo/dao/impl/nest_if.sql");

		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP", completeSql.trim());
	}

	@Test
	public void testNestIfSql2() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		@SuppressWarnings("unused")
		List<Map<String, Object>> result = dao.selectMap("jp/dodododo/dao/impl/nest_if2.sql");

		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP", completeSql.trim());
	}

	@Test
	public void testNestIfSql3() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		@SuppressWarnings("unused")
		List<Map<String, Object>> result = dao.selectMap("jp/dodododo/dao/impl/nest_if3.sql");

		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP\nwhere\nEMPNO = EMPNO", completeSql.trim());
	}

	@Test
	public void testSelectPaging() {
		dao = newTestDao(getDataSource());

		String sql = "select * from EMP order by empno";
		List<Emp> allList = dao.select(sql, Emp.class);
		List<Emp> list0 = dao.select(sql, args(new Paging(2, 0)), Emp.class);
		List<Emp> list1 = dao.select(sql, args(new Paging(2, 1)), Emp.class);

		assertEquals(allList.get(0).getEMPNO(), list0.get(0).getEMPNO());
		assertEquals(allList.get(1).getEMPNO(), list0.get(1).getEMPNO());
		assertEquals(allList.get(2).getEMPNO(), list1.get(0).getEMPNO());
		assertEquals(allList.get(3).getEMPNO(), list1.get(1).getEMPNO());

		list0 = dao.select(sql, args(/* dummy */"a", "", new LimitOffset(2, 0)), Emp.class);
		list1 = dao.select(sql, args(/* dummy */"b", "", new LimitOffset(2, 2)), Emp.class);
		assertEquals(allList.get(0).getEMPNO(), list0.get(0).getEMPNO());
		assertEquals(allList.get(1).getEMPNO(), list0.get(1).getEMPNO());
		assertEquals(allList.get(2).getEMPNO(), list1.get(0).getEMPNO());
		assertEquals(allList.get(3).getEMPNO(), list1.get(1).getEMPNO());
	}

	@Test
	public void testIterationCallback1() throws Exception {
		dao = newTestDao(getDataSource());

		final AtomicInteger count = new AtomicInteger(0);
		String sql = "select * from EMP order by empno";
		assertEquals(0, count.intValue());

		dao.select(sql, Emp.class, new Each<Emp>() {
			@Override
			public void each(Emp it) {
				count.set(count.intValue() + 1);
				logger.debug("" + it);
			}
		});
		assertEquals(14, count.intValue());
	}

	@SuppressWarnings("unused")
	@Test
	public void testIterationCallback2() throws Exception {
		dao = newTestDao(getDataSource());

		final AtomicInteger count = new AtomicInteger(0);
		String sql = "select * from EMP order by empno";
		assertEquals(0, count.intValue());

		dao.select(sql, Emp.class, new jp.dodododo.dao.script.Each() {
			public void each(Emp it) {
				count.set(count.intValue() + 1);
				logger.debug("" + it);
			}
		});
		assertEquals(14, count.intValue());
	}

	@Test
	public void testIn() throws Exception {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		List<String> empnoList = new ArrayList<String>();
		empnoList.add("1");
		empnoList.add("2");
		@SuppressWarnings("unused")
		Map<String, Object> selectOneMap = dao.selectOneMap("SELECT * FROM EMP WHERE EMPNO IN /*empno*/'0'",
				args("empno", empnoList)).orElse(new HashMap<>());
		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEquals("SELECT * FROM EMP WHERE EMPNO IN ('1', '2')", completeSql);
	}

	@Test
	public void testSimpleSql() throws Exception {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		logRegistry.clear();

		Emp query = new Emp();
		query.setEMPNO("1");
		dao.select(ALL, query);
		String completeSql = logRegistry.get(0).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP", completeSql);
		assertTrue(StringUtil.equalsIgnoreCase("SELECT * FROM EMP", completeSql));

		dao.select(SIMPLE_WHERE, query);
		completeSql = logRegistry.get(1).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.select(COUNT_ALL, query);
		completeSql = logRegistry.get(2).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP", completeSql);

		dao.select(SIMPLE_COUNT_WHERE, query);
		completeSql = logRegistry.get(3).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectOne(SIMPLE_COUNT_WHERE, query);
		completeSql = logRegistry.get(4).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectOneNumber(COUNT_ALL, query);
		completeSql = logRegistry.get(5).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP", completeSql);

		dao.select(BY, query);
		completeSql = logRegistry.get(6).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.select(BY, query("tableName", "EMP", "EMPNO", "1"));
		completeSql = logRegistry.get(7).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);
	}

	@Test
	public void testSimpleSqlMap() throws Exception {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("table_Name", "emp");
		query.put("EMPNO", "1");
		dao.selectMap(ALL, query);
		String completeSql = logRegistry.get(0).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP", completeSql);

		dao.selectMap(SIMPLE_WHERE, query);
		completeSql = logRegistry.get(1).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectMap(COUNT_ALL, query);
		completeSql = logRegistry.get(2).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP", completeSql);

		dao.selectMap(SIMPLE_COUNT_WHERE, query);
		completeSql = logRegistry.get(3).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectOneMap(SIMPLE_COUNT_WHERE, query);
		completeSql = logRegistry.get(4).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectOneNumber(COUNT_ALL, query);
		completeSql = logRegistry.get(5).getCompleteSql();
		assertEqualsIgnoreCase("SELECT count(*) FROM EMP", completeSql);

		dao.selectMap(from("emp"), by("empno", eq("1")));
		completeSql = dao.getSqlLogRegistry().get(1).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);

		dao.selectOneMap(from("emp"), by("empno", eq("1")));
		completeSql = dao.getSqlLogRegistry().get(1).getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = '1'", completeSql);
	}

	@Test
	public void testOperator() {
		dao = newTestDao(getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		List<Row> select = dao.select(Row.class, from("emp"), where("empno", eq(1)));
		String completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = 1", completeSql);
		assertEquals(0, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", ne(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO <> 1", completeSql);
		assertEquals(14, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", lt(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO < 1", completeSql);
		assertEquals(0, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", le(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO <= 1", completeSql);
		assertEquals(0, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", gt(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO > 1", completeSql);
		assertEquals(14, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", ge(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO >= 1", completeSql);
		assertEquals(14, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", in(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO IN (1)", completeSql);
		assertEquals(0, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", in(1, 7369)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO IN (1, 7369)", completeSql);
		assertEquals(1, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", notIn(1)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO NOT IN (1)", completeSql);
		assertEquals(14, select.size());

		select = dao.select(Row.class, from("emp"), where("empno", notIn(1, 7369)));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO NOT IN (1, 7369)", completeSql);
		assertEquals(13, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", like("I")));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME LIKE '%I%'", completeSql);
		assertEquals(4, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", notLike("I")));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME NOT LIKE '%I%'", completeSql);
		assertEquals(10, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", contains("I")));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME LIKE '%I%'", completeSql);
		assertEquals(4, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", notContains("I")));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME NOT LIKE '%I%'", completeSql);
		assertEquals(10, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", isNull()));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME IS NULL", completeSql);
		assertEquals(0, select.size());

		select = dao.select(Row.class, from("emp"), where("ename", isNotNull()));
		completeSql = logRegistry.getLast().getCompleteSql();
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE ENAME IS NOT NULL", completeSql);
		assertEquals(14, select.size());
	}

	public void _testPerformance() throws Exception {
		dao = newTestDao(getDataSource());

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			EmpHasIdAnnotation emp = new EmpHasIdAnnotation();
			emp.setCOMM("2");
			emp.setDEPTNO("1");
			emp.setENAME("ename");
			int count = dao.insert(emp);
			if (count != 1) {
				fail();
			}
		}
		long end = System.currentTimeMillis();
		logger.debug("" + (end - start));
	}

	private static final Logger logger = LoggerFactory.getLogger(RdbDaoTest.class);

	@Test
	public void testGatherValue() throws Exception {
		TestBean bean = new TestBean();
		bean.bean = new TestBean();

		String tableName = "test_table";
		String columnName = "test_column";
		List<CandidateValue> values = new ArrayList<CandidateValue>();
		new RdbDao().gatherValue(new Object[] { bean }, tableName, columnName, values);
		assertEquals(1, values.size());
		assertEquals("val", values.get(0).value.getValue());
		assertTrue(values.get(0).matchTableName);

		tableName = "test_table2";
		columnName = "test_column2";
		values = new ArrayList<CandidateValue>();
		new RdbDao().gatherValue(new Object[] { bean }, tableName, columnName, values);
		assertEquals(1, values.size());
		assertEquals("val2", values.get(0).value.getValue());
		assertTrue(values.get(0).matchTableName);

		tableName = "test_table3";
		columnName = "test_column3";
		values = new ArrayList<CandidateValue>();
		new RdbDao(getDataSource()).gatherValue(new Object[] { bean }, tableName, columnName, values);
		assertEquals(5, values.size());
		assertEquals("val3", values.get(0).value.getValue());
		assertTrue(values.get(0).matchTableName);
}

	@Test
	public void testGatherValueNestedBeanIsNull() throws Exception {
		TestBean bean = new TestBean();
		bean.bean = null;

		String tableName = "test_table";
		String columnName = "test_column";
		List<CandidateValue> values = new ArrayList<>();
		new RdbDao().gatherValue(new Object[] { bean }, tableName, columnName, values);
		assertEquals(0, values.size());
	}

	public static class TestBean {
		@Relations({
		//
				@Rel(table = "test_table", column = "test_column", property = "value"), //
				@Rel(table = "test_table2", column = "test_column2", property = "value2"),//
				@Rel(table = "test_table3", column = "test_column3", property = "value3") //
		})
		private TestBean bean;

		public String getValue() {
			return "val";
		}

		public String getValue2() {
			return "val2";
		}

		@Column("test_column3")
		public String getValue3() {
			return "val3";
		}

		TestBean2 bean2 = new TestBean2(this);
		public TestBean2 getTestBean2() {
			return bean2;
		}
	}

	public static class TestBean2 {

		TestBean testBean;
		public TestBean2( TestBean testBean) {
			this.testBean = testBean;
		}
		@Columns(@Column("test_column3"))
		public String getValue() {
			return "@Columns getValue";
		}

		public String getTestColumn3() {
			return "getTestColumn3";
		}

		@Column("test_column3")
		public String getValue3() {
			return "@Column getValue3";
		}

		public TestBean getTestBean() {
			return testBean;
		}
	}

	@Test
	public void testDeleteFromTowPk() throws Exception {
		Dao dao = newTestDao(getConnection());
		TwoPkTable twoPk = new TwoPkTable();
		twoPk.PK1 = 1;
		twoPk.PK2 = 2;
		dao.insert(twoPk);
		dao.update(twoPk);
		dao.delete(twoPk);
	}

	public static class TwoPkTable {
		public int PK1;
		public int PK2;
	}

	@Test
	public void testGatherValue2() throws Exception {
		C c = new C();
		List<CandidateValue> values = new ArrayList<CandidateValue>();
		new _Dao(getConnection()).gatherValue(new Object[] { c }, "B", "a", values);
		CandidateValue value = CandidateValue.getValue(values, null, null);
		assertEquals(0, value.value.getValue());
	}

	@Test
	public void testLambda() throws Exception {
		dao = newTestDao(getConnection());

		Long sum1 = dao.selectOne("select sum(sal) from EMP", Long.class).get();
		AtomicLong sum2 = new AtomicLong(0);
		AtomicLong sum3 = new AtomicLong(0);
		dao.select("select *   from EMP", Row.class, System.out::println);
		dao.select("select *   from EMP", Row.class, row -> { sum2.addAndGet(row.getLong("sal")); });
		dao.select("select sal from EMP", Long.class, sal -> { sum3.addAndGet(sal); });
		assertEquals(sum1.longValue(), sum2.longValue());
		assertEquals(sum1.longValue(), sum3.longValue());
	}

	public class _Dao extends RdbDao {
		public _Dao() {
			super();
		}

		public _Dao(Connection connection) {
			super(connection);
		}

		@Override
		public void gatherValue(Object entity, String tableName, String columnName, List<CandidateValue> values, StringBuilder path, Map<Integer, Object> processedObjects) {
			super.gatherValue(entity, tableName, columnName, values, path, processedObjects);
		}

	}

	public static class A {
		@Column("a")
		public int getB() {
			return 0;
		}
	}

	public static class B extends A {
		public String getA() {
			return "B";
		}
	}

	public static class C extends B {
		@Override
		public String getA() {
			return "C";
		}
	}

	@Table("EMP")
	public static class GatherTestBean {
		private static final Logger logger = LoggerFactory.getLogger(GatherTestBean.class);

		public String EMPNO = "1";

		public Object getA() {
			logger.debug("getA");
			return new Object() {
				@Override
				public String toString() {
					return "A";
				}
			};
		}

		public Object getB() {
			logger.debug("getB");
			return new Object() {
				@Override
				public String toString() {
					return "B";
				}
			};
		}

		public Object getC() {
			logger.debug("getC");
			return new GatherTestBean2();
		}

		public String getEname() {
			logger.debug("getEname");
			return "ename";
		}

		@Column("ename")
		public String getEname2() {
			logger.debug("getEname2");
			return "ename2";
		}
	}

	public static class GatherTestBean2 {
		private static final Logger logger = LoggerFactory.getLogger(GatherTestBean.class);

		public Object getD() {
			logger.debug("getD");
			return new Object() {
				@Override
				public String toString() {
					return "D";
				}
			};
		}

		public Object getE() {
			logger.debug("getE");
			return null;
		}
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}

	private Connection getConnection() throws SQLException {
		return dbTestExtension.getConnection();
	}
}
