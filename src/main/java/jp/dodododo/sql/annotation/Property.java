package jp.dodododo.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.dodododo.sql.access.AccessMode;
import jp.dodododo.sql.commons.Bool;

/**
 *
 * @author Satoshi Kimura
 */
@Inherited
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
	AccessMode value() default AccessMode.READ_WRITE;

	Class<? extends Throwable>[] ignoreExceptions() default {};
}
