package jp.dodododo.dao.function;

import static jp.dodododo.dao.columns.ColumnsUtil.*;
import static jp.dodododo.dao.unit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.impl.RdbDao;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NoPersistentColumnInsertTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private RdbDao dao;

	@Test
	public void testInsertAndSelect() {
		dao = new RdbDao(dbTestExtension.getDataSource());

		int count = dao.insert("emp", Emp.EMP, npc("ename", "COMM", "deptNo", "HIREDATE", "MGR", "SAL", "TSTAMP", "JOB"));
		assertEquals(1, count);
		assertEqualsIgnoreCase("INSERT INTO emp ( EMPNO ) VALUES ( 1 )", dao.getSqlLogRegistry().getLast().getCompleteSql());

		try {
			count = dao.insert("emp", Emp.EMP);
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
