package jp.dodododo.dao.issue;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.exception.DaoRuntimeException;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue15Test {

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

		try {
			dao.update("EMP", map("ename", "mike"));
			fail();
		} catch (DaoRuntimeException success) {
			assertEquals("00044", success.getMessageCode());
		}

		try {
			dao.delete("EMP", map("ename", "mike"));
			fail();
		} catch (DaoRuntimeException success) {
			assertEquals("00044", success.getMessageCode());
		}
	}
}
