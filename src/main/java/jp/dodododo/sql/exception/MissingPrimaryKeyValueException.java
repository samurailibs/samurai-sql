package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class MissingPrimaryKeyValueException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	protected String messageCode;

	public MissingPrimaryKeyValueException() {
		super();
	}

	public MissingPrimaryKeyValueException(String messageCode, Object... args) {
		super(Message.getMessage(messageCode, args));
		this.messageCode = messageCode;
	}

	public MissingPrimaryKeyValueException(Throwable cause, String messageCode, Object... args) {
		super(Message.getMessage(messageCode, args), cause);
	}

	public MissingPrimaryKeyValueException(Throwable cause) {
		super(cause);
	}

	public String getMessageCode() {
		return messageCode;
	}

}
