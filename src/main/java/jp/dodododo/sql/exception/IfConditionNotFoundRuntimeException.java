package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class IfConditionNotFoundRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4946612167040898739L;

	public IfConditionNotFoundRuntimeException() {
		super(Message.getMessage("00022"));
	}
}
