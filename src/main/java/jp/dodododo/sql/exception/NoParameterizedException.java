package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class NoParameterizedException extends RuntimeException {

	private static final long serialVersionUID = -9092057391332120976L;

	private Class<?> targetClass;

	private String propertyName;

	public NoParameterizedException(Class<?> targetClass, String propertyName) {
		super(Message.getMessage("00018", targetClass.getName(), propertyName));
		this.targetClass = targetClass;
		this.propertyName = propertyName;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

}
