package jp.dodododo.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.dodododo.sql.commons.Null;

@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

	Class<?> value() default Null.class;

	Class<?> defaultBeanClass() default Null.class;

	boolean nullable() default false;

	String createMethod() default "";

	String property() default "";
}
