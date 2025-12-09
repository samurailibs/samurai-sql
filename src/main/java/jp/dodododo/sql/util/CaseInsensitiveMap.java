package jp.dodododo.sql.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ignore UpperCase,LowerCase,CamelCase and SnakeCase.
 *
 * <br>
 *
 * UpperCase,LowerCase,CamelCase,SnakeCaseを無視。
 *
 * @author Satoshi Kimura
 *
 * @param <V> valueType
 */
public class CaseInsensitiveMap<V> extends HashMap<String, V> {
	private static final long serialVersionUID = -5454924302411799023L;

	private HashMap<String, V> cache;

	private HashMap<String, V> original;

	private CaseInsensitiveMap0<V> camelize;

	private CaseInsensitiveMap0<V> decamelize;

	public CaseInsensitiveMap() {
		super();
		cache = new HashMap<>();
		original = new HashMap<>();
		camelize = new CaseInsensitiveMap0<>();
		decamelize = new CaseInsensitiveMap0<>();
	}

	public CaseInsensitiveMap(int i, float f) {
		super(i, f);
		cache = new HashMap<>(i, f);
		original = new HashMap<>(i, f);
		camelize = new CaseInsensitiveMap0<>(i, f);
		decamelize = new CaseInsensitiveMap0<>(i, f);
	}

	public CaseInsensitiveMap(int i) {
		super(i);
		cache = new HashMap<>(i);
		original = new HashMap<>(i);
		camelize = new CaseInsensitiveMap0<>(i);
		decamelize = new CaseInsensitiveMap0<>(i);
	}

	public CaseInsensitiveMap(Map<String, V> map) {
		super(map);
		int size = map.size();
		cache = new HashMap<>(size);
		original = new HashMap<>(size);
		camelize = new CaseInsensitiveMap0<>(size);
		decamelize = new CaseInsensitiveMap0<>(size);
		putAll(map);
	}

	@Override
	public void clear() {
		original.clear();
		camelize.clear();
		decamelize.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		boolean containsKey = original.containsKey(key);
		if (containsKey == true) {
			return true;
		}
		containsKey = camelize.containsKey(key);
		if (containsKey == true) {
			return true;
		}
		containsKey = decamelize.containsKey(key);
		return containsKey;
	}

	@Override
	public boolean containsValue(Object obj) {
		boolean containsValue = original.containsValue(obj);
		if (containsValue == true) {
			return true;
		}
		containsValue = camelize.containsValue(obj);
		if (containsValue == true) {
			return true;
		}
		containsValue = decamelize.containsValue(obj);
		return containsValue;

	}

	@Override
	public Set<Entry<String, V>> entrySet() {
		Map<String, V> tmp = new HashMap<>();
		tmp.putAll(decamelize);
		tmp.putAll(camelize);
		tmp.putAll(original);
		return tmp.entrySet();
	}

	@Override
	public boolean equals(Object obj) {
		try {
			@SuppressWarnings("unchecked")
			CaseInsensitiveMap<V> target = (CaseInsensitiveMap<V>) obj;
			if (camelize.equals(target.camelize) == false) {
				return false;
			}
			if (decamelize.equals(target.decamelize) == false) {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public V get(Object obj) {
		V value = cache.get(obj);
		if (value != null) {
			return value;
		}
		value = original.get(obj);
		if (value != null) {
			cache.put((String) obj, value);
			return value;
		}
		value = camelize.get(obj);
		if (value != null) {
			cache.put((String) obj, value);
			return value;
		}
		value = decamelize.get(obj);
		cache.put((String) obj, value);
		return value;

	}

	@Override
	public boolean isEmpty() {
		return original.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		Set<String> set = new HashSet<String>();
		// set.addAll(decamelize.keySet());
		// set.addAll(camelize.keySet());
		set.addAll(original.keySet());
		return set;
	}

	@Override
	public V put(String key, V val) {
		original.put(key, val);
		camelize.put(StringUtil.camelize(key), val);
		return decamelize.put(StringUtil.decamelize(key), val);
	}

	@Override
	public final void putAll(@SuppressWarnings("rawtypes") Map map) {
		if (map == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		Set<Entry<String, V>> entrySet = map.entrySet();
		entrySet.forEach(entry -> put(entry.getKey(), entry.getValue()));
	}

	@Override
	public V remove(Object obj) {
		original.remove(obj);
		camelize.remove(obj);
		return decamelize.remove(obj);
	}

	@Override
	public int size() {
		return original.size();
	}

	@Override
	public String toString() {
		return original.toString();
	}

	@Override
	public Collection<V> values() {
		return original.values();
	}

	/**
	 * Ignore UpperCase and LowerCase.
	 *
	 * <br>
	 *
	 * UpperCase,LowerCaseを無視。
	 *
	 * @author Satoshi Kimura
	 *
	 * @param <V> valueType
	 */
	private static class CaseInsensitiveMap0<V> extends HashMap<String, V> {

		private static final long serialVersionUID = -7275013706892532473L;

		public CaseInsensitiveMap0() {
			super();
		}

		public CaseInsensitiveMap0(int capacity) {
			super(capacity);
		}

		public CaseInsensitiveMap0(int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
		}

		@Override
		public final V get(Object key) {
			return super.get(convertKey(key));
		}

		@Override
		public final V put(String key, V value) {
			return super.put(convertKey(key), value);
		}

		@Override
		public final void putAll(@SuppressWarnings("rawtypes") Map map) {
			@SuppressWarnings("unchecked")
			Set<Entry<String, V>> entrySet = map.entrySet();
			entrySet.forEach(entry -> put(convertKey(entry.getKey()), entry.getValue()));
		}

		@Override
		public final V remove(Object key) {
			return super.remove(convertKey(key));
		}

		@Override
		public boolean containsKey(Object key) {
			return super.containsKey(convertKey(key));
		}

		private String convertKey(Object key) {
			if (key == null) {
				return null;
			}
			return key.toString().toUpperCase();
		}
	}
}