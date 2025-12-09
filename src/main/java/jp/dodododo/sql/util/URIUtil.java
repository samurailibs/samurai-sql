package jp.dodododo.sql.util;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jp.dodododo.sql.exception.URISyntaxRuntimeException;

public class URIUtil {
	public static URL toURL(URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static URI newURI(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new URISyntaxRuntimeException(e);
		}
	}
}
