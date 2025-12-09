package jp.dodododo.sql.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;

public class FileInputStreamUtil {

	public static FileInputStream newFileInputStream(File file) {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new UncheckedIOException(e);
		}
	}
}
