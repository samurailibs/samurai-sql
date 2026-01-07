package jp.dodododo.sql.function;

import static jp.dodododo.sql.columns.ColumnsUtil.*;
import static jp.dodododo.sql.unit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.impl.SamuraiSqlClientImpl;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NoPersistentColumnInsertTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClientImpl client;

	@Test
	public void testInsertAndSelect() {
		client = new SamuraiSqlClientImpl(dbTestExtension.getDataSource());

		int count = client.insert("emp", Emp.EMP, npc("ename", "COMM", "deptNo", "HIREDATE", "MGR", "SAL", "TSTAMP", "JOB"));
		assertEquals(1, count);
		assertEqualsIgnoreCase("INSERT INTO emp ( EMPNO ) VALUES ( 1 )", client.getSqlLogRegistry().getLast().getCompleteSql());

		try {
			count = client.insert("emp", Emp.EMP);
			fail();
		} catch (SQLRuntimeException success) {
		}
	}

	public static enum Emp {
		EMP;

		public String EMPNO = "1";

		public String NAME;

		public Date TSTAMP;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		public String DEPTNO;

		public char test;

	}
}
