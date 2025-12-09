package jp.dodododo.sql.exception;

public class InvalidSQLException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	protected String sql;

	public InvalidSQLException(String sql, Throwable t) {
		super(sql, t);
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}

}
