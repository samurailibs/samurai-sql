package jp.dodododo.sql.util;

import java.util.Collection;
import java.util.Map;

public class EmptyUtil {

	public static final boolean isEmpty(Collection<?> collection) {
		if (collection == null) {
			return true;
		}
		return collection.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static final boolean isEmpty(Map<?, ?> map) {
		if (map == null) {
			return true;
		}
		return map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static final boolean isEmpty(Object[] array) {
		if (array == null) {
			return true;
		}
		return array.length == 0;
	}

	public static final boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		return s.length() == 0;
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

}
