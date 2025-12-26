package jp.dodododo.sql.exception;

// TODO change parent class
public class ArgNotFoundException extends MissingPrimaryKeyValueException {
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
