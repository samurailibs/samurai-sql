package jp.dodododo.sql.lazyloading;

import java.lang.reflect.Constructor;

import jp.dodododo.sql.exception.FailLazyLoadException;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.object.aop.field.FieldAccess;
import jp.dodododo.sql.object.aop.field.FieldInterceptor;
import jp.dodododo.sql.object.aop.field.FieldInterceptors;
import jp.dodododo.sql.util.ClassUtil;
import jp.dodododo.sql.util.ConstructorUtil;

public class ProxyFactory {

	public <T> T create(Class<T> clazz) {

		return ClassUtil.newInstance(clazz);
	}

	public <T> T create(Class<T> clazz, Constructor<T> constructor, Object... args) {

		return ConstructorUtil.newInstance(constructor, args);
	}

	static {
		FieldInterceptor interceptor = new FieldInterceptor() {
			@Override
			public Object get(FieldAccess fieldRead) throws Throwable {
				init(fieldRead.getThis(), fieldRead);
				return fieldRead.proceed();
			}

			@Override
			public Object set(FieldAccess fieldWrite) throws Throwable {
				init(fieldWrite.getThis(), fieldWrite);
				return fieldWrite.proceed();
			}

			private void init(Object target, FieldAccess fieldAccess) {
				if (target == null) {
					return;
				}
				if (target instanceof LazyLoadingProxy == false) {
					return;
				}
				@SuppressWarnings("unchecked")
				LazyLoadingProxy<Object> proxy = (LazyLoadingProxy<Object>) target;
				init(proxy, fieldAccess);
			}

			private void init(LazyLoadingProxy<Object> target, FieldAccess fieldAccess) {
				Object real = target.real();
				if (real != null) {
					setThis(fieldAccess,target, real);
					return;
				}
				real = target.lazyLoad();
				if (real == null) {
					throw new FailLazyLoadException(target);
				}
				target.setReal(real);
				setThis(fieldAccess, target,real);
			}

			private void setThis(FieldAccess fieldAccess, Object target, Object real) {
				if (target.getClass().isAssignableFrom(fieldAccess.getField().getDeclaringClass())) {
					return;
				}
				fieldAccess.setThis(real);
			}
		};
		FieldInterceptors.addInterceptor(interceptor);
	}

	public static ProxyFactory newInstance(Class<?> clazz) {
		boolean auto = false;
		if (AutoLazyLoadingProxy.class.isAssignableFrom(clazz)) {
			auto = true;
		}
		if (auto == true) {
			try {
				return (ProxyFactory) ClassUtil.newInstance(ClassUtil.forName("jp.dodododo.sql.lazyloading.AutoProxyFactory"));
			} catch (RuntimeException e) {
				if (e.getCause() instanceof ClassNotFoundException) {
					throw new RuntimeException(Message.getMessage("00024"), e);
				} else {
					throw e;
				}
			}
		} else {
			return new ProxyFactory();
		}
	}
}
