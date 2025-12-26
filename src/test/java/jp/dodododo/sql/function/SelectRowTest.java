package jp.dodododo.sql.function;

import static jp.dodododo.sql.sql.GenericSql.*;
import static jp.dodododo.sql.unit.Assert.*;
import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.row.Row;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SelectRowTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;


	@Test
	public void testInsertAndSelect() {
		dao = newTestClient(dbTestExtension.getDataSource());
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();
		Emp emp = new Emp();
		emp.COMM = "2";
		// emp.EMPNO = "1";
		emp.TSTAMP = null;
		emp.TSTAMP = new Date();
		emp.NAME = "ename";
		int count = dao.insert("EMP", emp);
		assertEquals(1, count);
		Integer empNo = Integer.parseInt(emp.EMPNO);

		List<Row> select = dao.select("SELECT * FROM EMP WHERE empno = " + empNo, Row.class);
		assertEqualsIgnoreCase("SELECT * FROM emp WHERE empno = "+ empNo, logRegistry.getLast().getCompleteSql());
		assertEquals(empNo, select.get(0).getInteger("EMPNO"));
		assertEquals("" + empNo, select.get(0).getString("EMPNO"));
		assertEquals(Integer.valueOf(2), select.get(0).getInteger("COMM"));
		assertEquals("ename", select.get(0).getString("ENAME"));
		assertNotNull(select.get(0).getString("TSTAMP"));

		select = dao.select(SIMPLE_WHERE, args(TABLE_NAME, "EMP", "EMPNO", empNo), Row.class);
		assertEqualsIgnoreCase("SELECT * FROM EMP WHERE EMPNO = "+ empNo, logRegistry.getLast().getCompleteSql());
		assertEquals(empNo, select.get(0).getInteger("EMPNO"));
		assertEquals("" + empNo, select.get(0).getString("EMPNO"));
		assertEquals(Integer.valueOf(2), select.get(0).getInteger("COMM"));
		assertEquals("ename", select.get(0).getString("ENAME"));
		assertNotNull(select.get(0).getString("TSTAMP"));
	}

	public static class Emp {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"), @IdDefSet(strategy = Identity.class, db = SQLite.class)}, targetTables = { "EMP" })
		public String EMPNO;

		@Column("ename")
		public String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;
	}

}
