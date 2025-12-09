package config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import jp.dodododo.sql.unit.DBConfig;

import org.sqlite.SQLiteOpenMode;

public class SqliteConfig implements DBConfig {

	@Override
	public String driverClassName() {
		return "org.sqlite.JDBC";
	}

	@Override
	public String URL() throws IOException, URISyntaxException {
		return "jdbc:sqlite:"//
				+ new File(DBConfig.class.getResource("/log4j.properties").toURI()).getParentFile().getCanonicalPath()//
				+ "/data-sqlite/test.db";
	}

	@Override
	public String user() {
		return "sa";
	}

	@Override
	public String password() {
		return "";
	}

	@Override
	public Properties properties() {
		Properties properties = DBConfig.super.properties();
		properties.put("open_mode", String.valueOf(SQLiteOpenMode.OPEN_MEMORY.ordinal()));
		return properties;
	}

	//0 1 READONLY
	//1 2 READWRITE
	//2 4 CREATE
	//3 8 DELETEONCLOSE
	//4 16 EXCLUSIVE
	//5 64 OPEN_URI
	//6 128 OPEN_MEMORY
	//7 256 MAIN_DB
	//8 512 TEMP_DB
	//9 1024 TRANSIENT_DB
	//10 2048 MAIN_JOURNAL
	//11 4096 TEMP_JOURNAL
	//12 8192 SUBJOURNAL
	//13 16384 MASTER_JOURNAL
	//14 32768 NOMUTEX
	//15 65536 FULLMUTEX
	//16 131072 SHAREDCACHE
	//17 262144 PRIVATECACHE

}