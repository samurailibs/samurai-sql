package jp.dodododo.sql.unit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public interface DBConfig {

	String driverClassName();

	String URL() throws IOException, URISyntaxException;

	String user();

	String password();

	default Properties properties() {
		Properties properties = new Properties();
		if (user() != null) {
			properties.put("user", user());
		}
		if (password() != null) {
			properties.put("password", password());
		}
		return properties;
	}
}
