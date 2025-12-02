package jp.dodododo.dao.issue;

import static jp.dodododo.dao.sql.GenericSql.*;
import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.row.Row;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue47Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() throws Exception {
		dao = newTestDao(dbTestExtension.getDataSource());

		List<Emp> empList = dao.select(ALL, Emp.class);
		for (Emp emp : empList) {
			assertNotNull(emp.empno);
			assertNotNull(emp.dept.deptno);
		}
	}

	public static class Emp {
		protected String empno;
		protected Dept dept;

		public Emp(@Column("empno") String empno, @Bean(Dept2.class) Dept dept) {
			this.empno = empno;
			this.dept = dept;
		}
	}

	public static class Dept {
		protected String deptno;
	}

	@Bean(createMethod = "@jp.dodododo.dao.issue.Issue47Test$Dept2@create(resultSetMap)")
	public static class Dept2 extends Dept {


		public Dept2() {

		}
		private Dept2(String deptno) {
			this.deptno = deptno;
		}

		public static Dept create(Map<String, Object> row) {
			return new Dept2(new Row(row).getString("deptno"));
		}
	}
}
