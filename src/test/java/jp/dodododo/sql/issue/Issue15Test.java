package jp.dodododo.sql.issue;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.exception.MissingPrimaryKeyValueException;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue15Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private SamuraiSqlClient client;

	@BeforeEach
	public void setUp() throws Exception {
		SqlConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws ParseException {
		client = newTestClient(dbTestExtension.getDataSource());

		try {
			client.update("EMP", map("ename", "mike"));
			fail();
		} catch (MissingPrimaryKeyValueException success) {
			assertEquals("00044", success.getMessageCode());
		}

		try {
			client.delete("EMP", map("ename", "mike"));
			fail();
		} catch (MissingPrimaryKeyValueException success) {
			assertEquals("00044", success.getMessageCode());
		}
	}
}
