package jp.dodododo.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jp.dodododo.sql.exception.URISyntaxRuntimeException;

public class URLUtil {

	public static URL newURL(String spec) {
		try {
			return new URL(spec);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static InputStream openStream(URL url) {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static URI toURI(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new URISyntaxRuntimeException(e);
		}
	}

}
