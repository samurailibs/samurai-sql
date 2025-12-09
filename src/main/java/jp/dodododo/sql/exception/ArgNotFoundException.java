package jp.dodododo.sql.exception;

public class ArgNotFoundException extends DaoRuntimeException {
	private static final long serialVersionUID = 1L;

	protected String argName;

	protected String sql;

	public ArgNotFoundException(Object... args) {
		super("00012", args);
		this.argName = (String) args[0];
		this.sql = (String) args[1];
	}

	public String getArgName() {
		return argName;
	}
}
