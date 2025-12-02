package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Date;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Timestamp;
import jp.dodododo.dao.annotation.VersionNo;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.DialectManager;
import jp.dodododo.dao.dialect.MySQL;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.id.Identity;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.unit.DbTestExtension;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue42Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite) {
			return;
		}

		dao = newTestDao(getDataSource());

		Emp emp = new Emp();

		assertNull(emp.empno);
		assertNull(emp.tstamp);
		assertNull(emp.comm);

		Date start = new Date();
		Thread.sleep(10);
		dao.insert(emp);
		Thread.sleep(10);
		Date end = new Date();

		assertNotNull(emp.empno);
		assertNotNull(emp.tstamp);
		assertNotNull(emp.comm);

		emp = dao.selectOne(Emp.class, from("emp"), by("empno", emp.empno)).get();
		assertNotNull(emp.empno);
		assertTrue("" + start.getTime() + " : " + end.getTime() + " : " + emp.tstamp.getTime(),
				start.getTime() <= emp.tstamp.getTime() && emp.tstamp.getTime() <= end.getTime());
		assertEquals(1, (int) emp.comm);
	}

	public static class Emp {
		@Id({ @IdDefSet(strategy = Sequence.class, name = "sequence"), @IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public String empno;

		@Timestamp
		public Date tstamp;

		@VersionNo
		public Integer comm;

		public Emp getThis() {
			return this;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}

	private Connection getConnection() {
		return dbTestExtension.getConnection();
	}
}
