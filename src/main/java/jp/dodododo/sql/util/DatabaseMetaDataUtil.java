package jp.dodododo.sql.util;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import jp.dodododo.sql.error.SQLError;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class DatabaseMetaDataUtil {

	public static String getDatabaseProductName(DatabaseMetaData dmd) {
		try {
			return dmd.getDatabaseProductName();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static Set<String> getTableNames(DatabaseMetaData dmd) {
		return getTables(dmd, "TABLE");
	}

	public static Set<String> getViewNames(DatabaseMetaData dmd) {
		return getTables(dmd, "VIEW");
	}

	private static Set<String> getTables(DatabaseMetaData dmd, String... types) throws SQLError {
		Set<String> tableNames = new HashSet<>();
		ResultSet rs = null;
		try {
			rs = dmd.getTables(null, null, "%", types);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				tableNames.add(tableName);
			}
			return tableNames;
		} catch (SQLException e) {
			throw new SQLError(e);
		} finally {
			ResultSetUtil.close(rs);
		}
	}

	public static int getDatabaseMajorVersion(DatabaseMetaData dmd) {
		try {
			return dmd.getDatabaseMajorVersion();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static int getDatabaseMinorVersion(DatabaseMetaData dmd) {
		try {
			return dmd.getDatabaseMinorVersion();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static String getDatabaseProductVersion(DatabaseMetaData dmd) {
		try {
			return dmd.getDatabaseProductVersion();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

}
