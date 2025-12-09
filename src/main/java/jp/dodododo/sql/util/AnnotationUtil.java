package jp.dodododo.sql.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class AnnotationUtil {
	public static <A extends Annotation> List<A> getParameterAnnotations(Constructor<?> constructor, Class<A> annotation) {

		List<A> ret = new ArrayList<>();
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
		return ret;
	}

	public static Annotation[] getParameterAnnotations(Constructor<?> constructor, int paramIndex) {
		return constructor.getParameterAnnotations()[paramIndex];

	}
}
