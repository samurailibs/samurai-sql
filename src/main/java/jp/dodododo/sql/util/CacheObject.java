package jp.dodododo.sql.util;

/**
 * 
 * @author Satoshi Kimura
 */
public class CacheObject<T> {
	private T cacheValue;

	CacheObject() {
	}

	public T getValue() {
		return cacheValue;
	}

	public void setValue(T cacheValue) {
		this.cacheValue = cacheValue;
	}

	public void clear() {
		cacheValue = null;
	}

}
