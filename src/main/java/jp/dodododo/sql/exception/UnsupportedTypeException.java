package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class UnsupportedTypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnsupportedTypeException(Class<?> clazz) {
		super(Message.getMessage("00039", clazz.getName()));
	}

}
