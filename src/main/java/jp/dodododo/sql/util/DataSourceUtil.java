package jp.dodododo.sql.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jp.dodododo.sql.error.SQLError;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class DataSourceUtil {

	public static Connection getConnection(DataSource dataSource) {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

}
