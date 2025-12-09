package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.impl.Dept;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CreateMethodTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testMethodCreateBean() {
		dao = newTestClient(getDataSource());

		List<Emp> select = dao.select("select EMPNO, DEPT.DEPTNO as DEPTNO from EMP, DEPT where EMP.deptno = DEPT.deptno ",
				Emp.class);

		for (Emp selectedEmp : select) {

			if (selectedEmp.EMPNO.equals("23")) {
				assertTrue(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
			} else {
				assertFalse(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
				assertFalse(selectedEmp instanceof Emp3, selectedEmp.getClass().toString());
				assertFalse(selectedEmp instanceof Emp2, selectedEmp.getClass().toString());
			}

			assertNotNull(selectedEmp.dept);
		}

	}

	@Test
	public void testMethodCreateConstructor() {
		dao = newTestClient(getDataSource());

		List<Emp2> select = dao.select("select EMPNO, DEPT.deptno as deptno from EMP, DEPT where EMP.deptno = DEPT.deptno ",
				Emp2.class);

		for (Emp selectedEmp : select) {

			if (selectedEmp.EMPNO.equals("23")) {
				assertTrue(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
			} else {
				assertFalse(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
				assertFalse(selectedEmp instanceof Emp3, selectedEmp.getClass().toString());
				assertTrue(selectedEmp instanceof Emp2, selectedEmp.getClass().toString());
			}

			assertNotNull(selectedEmp.dept);
		}

	}

	@Test
	public void testMethodCreateClass() {
		dao = newTestClient(getDataSource());

		List<Emp3> select = dao.select("select EMPNO, DEPT.deptno as deptno from EMP, DEPT where EMP.deptno = DEPT.deptno ",
				Emp3.class);

		for (Emp selectedEmp : select) {

			if (selectedEmp.EMPNO.equals("23")) {
				assertTrue(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
			} else {
				assertFalse(selectedEmp instanceof Smith, selectedEmp.getClass().toString());
				assertTrue(selectedEmp instanceof Emp3, selectedEmp.getClass().toString());
				assertTrue(selectedEmp instanceof Emp2, selectedEmp.getClass().toString());
			}

			assertNotNull(selectedEmp.dept);
		}
	}

	@Bean(createMethod = "@jp.dodododo.sql.function.CreateMethodTest@getConstructorForDao(resultSetMap)")
	public static class Emp2 extends Emp {
	}

	@Bean(createMethod = "@jp.dodododo.sql.function.CreateMethodTest@getBeanClassForDao(resultSetMap)")
	public static class Emp3 extends Emp2 {
	}

	@Bean(createMethod = "@jp.dodododo.sql.function.CreateMethodTest@createEmp(resultSetMap)")
	public static class Emp {
		@Id(value = @IdDefSet(strategy = Sequence.class, name = "sequence"), targetTables = { "emp" })
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

		private Dept dept;

		public Emp() {
		}

		public Dept getDept() {
			return dept;
		}

		public void setDept(Dept dept) {
			this.dept = dept;
		}
	}

	public static Emp createEmp(Map<String, Object> rsMap) {
		int empno = ((Number) rsMap.get("empno")).intValue();
		if (empno == 23) {
			return new Smith();
		}
		return new Emp();
	}

	public static Class<? extends Emp> getBeanClassForDao(Map<String, Object> rsMap) throws Exception {
		int empno = ((Number) rsMap.get("empno")).intValue();
		if (empno == 23) {
			return Smith.class;
		}
		return Emp3.class;
	}

	public static Constructor<? extends Emp> getConstructorForDao(Map<String, Object> rsMap) throws Exception {
		int empno = ((Number) rsMap.get("empno")).intValue();
		if (empno == 23) {
			return Smith.class.getConstructor();
		}
		return Emp2.class.getConstructor();
	}

	public static class Smith extends Emp3 {
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
