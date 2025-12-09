package jp.dodododo.sql.util;

import java.util.Collection;

public class CollectionUtil {

	public static Object[] toArray(Collection<?> collection) {
		return collection.toArray(new Object[collection.size()]);
	}

	public static <E> E[] toArray(Collection<E> collection, E[] array) {
		return collection.toArray(array);
	}
}
