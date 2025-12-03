package jp.dodododo.dao.io;

import static jp.dodododo.dao.util.IOUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.util.TmpFileUtil;

import org.junit.jupiter.api.Test;

public class MultiInputStreamTest {

	@Test
	public void testByteArray() throws Exception {
		ByteArrayInputStream original = new ByteArrayInputStream("abc".getBytes());

		InputStream in = new MultiInputStream(original);
		String str = TypeConverter.convert(in, String.class);
		assertEquals("abc", str);
	}

	@Test
	public void testGZIP() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		GZIPOutputStream gzipOutput = new GZIPOutputStream(out);
		copy(new ByteArrayInputStream("abc".getBytes()), gzipOutput);
		gzipOutput.finish();
		gzipOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals("abc", str);
	}

	@Test
	public void testDeflater() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out);
		String val = "abc";
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testDeflater2() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out);
		String val = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testDeflater3() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_SPEED));
		String val = "abc";
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testDeflater4() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION));
		String val = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testLongGZIP() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		GZIPOutputStream gzipOutput = new GZIPOutputStream(out);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			builder.append(UUID.randomUUID());
		}
		String val = builder.toString();
		copy(new ByteArrayInputStream(val.getBytes()), gzipOutput);
		gzipOutput.finish();
		gzipOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testLongDeflater() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			builder.append(UUID.randomUUID());
		}
		String val = builder.toString();
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testLongDeflater2() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			builder.append(UUID.randomUUID());
		}
		String val = builder.toString();
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testLongDeflater3() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_SPEED));
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			builder.append(UUID.randomUUID());
		}
		String val = builder.toString();
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}

	@Test
	public void testLongDeflater4() throws Exception {
		File tmpFile = TmpFileUtil.createTempFile();
		FileOutputStream out = new FileOutputStream(tmpFile);

		DeflaterOutputStream deflateOutput = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION));
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			builder.append(UUID.randomUUID());
		}
		String val = builder.toString();
		copy(new ByteArrayInputStream(val.getBytes()), deflateOutput);
		deflateOutput.finish();
		deflateOutput.close();

		InputStream in = new MultiInputStream(new AutoCloseFileInputStream(tmpFile));
		String str = TypeConverter.convert(in, String.class);
		assertEquals(val, str);
	}
}
