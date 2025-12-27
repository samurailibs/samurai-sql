package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InsertObjectAndMap {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@Test
	public void testInsertAndSelect() {
		client = newTestClient(dbTestExtension.getDataSource());

		assertFalse(client.exists(new Emp(1)));
		assertFalse(client.exists(new Emp(2)));
		assertFalse(client.exists("emp", new Emp(1), args("ename", "nnn", "job", "jjj")));
		int count = client.insert("emp", new Emp(1), args("ename", "nnn", "job", "jjj"));
		assertEquals(1, count);
		assertFalse(client.exists(new Emp(2)));
		assertTrue(client.exists(new Emp(1)));
		assertTrue(client.exists("emp", args("ename", "nnn", "job", "jjj"), new Emp(1)));
		count = client.insert("emp", new Emp(2), args("ename", "nnn", "job", "jjj"));
		assertEquals(1, count);
		assertTrue(client.exists(new Emp(1)));
		assertTrue(client.exists(new Emp(2)));

		TypeConverter result = new TypeConverter(client.selectOneMap("select * from EMP where empno = 1"));
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
