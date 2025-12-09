package jp.dodododo.sql.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class CacheUtil {

	public static <K, V> Map<K, V> cacheMap() {
		return cacheMap(1024);
	}

	public static <K, V> Map<K, V> cacheMap(int initialCapacity) {
		return cacheMap(new HashMap<>(initialCapacity));
	}

	public static <V> Map<String, V> cacheCaseInsensitiveMap() {
		return cacheCaseInsensitiveMap(1024);
	}

	public static <V> Map<String, V> cacheCaseInsensitiveMap(int initialCapacity) {
		return cacheMap(new CaseInsensitiveMap<>(initialCapacity));
	}

	public static <K, V> Map<K, V> cacheMap(Map<K, V> cacheMap) {
		cacheMapList.add(cacheMap);
		return cacheMap;
	}

	private static List<Map<?, ?>> cacheMapList = Collections.synchronizedList(new LinkedList<>());

	private static List<List<?>> cacheListList = Collections.synchronizedList(new LinkedList<>());

	private static List<CacheObject<?>> cacheObjectList = Collections.synchronizedList(new LinkedList<>());

	public static void clearAllCache() {
		cacheMapList.stream().parallel().forEach(cacheMap -> cacheMap.clear());
		cacheListList.stream().parallel().forEach(cacheList -> cacheList.clear());
		cacheObjectList.stream().parallel().forEach(cacheObject -> cacheObject.clear());

		cacheMapList.clear();
		cacheListList.clear();
		cacheObjectList.clear();
	}

	public static <T> List<T> cacheList() {
		return cacheList(new ArrayList<>(1024));
	}

	public static <T> List<T> cacheList(List<T> list) {
		List<T> cacheList = Collections.synchronizedList(list);
		cacheListList.add(cacheList);
		return cacheList;
	}

	public static <T> CacheObject<T> cacheObject() {
		CacheObject<T> ret = new CacheObject<>();
		cacheObjectList.add(ret);
		return ret;
	}
}
