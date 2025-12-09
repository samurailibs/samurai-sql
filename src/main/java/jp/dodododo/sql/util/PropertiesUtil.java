package jp.dodododo.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Satoshi Kimura
 */
public abstract class PropertiesUtil {
	public static Properties getProperties(String resourcePath) {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
			Properties properties = new Properties();
			properties.load(is);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			CloseableUtil.close(is);
		}
	}
}
