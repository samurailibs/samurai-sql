package jp.dodododo.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class InputStreamUtil {
	public static int available(InputStream is) {
		try {
			return is.available();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}