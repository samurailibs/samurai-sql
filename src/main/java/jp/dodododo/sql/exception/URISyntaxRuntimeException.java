package jp.dodododo.sql.exception;

import java.net.URISyntaxException;

public class URISyntaxRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public URISyntaxRuntimeException(URISyntaxException cause) {
		super(cause);
	}
}
