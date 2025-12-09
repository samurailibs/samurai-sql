package jp.dodododo.sql.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.types.JavaTypes;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class EnumConverter {

	public static String toString(Enum<?> value) {

		String string = toStringByAnnotationOrInterface(value);
		if (string != null) {
			return string;
		}

		return value.name();
	}

	public static String toStringByAnnotationOrInterface(Enum<?> value) {
		try {
			Class<?> clazz = value.getClass();
			Method stringAnnotationMethod = getStringAnnotationMethod(clazz);
			return (String) MethodUtil.invoke(stringAnnotationMethod, value, (Object[]) null);
		} catch (Exception ignore) {
		}
		return null;
	}

	public static Number toNumber(Enum<?> value) {

		Number num = toNumberByAnnotationOrInterface(value);
		if (num != null) {
			return num;
		}

		return value.ordinal();
	}

	public static Number toNumberByAnnotationOrInterface(Enum<?> value) {

		try {
			Class<?> clazz = value.getClass();
			Method numAnnotationMethod = getNumAnnotationMethod(clazz);
			return (Number) MethodUtil.invoke(numAnnotationMethod, value, (Object[]) null);
		} catch (Exception ignore) {
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Enum<?> convertToEnum(Object value, Class<?> dest) {
		Class enumClass = dest;
		return EnumConverter.convert(value, enumClass);
	}

	public static <E extends Enum<E>> E convert(Object value, Class<E> enumClass) {
		if (value != null && enumClass.isAssignableFrom(value.getClass())) {
			@SuppressWarnings("unchecked")
			E ret = (E) value;
			return ret;
		}
		E[] enumConstants = enumClass.getEnumConstants();
		if (isCharSequence(value) && hasStringAnnotation(enumClass) == true) {
			Method method = getStringAnnotationMethod(enumClass);
			E ret = getEqualsValueEnum(value, enumClass, method);
			if (ret != null) {
				return ret;
			}
		} else if (isCharSequence(value)) {
			E val = nameToEnum(value, enumConstants);
			if (val != null) {
				return val;
			}
			if (hasNumAnnotation(enumClass) == true) {
				Double d = JavaTypes.DOUBLE.convert(value);
				Method method = getNumAnnotationMethod(enumClass);
				E ret = getEqualsValueEnum(d, enumClass, method);
				if (ret != null) {
					return ret;
				}
			}
		} else if (isNumber(value) && hasNumAnnotation(enumClass) == true) {
			Method method = getNumAnnotationMethod(enumClass);
			E ret = getEqualsValueEnum(value, enumClass, method);
			if (ret != null) {
				return ret;
			}
		} else if (isNumber(value) && value != null) {
			int index = ((Number) value).intValue();
			if (0 <= index && index < enumConstants.length) {
				return enumConstants[index];
			}
		}
		if (value == null) {
			return null;
		}
		throw new IllegalArgumentException("value=[" + value + "], valueClass=[" + value.getClass() + "], enumClass=["
				+ enumClass + "(" + Arrays.asList(enumConstants) + ")]");
	}

	private static boolean isCharSequence(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof CharSequence) {
			return true;
		}
		return false;
	}

	private static boolean isNumber(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Number) {
			return true;
		}
		return false;
	}

	private static <E extends Enum<E>> E getEqualsValueEnum(Object value, Class<E> enumClass, Method method) {
		E[] enumConstants = enumClass.getEnumConstants();
		if (enumConstants == null) {
			return null;
		}
		for (E enumVal : enumConstants) {
			Object val = MethodUtil.invoke(method, enumVal, (Object[]) null);
			if (isNumber(val)) {
				if (equalsNum(value, val)) {
					return enumVal;
				}

			} else if (equals(value, val)) {
				return enumVal;
			}
		}
		return null;
	}

	private static boolean equals(Object value, Object val) {
		if (value == val) {
			return true;
		}
		if (value == null && val != null) {
			return false;
		}
		if (value != null && val == null) {
			return false;
		}
		return value.equals(val);
	}

	private static boolean equalsNum(Object value, Object val) {
		if (value == val) {
			return true;
		}
		if (value == null && val != null) {
			return false;
		}
		if (value != null && val == null) {
			return false;
		}
		return ((Number) val).doubleValue() == ((Number) value).doubleValue();
	}

	private static <E extends Enum<E>> boolean hasStringAnnotation(Class<E> enumClass) {
		return getStringAnnotationMethod(enumClass) != null;
	}

	private static Method getStringAnnotationMethod(Class<?> enumClass) {
		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(enumClass);
		List<Method> annotatedMethods = objectDesc.getClassDesc().getAnnotatedMethods(jp.dodododo.sql.annotation.StringKey.class,
				Modifier.PUBLIC);
		if (annotatedMethods.isEmpty() == false) {
			return annotatedMethods.get(0);
		} else {
			return null;
		}
	}

	private static <E extends Enum<E>> boolean hasNumAnnotation(Class<E> enumClass) {
		return getNumAnnotationMethod(enumClass) != null;
	}

	private static Method getNumAnnotationMethod(Class<?> enumClass) {
		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(enumClass);
		List<Method> annotatedMethods = objectDesc.getClassDesc().getAnnotatedMethods(jp.dodododo.sql.annotation.NumKey.class,
				Modifier.PUBLIC);
		if (annotatedMethods.isEmpty() == false) {
			return annotatedMethods.get(0);
		} else {
			return null;
		}
	}

	private static <E extends Enum<E>> E nameToEnum(Object value, E[] enumConstants) {
		for (E e : enumConstants) {
			if (e.name().equals(toString(value))) {
				return e;
			}
		}
		return null;
	}

	private static String toString(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}
}
