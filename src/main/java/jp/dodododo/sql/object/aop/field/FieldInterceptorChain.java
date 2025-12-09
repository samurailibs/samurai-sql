package jp.dodododo.sql.object.aop.field;

import java.util.ArrayList;
import java.util.List;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class FieldInterceptorChain extends FieldInterceptor {

	private List<FieldInterceptor> interceptors = new ArrayList<>();

	public void add(FieldInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public Object get(FieldAccess fieldRead) throws Throwable {
		FieldAccess nestInvocation = new NestedFieldAccess(fieldRead, interceptors);
		return nestInvocation.proceed();
	}

	@Override
	public Object set(FieldAccess fieldWrite) throws Throwable {
		FieldAccess nestInvocation = new NestedFieldAccess(fieldWrite, interceptors);
		return nestInvocation.proceed();
	}

}
