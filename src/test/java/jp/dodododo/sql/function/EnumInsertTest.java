package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.NumKey;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EnumInsertTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestClient(getDataSource());

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
