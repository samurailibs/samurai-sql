package jp.dodododo.sql.util;

import java.lang.reflect.Array;

/**
 * @author Satoshi Kimura
 */
public class ToStringer {

	private static final String SEPARATOR = ",";

	private static final String ARRAY_BEGIN = "{";

	private static final String ARRAY_END = "}";

	public static String toString(Object target) {
		if (target == null) {
			return "null";
		}

		if (target.getClass().isArray() == false) {
			return target.toString();
		}

		return toStringArray(target);
	}

	private static String toStringArray(Object array) {
		StringBuilder buf = new StringBuilder();
		buf.append(ARRAY_BEGIN);
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			buf.append(toString(Array.get(array, i)));
			buf.append(SEPARATOR);
		}
		if (length > 0) {
			buf.setLength(buf.length() - SEPARATOR.length());
		}
		buf.append(ARRAY_END);
		return buf.toString();
	}
}
