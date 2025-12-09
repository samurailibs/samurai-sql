package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class EndCommentNotFoundRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -6339085205503559009L;

	public EndCommentNotFoundRuntimeException() {
		super(Message.getMessage("00021"));
	}
}
