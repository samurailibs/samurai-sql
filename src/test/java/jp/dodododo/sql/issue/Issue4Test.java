package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.impl.Dept;
import jp.dodododo.sql.impl.Emp;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue4Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@Test
	public void test() {
		client = newTestClient(dbTestExtension.getDataSource());
		RootEntity rootEntity = new RootEntity();
		rootEntity.emp.setCOMM("2");
		rootEntity.emp.setDEPTNO("10");
		rootEntity.emp.setEMPNO("1");
		rootEntity.emp.setENAME("ename");
		int count = client.insert(rootEntity);
		assertEquals(1, count);

		try {
			client.insert(rootEntity);
			fail();
		} catch (SQLRuntimeException success) {
		}
	}

	@Table("EMP")
	public static class RootEntity {
		public Dept a = new Dept();
		public Emp emp = new Emp();
	}
}
