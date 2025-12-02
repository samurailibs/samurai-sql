package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;
import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.impl.Dept;
import jp.dodododo.dao.impl.Emp;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue4Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() {
		dao = newTestDao(dbTestExtension.getDataSource());
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
