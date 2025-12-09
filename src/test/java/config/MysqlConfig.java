package config;

import jp.dodododo.sql.unit.DBConfig;

public class MysqlConfig implements DBConfig {

	@Override
	public String driverClassName() {
		return "com.mysql.cj.jdbc.Driver";
	}

	@Override
	public String URL() {
		return "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";
	}

	@Override
	public String user() {
		return "root";
	}

	@Override
	public String password() {
		return "root";
	}
}
