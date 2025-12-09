package jp.dodododo.sql.util;

public class CharacterUtil {

	public static boolean isHalf(char c) {

		if ((c <= '\u007e') || // 英数字
				(c == '\u00a5') || // \記号
				(c == '\u203e') || // ~記号
				(c >= '\uff61' && c <= '\uff9f') // 半角カナ
		) {
			return true;
		} else {
			return false;
		}
	}

}
