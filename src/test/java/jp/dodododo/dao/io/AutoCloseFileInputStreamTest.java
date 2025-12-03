package jp.dodododo.dao.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class AutoCloseFileInputStreamTest {

	@Test
	public void testAutoClose() throws Exception {
		testAutoClose(1);
		testAutoClose(65536);
	}

	private void testAutoClose(int buffSize) throws Exception {
		AutoCloseFileInputStream stream = new AutoCloseFileInputStream(new File("README.md"));
		int available = stream.available();
		int read = stream.read(new byte[buffSize]);
		do {
			if (available == read || read <= 0) {
				assertTrue(stream.isClosed());
				break;
			} else {
				read = stream.read(new byte[buffSize]);
			}
		} while (true);

		stream.close();
	}

	@Test
	public void testSomeClose() throws Exception {
		AutoCloseFileInputStream stream = new AutoCloseFileInputStream(new File("README.md"));
		for (int i = 0; i < 10; i++) {
			stream.close();
		}
	}
}
