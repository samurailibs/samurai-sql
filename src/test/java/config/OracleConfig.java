package config;

import jp.dodododo.sql.unit.DBConfig;

public class OracleConfig implements DBConfig {

	@Override
	public String driverClassName() {
		return "oracle.jdbc.driver.OracleDriver";
	}

	@Override
	public String URL() {
		return "jdbc:oracle:thin:@xxx:1521:xxx";
	}

	@Override
	public String user() {
		return "xxx";
	}

	@Override
	public String password() {
		return "xx";
	}
}
