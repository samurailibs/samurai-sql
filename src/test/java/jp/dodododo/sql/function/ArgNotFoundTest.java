package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.exception.ArgNotFoundException;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ArgNotFoundTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testSelect() {
		dao = newTestClient(dbTestExtension.getDataSource());

		try {
			dao.selectMap("select * from emp where id = /*arg1*/0", args("arg0", "1", "arg2", "1"));
		} catch (ArgNotFoundException e) {
			assertEquals("引数が見つかりませんでした。[引数名=arg1, SQL=select * from emp where id = /*arg1*/0]", e.getMessage());
		}
	}
}
