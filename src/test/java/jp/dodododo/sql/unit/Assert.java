package jp.dodododo.sql.unit;

import jp.dodododo.sql.util.StringUtil;
import org.opentest4j.AssertionFailedError;

public class Assert {

	public static void assertMatches(String message, String pattern, String actual) {
		if (actual.matches(pattern) == false) {
			throw new AssertionFailedError(message, pattern, actual);
		}
	}

	public static void assertMatches(String pattern, String actual) {
		assertMatches(null, pattern, actual);
	}
	
	public static void assertEqualsIgnoreCase(String message, String expected, String actual) {
		if(StringUtil.equalsIgnoreCase(expected, actual) == false) {
			throw new AssertionFailedError(message, expected, actual);
		}
	}
	
	public static void assertEqualsIgnoreCase(String expected, String actual) {
		assertEqualsIgnoreCase(null, expected, actual);
	}

}
