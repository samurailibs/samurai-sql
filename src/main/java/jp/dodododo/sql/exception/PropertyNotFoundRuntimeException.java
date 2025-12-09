package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class PropertyNotFoundRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4672244189882523042L;

	private Class<?> targetClass;

	private String propertyName;

	public PropertyNotFoundRuntimeException(Class<?> targetClass, String propertyName) {
		super(Message.getMessage("00006", targetClass.getName(), propertyName));
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