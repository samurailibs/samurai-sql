package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.dialect.MySQL;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.id.Identity;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


public class HasAliasTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestDao(getDataSource());
		Emp emp = new Emp("", "");
		emp.no = "10";
		emp.c = "foo";
		emp.c2 = "2";
		emp.no = "10";
		emp.TSTAMP = null;
		emp.TSTAMP = new Date();
		int count = dao.insert("EMP", emp);
		assertEquals(1, count);
		String empNo = emp.EMPNO;

		List<Emp> select = dao.select("select * from EMP where EMPNO =" + empNo, Emp.class);
		assertEquals(empNo, select.get(0).EMPNO);
		assertEquals("foo", select.get(0).NAME);
		assertEquals("10", select.get(0).no);
		assertEquals(Integer.valueOf(2), TypeConverter.convert(select.get(0).c, Integer.class));
		assertEquals(Integer.valueOf(2), TypeConverter.convert(select.get(0).c2, Integer.class));
		assertNotNull(select.get(0).TSTAMP);
	}

	public static class Emp {

		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) }, targetTables = { "emp" })
		public String EMPNO;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		@Column(value = "ename", alias = { "COMM" })
		public String c;

		@Column("COMM")
		public String c2;

		private String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

		@Column(value = "deptno", alias = "deptno")
		public String no;

		public Emp(@Column(value = "DEPTNO", alias = { "ENAME" }) String name, @Column("ENAME") String comm) {
			this.NAME = name;
			this.c = comm;
		}

		@Override
		public String toString() {
			return "Emp [EMPNO=" + EMPNO + ", JOB=" + JOB + ", MGR=" + MGR + ", HIREDATE=" + HIREDATE + ", SAL=" + SAL
					+ ", COMM=" + c + ", NAME=" + NAME + ", TSTAMP=" + TSTAMP + ", DEPTNO=" + no + "]";
		}

		public String getC() {
			return c;
		}

		public void setC(String comm) {
			c = comm;
		}

	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}
