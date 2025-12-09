package jp.dodododo.sql.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.util.CacheUtil;
import jp.dodododo.sql.util.ClassUtil;

public class ClassDesc<T> {

	private Class<T> targetClass;

	private Map<Integer, List<Constructor<T>>> constructors = Collections
			.synchronizedMap(new HashMap<>());

	private List<Method> methods = new ArrayList<>();

	public ClassDesc(Class<T> target) {
		this.targetClass = target;
		setUpMethods();
	}

	public List<Constructor<T>> getConstructors(int modifier) {
		List<Constructor<T>> ret = constructors.get(modifier);
		if (ret != null) {
			return ret;
		}
		ret = ClassUtil.getConstructors(targetClass, modifier);
		constructors.put(modifier, ret);
		return ret;
	}

	public <A extends Annotation> List<Constructor<T>> getAnnotatedConstructors(Class<A> annotationClass, int modifier) {

		List<Constructor<T>> constructors = getConstructors(modifier);
		List<Constructor<T>> ret = new ArrayList<>(constructors.size());
		for (Constructor<T> constructor : constructors) {
			if (constructor.getAnnotation(annotationClass) != null) {
				ret.add(constructor);
			}
		}
		return ret;
	}

	protected Map<Class<?>, List<Method>> annotatedMethods = CacheUtil.cacheMap();

	public <A extends Annotation> List<Method> getAnnotatedMethods(Class<A> annotationClass, int modifier) {

		if(annotatedMethods.containsKey(annotationClass)) {
			return annotatedMethods.get(annotationClass);
		}

		List<Method> methods = getMethods();
		List<Method> ret = new ArrayList<>(methods.size());
		methods.stream().filter(method -> method.getAnnotation(annotationClass) != null).forEach(method -> ret.add(method));
		annotatedMethods.put(annotationClass, ret);
		return ret;
	}

	private List<Method> getMethods() {
		return this.methods;
	}

	private void setUpMethods() {
		Method[] methods = targetClass.getMethods();
		Method[] declaredMethods = targetClass.getDeclaredMethods();

		this.methods = new ArrayList<>(methods.length + declaredMethods.length);

		for (Method method : methods) {
			this.methods.add(method);
		}

		for (Method method : declaredMethods) {
			if (this.methods.contains(method)) {
				continue;
			}
			this.methods.add(method);
		}
	}

	public Class<T> getTargetClass() {
		return targetClass;
	}
}
