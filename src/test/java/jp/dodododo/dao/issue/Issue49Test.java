package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.Assert.*;
import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Arg;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue49Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() throws Exception {
		dao = newTestDao(dbTestExtension.getDataSource());

		Emp1 emp1 = dao.selectOne(Emp1.class, from("emp"), by("empno", 7369, "argname", "a")).get();
		assertEquals(7369, emp1.empno);
		assertEquals("SMITH", emp1.name);

		Emp2 emp2 = dao.selectOne(Emp2.class, from("emp"), by("empno", 7369, "argname", "a")).get();
		assertEquals(7369, emp2.empno);
		assertEquals("a", emp2.name);

		Emp2 emp3 = dao.selectOne(Emp2.class, from("emp"), by("empno", 7369, "foo", "a")).get();
		assertEquals(7369, emp3.empno);
		assertEquals("SMITH", emp3.name);

		Emp3 emp4 = dao.selectOne(Emp3.class, from("emp"), by("empno", 7369, "argname", "a")).get();
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
