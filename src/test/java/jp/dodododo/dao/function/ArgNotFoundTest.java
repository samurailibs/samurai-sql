package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.exception.ArgNotFoundException;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ArgNotFoundTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testSelect() {
		dao = newTestDao(dbTestExtension.getDataSource());

		try {
			dao.selectMap("select * from emp where id = /*arg1*/0", args("arg0", "1", "arg2", "1"));
		} catch (ArgNotFoundException e) {
			assertEquals("引数が見つかりませんでした。[引数名=arg1, SQL=select * from emp where id = /*arg1*/0]", e.getMessage());
		}
	}
}
