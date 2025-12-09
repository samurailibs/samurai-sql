package jp.dodododo.sql.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class AutoCloseFileInputStream extends InputStream {

	protected boolean isClosed = false;
	protected File file;
	protected FileDescriptor fileDescriptor;
	protected FileInputStream delegator;

	public AutoCloseFileInputStream(FileDescriptor fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
		init();
	}

	public AutoCloseFileInputStream(File file) {
		this.file = file;
		init();
	}

	public AutoCloseFileInputStream(String filePath) {
		this(new File(filePath));
	}

	protected void init() {
		try {
			if (fileDescriptor != null) {
				delegator = new FileInputStream(fileDescriptor);
			} else if (file != null) {
				delegator = new FileInputStream(file);
			} else {
				throw new IllegalStateException();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public int read() throws IOException {
		if (isClosed()) {
			return -1;
		}
		int ret = delegator.read();
		if (ret < 0 || 0 == available()) {
			close();
		}
		return ret;
	}

	@Override
	public int read(byte[] b) throws IOException {
		if (isClosed()) {
			return -1;
		}
		int ret = delegator.read(b);
		if (ret < 0 || 0 == available()) {
			close();
		}
		return ret;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (isClosed()) {
			return -1;
		}
		int ret = delegator.read(b, off, len);
		if (ret < 0 || 0 == available()) {
			close();
		}
		return ret;
	}

	@Override
	public int available() throws IOException {
		if (isClosed() == true) {
			return 0;
		}
		try {
			return delegator.available();
		} catch (IOException e) {
			close();
			return 0;
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		try {
			delegator.close();
		} catch (Exception ignore) {
		}
		isClosed = false;
		init();
	}

	@Override
	public void close() throws IOException {
		if (isClosed() == true) {
			return;
		}
		super.close();
		isClosed = true;
	}

	public boolean isClosed() {
		return isClosed;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
		} finally {
			try {
				close();
			} catch (Exception ignore) {
			}
		}
	}
}
