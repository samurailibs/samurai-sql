package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.exception.UnsupportedTypeException;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class OneColumnResultSetHandlerTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@Test
	public void testInsertAndSelect() {
		client = newTestClient(dbTestExtension.getDataSource());

		List<String> resultStringList = client.select("SELECT ename FROM EMP ORDER BY empno", String.class);
		assertEquals("SMITH", resultStringList.get(0));

		List<Integer> resultIntegerList = client.select("SELECT sal FROM EMP ORDER BY empno", Integer.class);
		assertEquals(800, (int) resultIntegerList.get(0));

		List<Integer> resultBigDecimalList = client.select("SELECT sal FROM EMP ORDER BY empno", Integer.class);
		assertEquals(Integer.valueOf("800"), resultBigDecimalList.get(0));

		@SuppressWarnings("rawtypes")
		List<Map> resultMapList = client.select("SELECT * FROM EMP ORDER BY empno", Map.class);
		assertEquals(7369, ((Number) resultMapList.get(0).get("empno")).intValue());
		assertEquals("SMITH", resultMapList.get(0).get("ename"));
		assertEquals(800, ((Number) resultMapList.get(0).get("sal")).intValue());

		try {
			client.select("SELECT sal FROM emp ORDER BY empno", Class.class);
			fail();
		} catch (UnsupportedTypeException success) {
			System.out.println(success.getMessage());
		}
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
		@Id(value = @IdDefSet(strategy = Sequence.class, name = "sequence"), targetTables = { "emp" })
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
