package jp.dodododo.dao.util;

import static jp.dodododo.dao.util.EmptyUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.dodododo.dao.exception.InvocationTargetRuntimeException;
import jp.dodododo.dao.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodUtil.class);

	@SuppressWarnings("unchecked")
	public static <T> T invoke(Method method, Object target, Object... args) {

		try {
			if (logger.isTraceEnabled()) {
				logger.trace(Message.getMessage("00028", method.getDeclaringClass().getName(), method.getName(), toParamString(method)));
			}
			return (T) method.invoke(target, args);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw ((RuntimeException) cause);
			}
			if (cause instanceof Error) {
				throw ((Error) cause);
			}
			throw new InvocationTargetRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace(Message.getMessage("00029", method.getDeclaringClass().getName(), method.getName(), toParamString(method)));
			}
		}
	}

	private static final String EMPTY_STRING = "";

	private static String toParamString(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (isEmpty(parameterTypes) == true) {
			return EMPTY_STRING;
		}
		StringBuilder builder = new StringBuilder();
		for (Class<?> paramType : parameterTypes) {
			builder.append(paramType.getSimpleName() + ", ");
		}
		builder.setLength(builder.length() - 2);
		return builder.toString();
	}

	public static boolean isBridgeMethod(Method m) {
		return m.isBridge();
	}

	public static boolean isSyntheticMethod(Method m) {
		return m.isSynthetic();
	}

}
