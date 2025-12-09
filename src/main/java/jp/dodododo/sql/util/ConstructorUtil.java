package jp.dodododo.sql.util;

import static jp.dodododo.sql.util.EmptyUtil.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jp.dodododo.sql.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class ConstructorUtil {

	private static final Logger logger = LoggerFactory.getLogger(ConstructorUtil.class);

	public static <T> T newInstance(Constructor<T> constructor, Object... initArgs) {

		try {
			if (logger.isTraceEnabled()) {
				logger.trace(Message.getMessage("00026", toInfoString(constructor, initArgs)));
			}
			return constructor.newInstance(initArgs);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(toInfoString(constructor, initArgs), e);
		} catch (InstantiationException e) {
			throw new RuntimeException(toInfoString(constructor, initArgs), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(toInfoString(constructor, initArgs), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(toInfoString(constructor, initArgs), e);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace(Message.getMessage("00027", toInfoString(constructor, initArgs)));
			}
		}
	}

	public static <T> String toInfoString(Constructor<T> constructor, Object... initArgs) {

		return "Constructor=[" + constructor + "], values=[" + toValuesString(initArgs) + "], types of values=[" + toTypesInfo(initArgs) + "].";
	}

	private static String toValuesString(Object[] initArgs) {
		if (isEmpty(initArgs) == true) {
			return "";
		}
		StringBuilder4DebugInfo info = new StringBuilder4DebugInfo(128);
		for (Object initArg : initArgs) {
			info.append(initArg);
			info.append(", ");
		}
		info.setLength(info.length() - 2);
		return info.toString();
	}

	private static String toTypesInfo(Object[] initArgs) {
		if (isEmpty(initArgs) == true) {
			return "";
		}
		StringBuilder info = new StringBuilder(128);
		for (Object initArg : initArgs) {
			if (initArg == null) {
				info.append(null + ", ");
			} else {
				info.append(initArg.getClass() + ", ");
			}
		}
		info.setLength(info.length() - 2);
		return info.toString();
	}
}
