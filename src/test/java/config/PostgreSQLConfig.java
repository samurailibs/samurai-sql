package config;

import jp.dodododo.sql.unit.DBConfig;

public class PostgreSQLConfig implements DBConfig {

	@Override
	public String driverClassName() {
		return "org.postgresql.Driver";
	}

	@Override
	public String URL() {
		return "jdbc:postgresql://localhost/TEST";
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
