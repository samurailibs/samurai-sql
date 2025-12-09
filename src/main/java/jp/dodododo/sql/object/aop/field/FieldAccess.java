package jp.dodododo.sql.object.aop.field;

import java.lang.reflect.Field;

import jp.dodododo.sql.annotation.Internal;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.util.FieldUtil;

@Internal
public class FieldAccess {

	public static enum AccessType {
		READ, WRITE;
	}

	private Object target;
	private Field field;
	private AccessType accessType;
	private Object valueToSet;

	protected FieldAccess() {
	}

	public FieldAccess(Object target, Field field, AccessType accessType) {
		this.target = target;
		this.field = field;
		this.accessType = accessType;
	}

	public FieldAccess(Object target, Field field, AccessType accessType, Object valueToSet) {
		this(target, field, accessType);
		this.valueToSet = valueToSet;
	}

	public Object proceed() throws Throwable {
		switch (accessType) {
		case READ:
			return FieldUtil.get(field, target);
		case WRITE:
			FieldUtil.set(field, target, valueToSet);
			return null;
		default:
			throw new IllegalStateException(Message.getMessage("00043", accessType));
		}
	}

	public Object getThis() {
		return target;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public Field getField() {
		return field;
	}

	public Object getValueToSet() {
		return valueToSet;
	}

	public void setThis(Object target) {
		this.target = target;
	}
}
