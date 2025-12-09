package jp.dodododo.sql.lazyloading;

import jp.dodododo.sql.annotation.Proxy;

public class LazyLoadingUtil {

	public static boolean isProxy(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof LazyLoadingProxy<?>) {
			return true;
		}
		if (o.getClass().getAnnotation(Proxy.class) != null) {
			return true;
		}
		if (o instanceof Class && ((Class<?>) o).getAnnotation(Proxy.class) != null) {
			return true;
		}
		if (o instanceof Class && LazyLoadingProxy.class.isAssignableFrom((Class<?>) o)) {
			return true;
		}
		return false;
	}

}
