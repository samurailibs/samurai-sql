package jp.dodododo.sql.util;

import static jp.dodododo.sql.util.EmptyUtil.*;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class StringUtil {
	public static String newString(byte[] bs, String charset) {
		try {
			return new String(bs, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * fooBarBaz → FOO_BAR_BAZ
	 */
	public static String decamelize(String s) {
		if (isEmpty(s)) {
			return s;
		}
		int length = s.length();
		StringBuilder buf = new StringBuilder(length);
		for (int i = 0; i < length - 1; i++) {
			if (Character.isLowerCase(s.charAt(i)) && Character.isUpperCase(s.charAt(i + 1))) {
				buf.append(s.charAt(i));
				buf.append('_');
			} else {
				buf.append(s.charAt(i));
			}
		}
		buf.append(s.charAt(length - 1));
		return buf.toString().toUpperCase();
	}

	/**
	 * FOO_BAR_BAZ → fooBarBaz
	 */
	public static String camelize(String targetStr) {
		if (targetStr == null) {
			return null;
		}
		Pattern p = Pattern.compile("_([a-z])");
		Matcher m = p.matcher(targetStr.toLowerCase());

		StringBuffer sb = new StringBuffer(targetStr.length());
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * fooBarBaz → FooBarBaz
	 */
	public static String capitalize(String s) {
		if (s == null || isEmpty(s)) {
			return s;
		}
		return (new StringBuilder(s.length())).append(Character.toTitleCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * FooBarBaz → fooBarBaz
	 */
	public static String decapitalize(String s) {
		if (s == null) {
			return null;
		}
		return Introspector.decapitalize(s);
	}

	public static String trimLine(String sql) {
		try {
			StringBuilder ret = new StringBuilder();
			BufferedReader bufferedReader = new BufferedReader(new StringReader(sql));
			for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
				if (EmptyUtil.isNotEmpty(line.trim())) {
					ret.append(line);
					ret.append("\n");
				}
			}
			return ret.toString().trim();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static int width(String s) {
		if (s == null) {
			return 0;
		}

		int ret = 0;
		for (int i = 0; i < s.length(); i++) {
			if (CharacterUtil.isHalf(s.charAt(i))) {
				ret += 1;
			} else {
				ret += 2;
			}
		}
		return ret;
	}

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		}
		if (s1 == null && s2 != null) {
			return false;
		}
		if (s1 != null && s2 == null) {
			return false;
		}
		CaseInsensitiveSet tmp = new CaseInsensitiveSet();
		tmp.add(s1);
		return tmp.contains(s2);
	}

	public static String[] split(String str, String separatorChars) {
		return str.split(separatorChars);
		// return StringUtils.split(str, separatorChars);
	}

	public static String replace(String text, String searchString, String replacement) {
		return text.replaceAll(searchString, replacement);
		// return StringUtils.replace(text, searchString, replacement);
	}

	public static int length(String s) {
		int length = 0;
		for (int i = 0; i < s.length(); i++) {
			if (CharUtil.isHalfWidth(s.charAt(i))) {
				length++;
			} else {
				length++;
				length++;
			}

		}
		return length;
	}
}
