package jp.dodododo.dao.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationUtilTest {

	@Test
	public void testGetParameterAnnotations() {
		Constructor<?>[] constructors = TargetClass.class.getConstructors();

		List<A> parameterAnnotations = AnnotationUtil.getParameterAnnotations(constructors[0], A.class);
		assertEquals("i_a", parameterAnnotations.get(0).name());
		assertEquals("s_a", parameterAnnotations.get(1).name());
		assertEquals("d_a", parameterAnnotations.get(2).name());
		assertNull(parameterAnnotations.get(3));

		List<B> parameterAnnotations2 = AnnotationUtil.getParameterAnnotations(constructors[0], B.class);
		assertEquals("i_b", parameterAnnotations2.get(0).name());
		assertEquals("s_b", parameterAnnotations2.get(1).name());
		assertNull(parameterAnnotations2.get(2));
		assertNull(parameterAnnotations2.get(3));
	}

	@Test
	public void testGetParameterAnnotationsConstructorInt() {
		Constructor<?>[] constructors = TargetClass.class.getConstructors();

		Annotation[] parameterAnnotations = AnnotationUtil.getParameterAnnotations(constructors[0], 0);
		assertEquals("i_a", ((A) parameterAnnotations[0]).name());
		assertEquals("i_b", ((B) parameterAnnotations[1]).name());

		parameterAnnotations = AnnotationUtil.getParameterAnnotations(constructors[0], 1);
		assertEquals("s_b", ((B) parameterAnnotations[0]).name());
		assertEquals("s_a", ((A) parameterAnnotations[1]).name());

	}

	public static class TargetClass {
		public TargetClass( //
				@A(name = "i_a") @B(name = "i_b")//
				int i, //
				@B(name = "s_b") @A(name = "s_a")//
				short s, //
				@A(name = "d_a")//
				double d, //
				float f) {

		}
	}

	@Inherited
	@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface A {
		String name();
	}

	@Inherited
	@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface B {
		String name();
	}

}
