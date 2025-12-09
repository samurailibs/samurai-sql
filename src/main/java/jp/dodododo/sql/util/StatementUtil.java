package jp.dodododo.sql.util;

import java.sql.SQLException;
import java.sql.Statement;

import jp.dodododo.sql.error.SQLError;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class StatementUtil {

	public static void close(Statement s) {
		try {
			if (s == null) {
				return;
			}
			s.close();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static void setQueryTimeout(Statement ps, int queryTimeout) {
		if (0 < queryTimeout) {
			try {
				ps.setQueryTimeout(queryTimeout);
			} catch (SQLException e) {
				throw new SQLError(e);
			}
		}
	}
}
