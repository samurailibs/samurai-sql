package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SomeEntitiesTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsert() {
		dao = newTestDao(dbTestExtension.getDataSource());

		Emp e = new Emp();
		e.EMPNO = "100";
		e.ENAME = "name";

		Dept d = new Dept();
		d.DEPTNO = "10";

		Misc misc = new Misc();
		misc.JOB = "foo_job";
		misc.MGR = "11";
		misc.SAL = "0";
		misc.COMM = "1";
		misc.TSTAMP = null;

		dao.insert("EMP", e, d, misc);

		Map<String, Object> result = dao.selectOneMap("select * from EMP where EMPNO = 100").get();
		TypeConverter converter = new TypeConverter(result);
		assertEquals(new Integer("1"), converter.getInteger("COMM"));
		assertEquals(new Integer("10"), converter.getInteger("DEPTNO"));
		assertNull(result.get("HIREDATE"));
		assertEquals(new Integer("11"), converter.getInteger("MGR"));
		assertEquals(new Integer("0"), converter.getInteger("SAL"));
		assertNull(result.get("TSTAMP"));
		assertEquals("name", result.get("ENAME"));
		assertEquals("foo_job", result.get("JOB"));
		assertEquals(new Integer("100"), converter.getInteger("EMPNO"));

		misc.JOB = "bar_job";

		dao.update("EMP", e, d, misc);

		result = dao.selectOneMap("select * from EMP where EMPNO = 100").get();
		converter = new TypeConverter(result);
		assertEquals(new Integer("1"), converter.getInteger("COMM"));
		assertEquals(new Integer("10"), converter.getInteger("DEPTNO"));
		assertNull(result.get("HIREDATE"));
		assertEquals(new Integer("11"), converter.getInteger("MGR"));
		assertEquals(new Integer("0"), converter.getInteger("SAL"));
		assertNull(result.get("TSTAMP"));
		assertEquals("name", result.get("ENAME"));
		assertEquals("bar_job", result.get("JOB"));
		assertEquals(new Integer("100"), converter.getInteger("EMPNO"));

		Dept dept = new Dept();
		dept.DEPTNO= "99";
		dao.insert(dept);
	}

	public static class Emp {
		public String EMPNO;
		public String ENAME;
	}

	public static class Dept {
		public String DEPTNO;
	}

	public static class Misc {
		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		public String TSTAMP;
	}
}
