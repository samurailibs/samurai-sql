package jp.dodododo.sql.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

public class TmpFileUtil {

	static private ThreadLocal<Set<File>> tmpFiles = new ThreadLocal<Set<File>>() {
		@Override
		protected java.util.Set<File> initialValue() {
			return new HashSet<File>();
		}
	};

	public static File createTempFile() {
		try {
			Set<File> tmpSet = tmpFiles.get();
			File tmpFile = File.createTempFile("samurai-dao-", ".tmp");
			tmpSet.add(tmpFile);
			tmpFile.deleteOnExit();
			return tmpFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void deleteThreadLocalTmpFiles() {
		Set<File> fileSet = tmpFiles.get();
		fileSet.forEach(file -> file.delete());
		fileSet.clear();
		ThreadLocalUtil.remove(tmpFiles);
	}
}
