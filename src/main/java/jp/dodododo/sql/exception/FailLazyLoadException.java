package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.object.ObjectDescFactory;

public class FailLazyLoadException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FailLazyLoadException(Object o, Throwable cause) {
		super(Message.getMessage("00045", toString(o)), cause);
	}

	public FailLazyLoadException(Object o) {
		super(Message.getMessage("00045", toString(o)));
	}

	private static String toString(Object o) {
		String simpleClassName = o.getClass().getSimpleName();
		try {
			return simpleClassName + "#" + ObjectDescFactory.getObjectDesc(o).toMapWithFields(o).toString();
		} catch (Throwable ignore) {
			return simpleClassName;
		}
	}

}
