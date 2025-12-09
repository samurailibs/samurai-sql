package jp.dodododo.sql.exception;

public class TokenNotClosedRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -6966212587300368100L;

	private String token;

	private String sql;

	public TokenNotClosedRuntimeException(String token, String sql) {
		this.token = token;
		this.sql = sql;
	}

	public String getToken() {
		return token;
	}

	public String getSql() {
		return sql;
	}
}
