package jp.dodododo.sql.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import jp.dodododo.sql.message.Message;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class ClassUtil {
	public static String getShortName(Class<?> clazz) {
		String name = clazz.getName();
		int i = name.lastIndexOf('.');
		if (i < 0) {
			return name;
		}
		return name.substring(i + 1, name.length());
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(className, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(String className, boolean initialize, ClassLoader loader) {
		try {
			return (Class<T>) Class.forName(className, initialize,loader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(className, e);
		}
	}

	public static <T> T newInstance(String className) {
		Class<T> clazz = forName(className);
		return newInstance(clazz);
	}

	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(clazz.toString(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(clazz.toString(), e);
		}
	}

	public static <T> List<Constructor<T>> getPublicConstructors(Class<T> clazz) {
		return getConstructors(clazz, Modifier.PUBLIC);
	}

	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException(clazz.toString(), e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(clazz.toString(), e);
		}
	}

	public static <T> List<Constructor<T>> getConstructors(Class<T> clazz, int modifier) {
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		List<Constructor<T>> ret = new ArrayList<>(constructors.length);
		for (Constructor<?> constructor : constructors) {
			if ((modifier & constructor.getModifiers()) != 0) {
				@SuppressWarnings("unchecked")
				Constructor<T> c = (Constructor<T>) constructor;
				ret.add(c);
			}
		}
		return ret;
	}

	public static List<Method> getPublicMethod(Class<?> clazz) {
		return getMethod(clazz, Modifier.PUBLIC);
	}

	public static Method getPublicMethod(Class<?> clazz, String methodName) {
		List<Method> methods = getPublicMethod(clazz);
		List<Method> nominee = new ArrayList<>(2);
		methods.stream().filter(method -> method.getName().equals(methodName)).forEach(method -> nominee.add(method));
		if (nominee.size() == 1) {
			return nominee.get(0);
		}
		if (nominee.size() == 0) {
			return null;
		}
		throw new RuntimeException(Message.getMessage("00042") + " " + nominee);
	}

	public static List<Method> getMethod(Class<?> clazz, int modifier) {

		Method[] methods = clazz.getMethods();
		List<Method> ret = new ArrayList<>(methods.length);
		for (Method method : methods) {
			if ((modifier & method.getModifiers()) != 0) {
				ret.add(method);
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(T t) {
		return (Class<T>) t.getClass();
	}
}
