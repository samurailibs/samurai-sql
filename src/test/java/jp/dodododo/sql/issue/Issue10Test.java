package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.row.Row;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue10Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@BeforeEach
	public void setUp() throws Exception {
		SqlConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws ParseException {
		client = newTestClient(dbTestExtension.getDataSource());
		EMP emp = new EMP();
		emp.EMPNO = "1";
		emp.HIREDATE = "2000-02-02";
		emp.TSTAMP = "2000/01/01";

		int count = client.insert(emp);
		assertEquals(1, count);

		Row row= client.selectOne("SELECT * FROM EMP WHERE EMPNO = 1", Row.class).get();
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20000202"), TypeConverter.convert(row.getObject("HIREDATE"), Date.class));
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20000101"), TypeConverter.convert(row.getObject("TSTAMP"), Date.class));
	}

	public static class EMP {
		public String EMPNO;
		public String ENAME;
		public String JOB;
		public String MGR;
		public String HIREDATE;
		public String SAL;
		public String COMM;
		public String DEPTNO;
		public String TSTAMP;
	}
}
