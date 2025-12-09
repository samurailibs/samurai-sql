package jp.dodododo.sql.object.aop.field;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class FieldInterceptor {

	public Object get(FieldAccess fieldRead) throws Throwable {
		return fieldRead.proceed();
	}

	public Object set(FieldAccess fieldWrite) throws Throwable {
		return fieldWrite.proceed();
	}

}
