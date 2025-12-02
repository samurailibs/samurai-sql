package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HasListOfIntPropertyTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestDao(getDataSource());

		List<Dept> deptList = dao.select("SELECT DEPT.DEPTNO as DEPTNO, DNAME, EMPNO FROM DEPT, EMP where DEPT.DEPTNO = EMP.DEPTNO AND DEPT.DEPTNO = 10", Dept.class);

		assertEquals(1, deptList.size());
		assertEquals(3, deptList.get(0).empNo.size());
	}

	public static class Dept {
		public String DEPTNO;

		public String DNAME;

		public List<Integer> empNo;
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
