package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.NumKey;
import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EnumInsertTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestDao(getDataSource());

		int count = dao.insert("emp", Emp.EMP);
		assertEquals(1, count);

		try {
			count = dao.insert("emp", Emp.EMP);
			fail();
		} catch (SQLRuntimeException success) {
		}

		List<Emp> result = dao.select("SELECT empno FROM EMP where empno=1", Emp.class);
		assertEquals(result.get(0).getClass(), Emp.class);
		assertEquals(result.get(0).getId(), Emp.EMP.getId());
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

		@NumKey
		public int getId() {
			return Integer.parseInt(EMPNO);
		}
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
