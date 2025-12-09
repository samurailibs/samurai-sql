package jp.dodododo.sql.metadata;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import javax.sql.DataSource;

import jp.dodododo.sql.unit.DbTestExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TableMetaDataTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@ParameterizedTest
	@ValueSource(strings = {"emp", "EMP", "Emp", "EmP", "EMp"})
	public void tableMetaDataConnectionString(String tableName) {
		TableMetaData data = new TableMetaData(getConnection(), tableName);

		ColumnMetaData columnMetaData = data.getColumnMetaData("ename");
		assertNotNull(columnMetaData);
		ColumnMetaData pk = data.getColumnMetaData("EMPNO");
		assertTrue(pk.isPrimaryKey());
	}

	protected Connection getConnection() {
		return dbTestExtension.getConnection();
	}

}

