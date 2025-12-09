package jp.dodododo.sql.object.aop.field;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class FieldInterceptors {
	private static FieldInterceptorChain interceptorChain = new FieldInterceptorChain();

	public static FieldInterceptor getInterceptorChain() {
		return interceptorChain;
	}

	public static synchronized void setInterceptorChain(FieldInterceptorChain interceptor) {
		FieldInterceptors.interceptorChain = interceptor;
	}

	public static void addInterceptor(FieldInterceptor interceptor) {
		FieldInterceptors.interceptorChain.add(interceptor);
	}

}
