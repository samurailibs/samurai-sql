package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.row.Row;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue10Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@BeforeEach
	public void setUp() throws Exception {
		DaoConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws ParseException {
		dao = newTestDao(dbTestExtension.getDataSource());
		EMP emp = new EMP();
		emp.EMPNO = "1";
		emp.HIREDATE = "2000-02-02";
		emp.TSTAMP = "2000/01/01";

		int count = dao.insert(emp);
		assertEquals(1, count);

		Row row= dao.selectOne("SELECT * FROM EMP WHERE EMPNO = 1", Row.class).get();
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
