package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.DaoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InsertObjectAndMap {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestClient(dbTestExtension.getDataSource());

		assertFalse(dao.exists(new Emp(1)));
		assertFalse(dao.exists(new Emp(2)));
		assertFalse(dao.exists("emp", new Emp(1), args("ename", "nnn", "job", "jjj")));
		int count = dao.insert("emp", new Emp(1), args("ename", "nnn", "job", "jjj"));
		assertEquals(1, count);
		assertFalse(dao.exists(new Emp(2)));
		assertTrue(dao.exists(new Emp(1)));
		assertTrue(dao.exists("emp", args("ename", "nnn", "job", "jjj"), new Emp(1)));
		count = dao.insert("emp", new Emp(2), args("ename", "nnn", "job", "jjj"));
		assertEquals(1, count);
		assertTrue(dao.exists(new Emp(1)));
		assertTrue(dao.exists(new Emp(2)));

		TypeConverter result = new TypeConverter(dao.selectOneMap("select * from EMP where empno = 1"));
		assertEquals(1, result.getInteger("empno").intValue());
		assertEquals("nnn", result.getString("ename"));
		assertEquals("jjj", result.getString("job"));
	}

	public static class Emp {

		public String EMPNO = "1";

		public Emp(int empno) {
			EMPNO = Integer.toString(empno);
		}

	}
}
