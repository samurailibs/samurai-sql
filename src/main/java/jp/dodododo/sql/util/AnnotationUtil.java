package jp.dodododo.sql.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class AnnotationUtil {

	private static final Map<Pair<Constructor<?>, Class<Annotation>>, List<?>> CACHE_MAP = CacheUtil.cacheMap();

	@SuppressWarnings("unchecked")
    public static <A extends Annotation> List<A> getParameterAnnotations(Constructor<?> constructor, Class<A> annotation) {
		Pair key = new Pair<>(constructor, annotation);

		List<A> ret = (List<A>) CACHE_MAP.get(key);
		if(ret!= null) {
			return ret;
		}

		ret = new ArrayList<>();
		Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

		for (Annotation[] annotations : parameterAnnotations) {
			boolean isAdd = false;
			for (int i = 0; i < annotations.length; i++) {
				Annotation a = annotations[i];
				if (a == null) {
					continue;
				} else if (annotation.isAssignableFrom(a.getClass()) == true) {
					@SuppressWarnings("unchecked")
					A addElement = (A) a;
					ret.add(addElement);
					isAdd = true;
					break;
				}
			}
			if (isAdd == false) {
				ret.add(null);
			}
		}
		CACHE_MAP.put(key, ret);
		return ret;
	}

	public static Annotation[] getParameterAnnotations(Constructor<?> constructor, int paramIndex) {
		return constructor.getParameterAnnotations()[paramIndex];
	}
	public record Pair<C, A>(C constructor, A annotation) {
	}

}
