package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SomeObjectTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient sqlClient;

	@Test
	public void testInsertAndSelect() {
		sqlClient = newTestClient(dbTestExtension.getDataSource());
		Emp emp = new Emp();
		emp.dept = new Dept();
		emp.dept.DEPTNO = "10";
		emp.person = new Person();
		emp.COMM = "2";
		// emp.EMPNO = "1";
		emp.TSTAMP = null;
		emp.person.TSTAMP = new Date();
		emp.person.NAME = "ename";
		int count = sqlClient.insert("EMP", emp);
		assertEquals(1, count);
		String empNo = emp.no.EMPNO;

		List<Emp> select = sqlClient.select("select * from EMP where EMPNO =" + empNo, Emp.class);
		assertEquals(empNo, select.get(0).no.EMPNO);
		assertEquals(Integer.valueOf(2), TypeConverter.convert(select.get(0).COMM, Integer.class));
		assertEquals("10", select.get(0).dept.DEPTNO);
		assertEquals("ename", select.get(0).person.NAME);
		assertNotNull(select.get(0).person.TSTAMP);
	}

	public static class Emp {
		public Empno no = new Empno();

		public Person person;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		@Column(table = "detp", value = "tstamp")
		public Date TSTAMP;

		public Dept dept;
	}

	public static class Empno {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				}, targetTables = { "emp" })
		public String EMPNO;
	}

	public static class Person {
		@Column("ename")
		public String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

	}

	public static class Dept {
		@Column("deptNO")
		public String DEPTNO;
	}
}
