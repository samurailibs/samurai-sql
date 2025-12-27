package jp.dodododo.sql.issue;

import static jp.dodododo.sql.sql.GenericSql.*;
import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.row.Row;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue47Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@Test
	public void test() throws Exception {
		client = newTestClient(dbTestExtension.getDataSource());

		List<Emp> empList = client.select(ALL, Emp.class);
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

	@Bean(createMethod = "@jp.dodododo.sql.issue.Issue47Test$Dept2@create(resultSetMap)")
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
