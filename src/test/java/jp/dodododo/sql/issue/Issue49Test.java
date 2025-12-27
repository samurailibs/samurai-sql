package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Arg;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue49Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();


	@Test
	public void test() throws Exception {
		SamuraiSqlClient client = newTestClient(dbTestExtension.getDataSource());

		Emp1 emp1 = client.selectOne(Emp1.class, from("emp"), by("empno", 7369, "argname", "a")).get();
		assertEquals(7369, emp1.empno);
		assertEquals("SMITH", emp1.name);

		Emp2 emp2 = client.selectOne(Emp2.class, from("emp"), by("empno", 7369, "argname", "a")).get();
		assertEquals(7369, emp2.empno);
		assertEquals("a", emp2.name);

		Emp2 emp3 = client.selectOne(Emp2.class, from("emp"), by("empno", 7369, "foo", "a")).get();
		assertEquals(7369, emp3.empno);
		assertEquals("SMITH", emp3.name);

		Emp3 emp4 = client.selectOne(Emp3.class, from("emp"), by("empno", 7369, "argname", "a")).get();
		assertEquals(7369, emp4.empno);
		assertEquals("a", emp4.name);

	}

	@Table("emp")
	public static class Emp1 {
		protected int empno;
		protected String name;

		public Emp1(@Column("empno") int empno, @Column("ename") @Arg("argname") String name) {
			this.empno = empno;
			this.name = name;
		}
	}

	@Table("emp")
	public static class Emp2 {
		protected int empno;
		protected String name;

		public Emp2(@Column("empno") int empno, @Arg("argname") @Column("ename") String name) {
			this.empno = empno;
			this.name = name;
		}
	}
	@Table("emp")
	public static class Emp3 {
		protected int empno;
		protected String name;

		public Emp3(@Column("empno") int empno, @Column("foo") @Arg("argname") String name) {
			this.empno = empno;
			this.name = name;
		}
	}

}
