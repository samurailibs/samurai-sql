package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Date;

import javax.sql.DataSource;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.annotation.Timestamp;
import jp.dodododo.sql.annotation.VersionNo;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.DialectManager;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.unit.DbTestExtension;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue42Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@Test
	public void test() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		Emp emp = new Emp();

		assertNull(emp.empno);
		assertNull(emp.tstamp);
		assertNull(emp.comm);

		Date start = new Date();
		Thread.sleep(10);
		client.insert(emp);
		Thread.sleep(10);
		Date end = new Date();

		assertNotNull(emp.empno);
		assertNotNull(emp.tstamp);
		assertNotNull(emp.comm);

		emp = client.selectOne(Emp.class, from("emp"), by("empno", emp.empno)).get();
		assertNotNull(emp.empno);
		assertTrue(
				start.getTime() <= emp.tstamp.getTime() && emp.tstamp.getTime() <= end.getTime(),
				start.getTime() + " : " + end.getTime() + " : " + emp.tstamp.getTime());
		assertEquals(1, (int) emp.comm);
	}

	public static class Emp {
		@Id({ @IdDefSet(strategy = Sequence.class, name = "sequence")})
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
