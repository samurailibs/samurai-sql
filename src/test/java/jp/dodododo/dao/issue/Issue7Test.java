package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.Assert.*;
import jp.dodododo.dao.Dao;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue7Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() {
		dao = newTestDao(dbTestExtension.getDataSource());
		Emp emp = new Emp();
		int count = dao.insert(emp);
		assertEquals(1, count);
	}

	public static class Emp {
		public No no = No.TWO;
	}

	public static enum No {

		ONE(1), TWO(2);

		@Column("EMPNO")
		public int no;

		private No(int no) {
			this.no = no;
		}

	}
}
