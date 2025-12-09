package jp.dodododo.sql.compress;

import static jp.dodododo.sql.types.TypeConverter.*;
import static jp.dodododo.sql.util.FileStreamUtil.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import jp.dodododo.sql.io.BufferedOutputStream;
import jp.dodododo.sql.util.CloseableUtil;
import jp.dodododo.sql.util.IOUtil;
import jp.dodododo.sql.util.TmpFileUtil;

public enum CompressType {

	/**
	 *
	 */
	NONE {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return stream;
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return stream;
		}
	},
	/**
	 *
	 */
	GZIP {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			GZIPOutputStream out = null;
			try {
				File tmpFile = createTempFile(tmpDataMode);
				OutputStream tmpDataOutputStream = tmpDataOutputStream(tmpDataMode, tmpFile, bufferSize);
				out = new GZIPOutputStream(tmpDataOutputStream, bufferSize);
				IOUtil.copy(stream, out);
				CloseableUtil.close(out, true);
				return tmpDataInputStream(tmpDataMode, tmpFile, bufferSize, tmpDataOutputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} finally {
				CloseableUtil.close(out, true);
			}
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			try {
				return new GZIPInputStream(stream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	},
	/**
	 * @see Deflater#BEST_COMPRESSION
	 */
	ZLIB_BEST_COMPRESSION(Deflater.BEST_COMPRESSION) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#BEST_SPEED
	 */
	ZLIB_BEST_SPEED(Deflater.BEST_SPEED) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#DEFAULT_COMPRESSION
	 */
	ZLIB_DEFAULT_COMPRESSION(Deflater.DEFAULT_COMPRESSION) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#DEFAULT_STRATEGY
	 */
	ZLIB_DEFAULT_STRATEGY(Deflater.DEFAULT_STRATEGY) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#DEFLATED
	 */
	ZLIB_DEFLATED(Deflater.DEFLATED) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#FILTERED
	 */
	ZLIB_FILTERED(Deflater.FILTERED) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#HUFFMAN_ONLY
	 */
	ZLIB_HUFFMAN_ONLY(Deflater.HUFFMAN_ONLY) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	/**
	 * @see Deflater#NO_COMPRESSION
	 */
	ZLIB_NO_COMPRESSION(Deflater.NO_COMPRESSION) {
		@Override
		protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
			return toZlibStream(stream, bufferSize, tmpDataMode);
		}

		@Override
		protected InputStream uncompress(InputStream stream) {
			return new InflaterInputStream(stream);
		}
	},
	;

	private int level;

	private CompressType() {
	}

	private CompressType(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public InputStream compressedInputStream(Object value, int bufferSize, TmpDataMode tmpDataMode) {
		return compressedInputStream(convert(value, InputStream.class), bufferSize, tmpDataMode);
	}

	protected InputStream compressedInputStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
		throw new UnsupportedOperationException();
	}

	public InputStream uncompress(Object value) {
		return uncompress(convert(value, InputStream.class));
	}

	protected InputStream uncompress(InputStream stream) {
		throw new UnsupportedOperationException();
	}

	protected InputStream toZlibStream(InputStream stream, int bufferSize, TmpDataMode tmpDataMode) {
		DeflaterOutputStream out = null;
		try {
			File tmpFile = createTempFile(tmpDataMode);
			OutputStream tmpDataOutputStream = tmpDataOutputStream(tmpDataMode, tmpFile, bufferSize);
			out = new DeflaterOutputStream(tmpDataOutputStream, new Deflater(getLevel()), bufferSize);
			IOUtil.copy(stream, out);
			CloseableUtil.close(out, true);
			return tmpDataInputStream(tmpDataMode, tmpFile, bufferSize, tmpDataOutputStream);
		} finally {
			CloseableUtil.close(out, true);
		}
	}

	protected static InputStream tmpDataInputStream(TmpDataMode tmpDataMode, File tmpFile, int bufferSize, OutputStream tmpDataOutputStream) {
		InputStream stream = null;
		switch (tmpDataMode) {
		case FILE:
			stream = newInputStream(tmpFile);
			break;
		case MEMORY:
			stream = new ByteArrayInputStream(((ByteArrayOutputStream)(((BufferedOutputStream) tmpDataOutputStream)).unwrap()).toByteArray());
			break;
		default:
			throw new RuntimeException("tmpDataMode = " + tmpDataMode);
		}
		return new BufferedInputStream(stream, bufferSize);
	}

	protected static OutputStream tmpDataOutputStream(TmpDataMode tmpDataMode, File tmpFile, int bufferSize) {
		OutputStream stream;
		switch (tmpDataMode) {
		case FILE:
			stream = newFileOutputStream(tmpFile);
			break;
		case MEMORY:
			stream = new ByteArrayOutputStream(bufferSize);
			break;
		default:
			throw new RuntimeException("tmpDataMode = " + tmpDataMode);
		}
		 return new BufferedOutputStream(stream, bufferSize);
	}

	protected static File createTempFile(TmpDataMode tmpDataMode) {
		if (tmpDataMode == TmpDataMode.FILE) {
			return TmpFileUtil.createTempFile();
		}
		return null;
	}
}
