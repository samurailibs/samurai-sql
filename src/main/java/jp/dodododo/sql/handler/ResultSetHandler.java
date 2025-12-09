package jp.dodododo.sql.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler<T> {
	void handle(ResultSet resultSet) throws SQLException;
}
