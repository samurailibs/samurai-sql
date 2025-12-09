package jp.dodododo.sql.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * 圧縮されている状態でもそのまま読み込めるようにするためのStream.<br>
 * 以下に対応したい。
 *
 * <pre>
 * ・非圧縮
 * ・Gzip
 * ・Deflater/Inflater
 * </pre>
 */
public class MultiInputStream extends InputStream {

	protected InputStream originalStream;
	protected InputStream stream;

	public MultiInputStream(File file) throws IOException {
		this(new AutoCloseFileInputStream(file));
	}

	public MultiInputStream(InputStream stream) throws IOException {
		this.originalStream = stream;

		byte[] first = new byte[1024];
		int count = originalStream.read(first);
		byte[] bs = new byte[count];
		System.arraycopy(first, 0, bs, 0, count);
		first = bs;
		originalStream.reset();

		ByteArrayInputStream tmp = new ByteArrayInputStream(first);
		boolean isGzip = false;
		@SuppressWarnings("unused")
		boolean isInflater = false;
		try {
			this.stream = new GZIPInputStream(originalStream);
			isGzip = true;
		} catch (ZipException ignore) {
			stream.reset();
			tmp.reset();
		}
		if (isGzip == false && isDeflater(first)) {
			this.stream = new InflaterInputStream(originalStream);
			isInflater = true;
		}
		if (this.stream == null) {
			if (count < 1024) {
				this.stream = tmp;
			} else {
				this.stream = originalStream;
			}
		}
	}

	private boolean isDeflater(byte[] header) {
		if (header[0] != 120) {
			return false;
		}
		return true;
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public int read(byte[] paramArrayOfByte) throws IOException {
		return stream.read(paramArrayOfByte);
	}

	@Override
	public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2) throws IOException {
		return stream.read(paramArrayOfByte, paramInt1, paramInt2);
	}

	@Override
	public int hashCode() {
		return stream.hashCode();
	}

	@Override
	public boolean equals(Object paramObject) {
		return stream.equals(paramObject);
	}

	@Override
	public long skip(long paramLong) throws IOException {
		return stream.skip(paramLong);
	}

	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void mark(int paramInt) {
		stream.mark(paramInt);
	}

	@Override
	public void reset() throws IOException {
		stream.reset();
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public String toString() {
		return stream.toString();
	}
}
