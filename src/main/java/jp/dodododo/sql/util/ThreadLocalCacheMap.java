package jp.dodododo.sql.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThreadLocalCacheMap<K, V> implements Map<K, V> {

	private ThreadLocal<Map<Object, Object>> threadLocalCaches = ThreadLocal.withInitial(() -> new HashMap<>());

    @Override
	public void clear() {
		ThreadLocalUtil.remove(threadLocalCaches);
	}

    @Override
	public boolean containsKey(Object key) {
		return getMap().containsKey(key);
	}

    @Override
	public boolean containsValue(Object value) {
		return getMap().containsValue(value);
	}

    @Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return getMap().entrySet();
	}

	@SuppressWarnings("unchecked")
	private Map<K, V> getMap() {
		return (Map<K, V>) threadLocalCaches.get();
	}

    @Override
	public V get(Object key) {
		return getMap().get(key);
	}

    @Override
	public boolean isEmpty() {
		return getMap().isEmpty();
	}

    @Override
	public Set<K> keySet() {
		return getMap().keySet();
	}

    @Override
	public V put(K key, V value) {
		return getMap().put(key, value);
	}

    @Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getMap().putAll(m);
	}

    @Override
	public V remove(Object key) {
		return getMap().remove(key);
	}

    @Override
	public int size() {
		return getMap().size();
	}

    @Override
	public Collection<V> values() {
		return getMap().values();
	}

	@Override
	public boolean equals(Object o) {
		return getMap().equals(o);
	}

	@Override
	public int hashCode() {
		return getMap().hashCode();
	}
}
