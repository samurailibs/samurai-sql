package jp.dodododo.sql.script;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.util.MethodUtil;

public class Each implements IterationCallback<Object> {

	public static final String METHOD_NAME = "each";

	private Map<Class<?>, Method> methodMap = new HashMap<>();

	public Each() {
		Arrays.asList(getClass().getMethods()).stream(). //
				filter(m -> MethodUtil.isBridgeMethod(m) == false).//
				filter(m -> MethodUtil.isSyntheticMethod(m) == false).//
				filter(m -> isEach(m)).//
				forEach(m -> methodMap.put(m.getParameterTypes()[0], m));

		if (methodMap.isEmpty()) {
			throw new RuntimeException("Method not found.");
		}
	}

	private boolean isEach(Method method) {
		return METHOD_NAME.equals(method.getName()) && method.getParameterTypes().length == 1;
	}

	@Override
	public final List<Object> getResult() {
		return null;
	}

	private Method getMethod(Object o) {
		Class<?> clazz = o.getClass();
		Method method = methodMap.get(clazz);
		while (method == null && !clazz.equals(Object.class)) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> iface : interfaces) {
				method = methodMap.get(iface);
				if (method != null) {
					return method;
				}
			}
			clazz = clazz.getSuperclass();
			method = methodMap.get(clazz);
		}
		return method;
	}

    @Override
	public final void iterate(Object o) {
		Method method = getMethod(o);
		if (method == null) {
			throw new RuntimeException("Method not found. argType=" + o.getClass().getName());
		}
		if (method.isAccessible() == false) {
			method.setAccessible(true);
		}
		MethodUtil.invoke(method, this, new Object[] { o });
	}

}
