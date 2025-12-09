package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue7Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void test() {
		dao = newTestClient(dbTestExtension.getDataSource());
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
