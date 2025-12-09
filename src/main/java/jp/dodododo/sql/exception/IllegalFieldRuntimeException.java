package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class IllegalFieldRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4272266699495155085L;

	private Class<?> targetClass;

	private String propertyName;

	public IllegalFieldRuntimeException(Class<?> targetClass, String propertyName, Object value, Throwable cause) {
		super(Message.getMessage("00040", targetClass.getName(), propertyName, value, cause), cause);
		this.targetClass = targetClass;
		this.propertyName = propertyName;
	}

	public IllegalFieldRuntimeException(Class<?> targetClass, String propertyName, Throwable cause) {
		super(Message.getMessage("00041", targetClass.getName(), propertyName, cause), cause);
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