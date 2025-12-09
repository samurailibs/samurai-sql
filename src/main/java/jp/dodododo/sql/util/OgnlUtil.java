package jp.dodododo.sql.util;

import java.util.Map;

import jp.dodododo.sql.message.Message;
import ognl.ClassResolver;
import ognl.DefaultMemberAccess;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlException;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class OgnlUtil {

	private static final ClassResolver CLASS_RESOLVER = new DefaultClassResolver();

	public static Object getValue(String expression, Object root) {
		return getValue(parseExpression(expression), root);
	}

	public static Object parseExpression(String expression) {
		return parseExpression(expression, null, 0);
	}

	public static Object getValue(Object exp, Object root) {
		return getValue(exp, root, null, 0);
	}

	private static Object getValue(Object exp, Object root, String path, int lineNumber) {
		return getValue(exp, null, root, path, lineNumber);
	}

	private static Object getValue(Object exp, //
			@SuppressWarnings("rawtypes")//
			Map ctx,//
			Object root, String path, int lineNumber) throws OgnlRuntimeException {
		try {
			if (ctx != null) {
				return Ognl.getValue(exp, ctx, root);
			} else {
				@SuppressWarnings("rawtypes")
				Map context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true), CLASS_RESOLVER, new DefaultTypeConverter());
				return Ognl.getValue(exp, context, root);
			}
		} catch (OgnlException ex) {
			throw new OgnlRuntimeException(ex.getReason() == null ? ex : ex.getReason(), path, lineNumber);
		} catch (Exception ex) {
			throw new OgnlRuntimeException(ex, path, lineNumber);
		}
	}

	protected static final Map<String, Object> PARSED_EXPRESSION_CACHE = CacheUtil.cacheMap();

	private static Object parseExpression(String expression, String path, int lineNumber) throws OgnlRuntimeException {
		Object object = PARSED_EXPRESSION_CACHE.get(expression);
		if (object != null) {
			return object;
		}
		try {
			Object ret = Ognl.parseExpression(expression);
			PARSED_EXPRESSION_CACHE.put(expression, ret);
			return ret;
		} catch (Exception ex) {
			throw new OgnlRuntimeException(ex, path, lineNumber);
		}
	}

	/**
	 *
	 * @author Satoshi Kimura
	 */
	public static class OgnlRuntimeException extends RuntimeException {

		private static final long serialVersionUID = 2290558407298004909L;

		public OgnlRuntimeException(Throwable cause) {
			this(cause, null, 0);
		}

		public OgnlRuntimeException(Throwable cause, String path, int lineNumber) {
			super(Message.getMessage("00016", createMessage(cause, path, lineNumber)), cause);
		}

		protected static String createMessage(Throwable cause, String path, int lineNumber) {
			StringBuilder buf = new StringBuilder(128);
			buf.append(cause.getMessage());
			if (EmptyUtil.isNotEmpty(path)) {
				buf.append(" at ").append(path).append("(").append(lineNumber).append(")");
			}
			return buf.toString();
		}
	}

	public static class DefaultClassResolver extends Object implements ClassResolver {
		private static final Map<String, Class<?>> CLASSES = CacheUtil.cacheMap(128);

		@Override
		public Class<?> classForName(String className, @SuppressWarnings("rawtypes") Map context) throws ClassNotFoundException		{
			Class<?> clazz = CLASSES.get(className);
			if (clazz != null) {
				return clazz;
			}
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				if (className.indexOf('.') == -1) {
					clazz = Class.forName("java.lang." + className);
				}
			}
			CLASSES.put(className, clazz);
			return clazz;
		}
	}

}
