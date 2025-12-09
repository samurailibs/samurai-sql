package jp.dodododo.sql.dialect;

import java.sql.Connection;
import java.sql.SQLException;

public interface TableNameResolver {
	TableNameResolver NULL_RESOLVER = new TableNameResolver() {
	};

	default String resolve(Connection connection, String tableName) throws SQLException {
		return tableName;
	}
}
