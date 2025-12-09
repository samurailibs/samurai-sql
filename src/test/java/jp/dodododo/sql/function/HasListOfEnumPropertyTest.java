package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.NumKey;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HasListOfEnumPropertyTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestClient(getDataSource());

		List<Dept> deptList = dao
				.select("SELECT DEPT.DEPTNO as DEPTNO, EMPNO FROM DEPT, EMP where DEPT.DEPTNO = EMP.DEPTNO AND DEPT.DEPTNO = 10",
						Dept.class);

		assertEquals(1, deptList.size());
		assertEquals(3, deptList.get(0).empNo.size());
	}

	public static class Dept {
		public String DEPTNO;

		public String DNAME;

		public List<EmpNo> empNo;
	}

	public static enum EmpNo {
		Emp7782(7782), Emp7839(7839), Emp7934(7934);

		private int no;

		private EmpNo(int no) {
			this.no = no;
		}

		@NumKey
		public int getNo() {
			return no;
		}

	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
