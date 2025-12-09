package jp.dodododo.sql.flyweight;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.types.JavaType;
import jp.dodododo.sql.util.ThreadLocalCacheMap;
import jp.dodododo.sql.util.TypesUtil;

public class FlyweightFactory {

	public static final FlyweightFactory DEFAULT_FLYWEIGHT_FACTORY = new NullFlyweightFactory();

	private static FlyweightFactory factory = DEFAULT_FLYWEIGHT_FACTORY;

	public static <T> T get(T value) {
		return factory.getFlyweight(value);
	}

	public static void dispose() {
		factory.clear();
	}

	public static void setFactory(FlyweightFactory factory) {
		FlyweightFactory.factory = factory;
	}

	private static Map<Class<?>, Map<Object, Object>> cacheMap = new ThreadLocalCacheMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getFlyweight(T value) {
		if (value == null) {
			return value;
		}
		Class<?> clazz = value.getClass();
		JavaType<?> javaType = TypesUtil.getJavaType(clazz);
		if (Object.class.equals(javaType.getType())) {
			return value;
		}
		Map<Object, Object> objects = cacheMap.get(clazz);
		if (objects == null) {
			objects = new HashMap<>();
			cacheMap.put(clazz, objects);
		}
		Object key = value;
		if (value instanceof Date) {
			Date v = (Date) value;
			key = v.getTime();
		}
		if (value instanceof Calendar) {
			Calendar v = (Calendar) value;
			key = v.getTime().getTime();
		}

		Object val = objects.get(key);
		if (val != null) {
			return (T) val;
		}
		objects.put(key, value);
		return value;
	}

	public void clear() {
		cacheMap.clear();
	}

}
