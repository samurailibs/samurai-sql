package jp.dodododo.sql.config;

public class SqlConfig {

	protected static SqlConfig defaultConfig = new SqlConfig(true);

	private static final int DEFAULT_LOG_MAX_SIZE = 128;
	private static final int DEFAULT_QUERY_TIMEOUT = -1;
	private static final double DEFAULT_LONG_QUERY_SECONDS = Double.MAX_VALUE;
	private static final String DEFAULT_ENCODING = "JISAutoDetect";

	private int logMaxSize = DEFAULT_LOG_MAX_SIZE;

	private String encoding = DEFAULT_ENCODING;

	private double longQuerySeconds = DEFAULT_LONG_QUERY_SECONDS;

	private String[] formats = new String[0];

	private boolean debugMode= false;

	private int queryTimeout = DEFAULT_QUERY_TIMEOUT;

	public SqlConfig() {
		this.logMaxSize = defaultConfig.logMaxSize;
		this.encoding = defaultConfig.encoding;
		this.longQuerySeconds = defaultConfig.longQuerySeconds;
		this.formats = defaultConfig.formats;
		this.debugMode = defaultConfig.debugMode;
		this.queryTimeout = defaultConfig.queryTimeout;
	}

	private SqlConfig(boolean defaultConfig) {
	}

	public int getLogMaxSize() {
		return logMaxSize;
	}

	public void setLogMaxSize(int logMaxSize) {
		this.logMaxSize = logMaxSize;
	}

	public static SqlConfig getDefaultConfig() {
		return defaultConfig;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public double getLongQuerySeconds() {
		return longQuerySeconds;
	}

	public void setLongQuerySeconds(double longQuerySeconds) {
		this.longQuerySeconds = longQuerySeconds;
	}

	public void setFormats(String... formats) {
		if (formats == null) {
			throw new IllegalArgumentException();
		}
		this.formats = formats;
	}

	public String[] getFormats() {
		return formats;
	}


	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

}
