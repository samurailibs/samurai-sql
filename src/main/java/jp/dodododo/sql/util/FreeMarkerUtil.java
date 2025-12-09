package jp.dodododo.sql.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class FreeMarkerUtil {
	private static final Configuration defaultConfig = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

	public static void setDefaultEncoding(String enc) {
		Configuration config = getDefaultConfig();
		config.setDefaultEncoding(enc);
	}

	public static String getDefaultEncoding() {
		Configuration config = getDefaultConfig();
		return config.getDefaultEncoding();
	}

	public static String process(String templateText, Object data) {
		try {
			Configuration config = getDefaultConfig();
			Template template = new Template("Template", new StringReader(
					templateText), config);
			StringWriter stringWriter = new StringWriter();
			template.process(data, stringWriter);
			stringWriter.flush();
			return stringWriter.toString();
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Configuration getDefaultConfig() {
		return defaultConfig;
	}
}
