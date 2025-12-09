package jp.dodododo.sql.error;

import java.sql.SQLException;

public class SQLError extends Error {
	private static final long serialVersionUID = 3165246253649446375L;

	public SQLError(String message, SQLException cause) {
		super(message, cause);
	}

	public SQLError(SQLException cause) {
		super(cause);
	}

	@Override
	public SQLException getCause() {
		return (SQLException) super.getCause();
	}

	public int getErrorCode() throws Exception {
		return getCause().getErrorCode();
	}

	public String getSQLState() throws Exception {
		return getCause().getSQLState();
	}
}
