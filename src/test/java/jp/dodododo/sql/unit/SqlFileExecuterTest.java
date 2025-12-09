package jp.dodododo.sql.unit;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SqlFileExecuterTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@Test
	public void testExecuteSelect() {
		SqlFileExecuter executer = new SqlFileExecuter(getConnection());
		String sqlFilePath = "jp/dodododo/sql/impl/many_to_one.sql";
		executer.executeSelect(sqlFilePath);

		sqlFilePath = "jp/dodododo/sql/impl/nest_if3.sql";
		executer.executeSelect(sqlFilePath);
	}

	@Test
	public void testExecuteUpdate() {
		SqlFileExecuter executer = new SqlFileExecuter(getConnection());
		String sqlFilePath = "jp/dodododo/sql/unit/insert.sql";
		executer.executeUpdate(sqlFilePath);
	}

	private Connection getConnection() {
		return dbTestExtension.getConnection();
	}
}
