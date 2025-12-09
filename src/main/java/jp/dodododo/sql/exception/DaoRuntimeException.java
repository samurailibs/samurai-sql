package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class DaoRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	protected String messageCode;

	public DaoRuntimeException() {
		super();
	}

	public DaoRuntimeException(String messageCode, Object... args) {
		super(Message.getMessage(messageCode, args));
		this.messageCode = messageCode;
	}

	public DaoRuntimeException(Throwable cause, String messageCode, Object... args) {
		super(Message.getMessage(messageCode, args), cause);
	}

	public DaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public String getMessageCode() {
		return messageCode;
	}

}
