package jp.dodododo.sql.error;

import javax.naming.NamingException;

public class NamingError extends Error {
	private static final long serialVersionUID = -7772785034038872490L;

	public NamingError(NamingException cause) {
		super(cause);
	}

}
