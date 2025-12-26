package jp.dodododo.sql.properties;

import java.util.Properties;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.util.PropertiesUtil;

public class SamuraiSqlProperties {
	private static Properties properties;

	static {
		try {
			properties = PropertiesUtil.getProperties("samurai-sql.properties");
		} catch (RuntimeException e) {
			properties = new Properties();
		}
	}

	public static boolean enableSqlDump() {
		Boolean enable = JavaTypes.BOOLEAN.convert(properties.getProperty("enable_sql_dump"));
		if(enable == null) {
			return false;
		}
		return enable;
	}
}
