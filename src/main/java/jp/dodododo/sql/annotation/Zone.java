package jp.dodododo.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Satoshi Kimura
 */
@Inherited
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Zone {
	/**
	 * ex: +9 , Asia/Tokyo, or JST.
	 */
	String id() default "";

	/**
	 * ex: +09:00
	 */
	String offset() default "";

	String idPropertyName() default "";

	String offsetPropertyName() default "";

	String idColumnName() default "";

	String offsetColumnName() default "";
}
