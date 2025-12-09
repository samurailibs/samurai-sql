package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.config.DaoConfig;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue38Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@BeforeEach
	public void setUp() throws Exception {
		DaoConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws ParseException {
		dao = newTestClient(dbTestExtension.getDataSource());

		List<Dept> deptList = dao.select(
				"select DEPT.deptno, DEPT.dname, EMP.empno, EMP.ename from DEPT , EMP where EMP.deptno = DEPT.deptno order by DEPT.deptno", Dept.class);

		assertEquals(3, deptList.size());
		assertEquals(3, deptList.get(0).empList.size());
		assertEquals(5, deptList.get(1).empList.size());
		assertEquals(6, deptList.get(2).empList.size());

	}

	public static class Emp {
		@Column(value = "no", alias = "empno")
		private String no;
		@Column(value = "name", alias = "ename")
		private String name;

		public Emp(@Column(value = "no", alias = "empno") String no, @Column(value = "name", alias = "ename") String name) {
			this.no = no;
			this.name = name;
		}

		public String getNo() {
			return no;
		}

		public String getName() {
			return name;
		}

	}

	public static class Dept {
		@Column(value = "no", alias = "deptno")
		private String no;
		@Column(value = "name", alias = "dname")
		private String name;

		private List<Emp> empList = new ArrayList<Emp>();

		public Dept(@Column(value = "no", alias = "deptno") String no, @Column(value = "name", alias = "dname") String name) {
			this.no = no;
			this.name = name;

		}

		public List<Emp> getEmpList() {
			return empList;
		}

		public void setEmpList(List<Emp> empList) {
			this.empList = empList;
		}
	}
}
