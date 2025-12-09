package jp.dodododo.sql.exception;

import java.lang.reflect.InvocationTargetException;

public class InvocationTargetRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvocationTargetRuntimeException() {
		super();
	}

	public InvocationTargetRuntimeException(String message, InvocationTargetException cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvocationTargetRuntimeException(String message, InvocationTargetException cause) {
		super(message, cause);
	}

	public InvocationTargetRuntimeException(String message) {
		super(message);
	}

	public InvocationTargetRuntimeException(InvocationTargetException cause) {
		super(cause);
	}

	@Override
	public synchronized InvocationTargetException getCause() {
		return (InvocationTargetException) super.getCause();
	}
}
