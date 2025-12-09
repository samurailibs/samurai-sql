package jp.dodododo.sql.message;

import static jp.dodododo.sql.util.EmptyUtil.*;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class Message {
	private static ResourceBundle bundle = ResourceBundle.getBundle("jp/dodododo/sql/message");

	public static String getMessage(String key, Object... args) {
		String text = bundle.getString(key);
		if (isEmpty(args) == true) {
			return text;
		}
		return MessageFormat.format(text, args);
	}

}
