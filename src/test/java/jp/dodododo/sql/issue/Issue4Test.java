package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.Dao;
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

	private Dao dao;

	@Test
	public void test() {
		dao = newTestClient(dbTestExtension.getDataSource());
		RootEntity rootEntity = new RootEntity();
		rootEntity.emp.setCOMM("2");
		rootEntity.emp.setDEPTNO("10");
		rootEntity.emp.setEMPNO("1");
		rootEntity.emp.setENAME("ename");
		int count = dao.insert(rootEntity);
		assertEquals(1, count);

		try {
			dao.insert(rootEntity);
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
