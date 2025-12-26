package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.List;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue35Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@BeforeEach
	public void setUp() throws Exception {
		SqlConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws ParseException {
		dao = newTestClient(dbTestExtension.getDataSource());

		Emp emp = dao.selectOne("select * from EMP where empno = 7369", Emp.class).get();
		assertNotNull(emp.dept);
		assertNotNull(emp.dept2);
		assertNotNull(emp.EMPNO);
		assertNotNull(emp.ENAME);

		emp = dao.selectOne("select empno, ename, 0 as DEPTNO from EMP where empno = 7369", Emp.class).get();
		assertNotNull(emp.dept);
		assertNotNull(emp.dept2);

		emp = dao.selectOne("select empno, ename, null as DEPTNO from EMP where empno = 7369", Emp.class).get();
		assertNull(emp.dept);
		assertNotNull(emp.EMPNO);
		assertNotNull(emp.ENAME);

		assertNotNull(emp.dept2);
		assertEquals(0, emp.dept2.DEPTNO);
		assertNull(emp.dept2.DNAME);

	}

	public static class Emp {
		public String EMPNO;

		public String ENAME;
		protected Dept dept;
		protected Dept dept2;
		protected Dept dept3;
		protected Dept dept4;

		public Emp(@Bean(nullable = true) Dept dept, Dept dept2, @Bean(nullable = true) Dept dept3, Dept dept4) {
			this.dept = dept;
			this.dept2 = dept2;
			this.dept3 = dept3;
			this.dept4 = dept4;
		}
	}

	public static class Dept {
		protected int DEPTNO = -1;

		protected String DNAME = "-";

		public Dept(@Column("DEPTNO") int DEPTNO, @Column("DNAME") String DNAME) {
			this.DEPTNO = DEPTNO;
			this.DNAME = DNAME;
		}
	}

	public static class Dept2 {
		protected Integer DEPTNO = -1;

		protected String DNAME = "-";

		public Dept2(@Column("DEPTNO") Integer DEPTNO, @Column("DNAME") String DNAME) {
			this.DEPTNO = DEPTNO;
			this.DNAME = DNAME;
		}
	}

	@Test
	public void testPrimitiveArgs() {
		Dao dao = newTestClient(dbTestExtension.getConnection());
		List<PrimitiveArgsEmp> list = dao.select("select * from EMP where comm > 0", PrimitiveArgsEmp.class);
		assertTrue(list.isEmpty() == false);
		for (PrimitiveArgsEmp emp : list) {
			assertTrue(0 < emp.comm);
		}
		list = dao.select("select * from EMP where comm is null", PrimitiveArgsEmp.class);
		assertTrue(list.isEmpty() == false);
		for (PrimitiveArgsEmp emp : list) {
			assertTrue(0 == emp.comm);
		}
		List<ObjectArgsEmp> list2 = dao.select("select * from EMP where comm > 0", ObjectArgsEmp.class);
		assertTrue(list.isEmpty() == false);
		for (ObjectArgsEmp emp : list2) {
			assertTrue(0 < emp.comm);
		}
		list2 = dao.select("select * from EMP where comm is null", ObjectArgsEmp.class);
		assertTrue(list.isEmpty() == false);
		for (ObjectArgsEmp emp : list2) {
			assertNull(emp.comm);
		}
	}

	public static class PrimitiveArgsEmp {
		protected int empNo;
		protected String name;
		protected int comm;

		public PrimitiveArgsEmp(@Column("EMPNO") int empNo, @Column("ENAME") String name, @Column("COMM") int comm) {
			this.empNo = empNo;
			this.name = name;
			this.comm = comm;
		}
	}

	public static class ObjectArgsEmp {
		protected int empNo;
		protected String name;
		protected Integer comm;

		public ObjectArgsEmp(@Column("EMPNO") int empNo, @Column("ENAME") String name, @Column("COMM") Integer comm) {
			this.empNo = empNo;
			this.name = name;
			this.comm = comm;
		}
	}
}
