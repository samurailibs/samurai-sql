package jp.dodododo.sql.lazyloading;

import jp.dodododo.sql.annotation.Proxy;

@Proxy
public interface LazyLoadingProxy<T> {

	T lazyLoad();

	T real();

	void setReal(T real);
}
