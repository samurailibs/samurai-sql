package jp.dodododo.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public abstract class IOUtil {

	public static long copy(InputStream input, OutputStream output) {
		long count = 0L;
		try {
			byte[] buffer = new byte[4096];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			output.flush();
			return count;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
