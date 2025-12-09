package jp.dodododo.sql.object.aop.field;

import java.lang.reflect.Field;
import java.util.List;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class NestedFieldAccess extends FieldAccess {

	private final FieldAccess parent;

	private final FieldInterceptor[] interceptors;

	private int interceptorsIndex;

	public NestedFieldAccess(FieldAccess parent, FieldInterceptor... interceptors) {
		this.parent = parent;
		this.interceptors = interceptors;
	}

	public NestedFieldAccess(FieldAccess parent, List<FieldInterceptor> interceptors) {
		this(parent, interceptors.toArray(new FieldInterceptor[interceptors.size()]));
	}

	@Override
	public Object proceed() throws Throwable {
		if (interceptorsIndex < interceptors.length) {
			if (parent.getAccessType() == AccessType.READ) {
				return interceptors[interceptorsIndex++].get(this);
			} else {
				return interceptors[interceptorsIndex++].set(this);
			}
		}
		return parent.proceed();
	}

	@Override
	public Object getThis() {
		return parent.getThis();
	}

	@Override
	public AccessType getAccessType() {
		return parent.getAccessType();
	}

	@Override
	public Field getField() {
		return parent.getField();
	}

	@Override
	public Object getValueToSet() {
		return parent.getValueToSet();
	}

	@Override
	public void setThis(Object target) {
		parent.setThis(target);
	}

	@Override
	public boolean equals(Object obj) {
		return parent.equals(obj);
	}

	@Override
	public int hashCode() {
		return parent.hashCode();
	}

}
