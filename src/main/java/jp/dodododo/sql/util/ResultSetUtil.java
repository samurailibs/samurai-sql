package jp.dodododo.sql.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.error.SQLError;
import jp.dodododo.sql.types.SQLType;
import jp.dodododo.sql.types.SQLTypes;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class ResultSetUtil {

	public static void close(ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static Object getObject(ResultSet rs, ResultSetColumn column) {
		int dataType = column.getDataType();
		SQLType sqlType = SQLTypes.valueOf(dataType);
		try {
			return sqlType.toJavaType().getValue(rs, column.getName());
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static Object getObject(ResultSet rs, int index) {
		try {
			return rs.getObject(index);
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static Object getObject(ResultSet rs, String columnLabel) {
		try {
			return rs.getObject(columnLabel);
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public static boolean next(ResultSet rs) {
		try {
			return rs.next();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}
}
