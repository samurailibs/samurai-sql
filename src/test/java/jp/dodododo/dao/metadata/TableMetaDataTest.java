package jp.dodododo.dao.metadata;

import static org.junit.Assert.*;

import java.sql.Connection;

import javax.sql.DataSource;

import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@RunWith(Theories.class)
public class TableMetaDataTest {

	@DataPoints({"tableNames"})
	public static String[] tableNames = {"emp", "EMP", "Emp", "EmP", "EMp"};

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@Theory
	public void tableMetaDataConnectionString(@FromDataPoints("tableNames") String tableName) {
		TableMetaData data = new TableMetaData(getConnection(), tableName);

		ColumnMetaData columnMetaData = data.getColumnMetaData("ename");
		assertNotNull(columnMetaData);
		ColumnMetaData pk = data.getColumnMetaData("EMPNO");
		assertTrue(pk.isPrimaryKey());
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}

	private Connection getConnection() {
		return dbTestExtension.getConnection();
	}

}
