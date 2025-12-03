package jp.dodododo.dao.object;

import java.util.Map;

import jp.dodododo.dao.annotation.Internal;
import jp.dodododo.dao.util.CacheUtil;

@Internal
public class ObjectDescFactory {

	private static final Map<Class<?>, ObjectDesc<?>> OBJECT_DESC_CACHE = CacheUtil.cacheMap();

	public static <T> ObjectDesc<T> getObjectDesc(T o) {
		return getObjectDesc(o, true);
	}

	@SuppressWarnings("unchecked")
	public static <T> ObjectDesc<T> getObjectDesc(T o, boolean isIgnoreCase) {
		if (o instanceof Map) {
			return (ObjectDesc<T>) new MapObjectDesc((Map<String, Object>) o, isIgnoreCase);
		}
		return getObjectDesc((Class<T>) o.getClass(), isIgnoreCase);
	}

	public static <T> ObjectDesc<T> getObjectDesc(Class<T> clazz) {
		return getObjectDesc(clazz, true);
	}

	public static <T> ObjectDesc<T> getObjectDesc(Class<T> clazz, boolean isIgnoreCase) {
		@SuppressWarnings("unchecked")
		ObjectDesc<T> objectDesc = (ObjectDesc<T>) OBJECT_DESC_CACHE.get(clazz);
		if (objectDesc == null) {
			objectDesc = new ObjectDesc<>(clazz, isIgnoreCase);
			OBJECT_DESC_CACHE.put(clazz, objectDesc);
		}
		return objectDesc;
	}

	public static void clearCache() {
		OBJECT_DESC_CACHE.clear();
	}
}
