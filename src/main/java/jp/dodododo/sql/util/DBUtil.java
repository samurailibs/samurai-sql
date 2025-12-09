package jp.dodododo.sql.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.Set;

public class DBUtil {

	public static Map<String, String> getTableNames(Connection connection) {
		DatabaseMetaData dmd = ConnectionUtil.getMetaData(connection);
		Set<String> tableNames = DatabaseMetaDataUtil.getTableNames(dmd);
		Map<String, String> ret = new CaseInsensitiveMap<>(tableNames.size());
		tableNames.forEach(tableName -> ret.put(tableName, tableName));
		return ret;
	}

	public static Map<String, String> getViewNames(Connection connection) {
		DatabaseMetaData dmd = ConnectionUtil.getMetaData(connection);
		Set<String> viewNames = DatabaseMetaDataUtil.getViewNames(dmd);
		Map<String, String> ret = new CaseInsensitiveMap<>(viewNames.size());
		viewNames.forEach(viewName -> ret.put(viewName, viewName));
		return ret;
	}
}
