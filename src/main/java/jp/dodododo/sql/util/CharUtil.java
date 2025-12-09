package jp.dodododo.sql.util;

public class CharUtil {

	public static boolean isHalfWidth(Character c) {
		if (c == null) {
			return false;
		}
		if (' ' <= c && c <= '~') { // alphabet, num, mark (32 - 126)
			return true;
		}
		if ('｡' <= c && c <= 'ﾟ') { // Half-width KANA (65377 - 65439)
			return true;
		}
		return false;
	}

	public static boolean isFullWidth(Character c) {
		if (c == null) {
			return false;
		}
		return !isHalfWidth(c);
	}
}
