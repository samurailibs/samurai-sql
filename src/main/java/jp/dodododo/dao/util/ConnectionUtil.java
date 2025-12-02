package jp.dodododo.dao.util;

import java.sql.*;
import java.util.Properties;

import javax.sql.DataSource;

import jp.dodododo.dao.error.SQLError;
import jp.dodododo.dao.exception.SQLRuntimeException;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class ConnectionUtil {

	public static Connection getConnection(Properties properties) {

		String jndi = properties.getProperty("jndi");
		String driver = properties.getProperty("driver");
		String url = properties.getProperty("url");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		try {
			if (jndi != null) {
				DataSource dataSource = JndiUtil.lookup(DataSource.class, jndi);
				Connection connection = DataSourceUtil.getConnection(dataSource);
				connection.setAutoCommit(false);
				return connection;
			}

			ClassUtil.forName(driver);
			Connection connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
			return connection;
		} catch (SQLException e) {
			throw new SQLError(e);
		}

	}

	public static void close(Connection connection) {
		close(connection, false);
	}

	public static void close(Connection connection, boolean ignoreException) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (SQLException e) {
			if (ignoreException == false) {
				throw new SQLError(e);
			}
		}
	}

	public static PreparedStatement prepareStatement(Connection connection, String sql) {
		try {
			// RETURN_GENERATED_KEYS is harmless for non-INSERT statements.
			// It simply returns an empty ResultSet when no keys are generated.
			return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			throw new SQLRuntimeException(sql, e);
		}
	}

	public static DatabaseMetaData getMetaData(Connection connection) {
		try {
			return connection.getMetaData();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static boolean isClosed(Connection connection) {
		try {
			return connection.isClosed();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static String getSchema(Connection connection) {
		try {
			return connection.getSchema();
		} catch (AbstractMethodError e) {
			return null;
		} catch (UnsupportedOperationException e) {
			return null;
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static String getCatalog(Connection connection) {
		try {
			return connection.getCatalog();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}
}
