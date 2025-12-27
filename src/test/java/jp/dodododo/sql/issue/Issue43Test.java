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

public class Issue43Test {

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

		public Emp(@Column("empno") String empno, Dept dept) {
			this.empno = empno;
			this.dept = dept;
		}
	}

	@Bean(createMethod = "@jp.dodododo.sql.issue.Issue43Test$Dept@create(resultSetMap)")
	public static class Dept {

		private String deptno;

		private Dept(String deptno) {
			this.deptno = deptno;
		}

		public static Dept create(Map<String, Object> row) {
			return new Dept(new Row(row).getString("deptno"));
		}
	}
}
