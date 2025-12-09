package jp.dodododo.sql.util;

import static jp.dodododo.sql.util.EmptyUtil.*;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class EnumUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T firstValue(Class<? extends Enum> enumType, Class<T> castType) {
		Enum enumVal = firstValue(enumType);
		return castType.cast(enumVal);
	}

	public static <E extends Enum<E>> E firstValue(Class<E> enumType) {
		E[] constants = getConstants(enumType);
		if (isEmpty(constants) == true) {
			return null;
		}
		return constants[0];
	}

	static public <E extends Enum<E>> E[] getConstants(Class<E> enumClass) {
		return enumClass.getEnumConstants();
	}
}
