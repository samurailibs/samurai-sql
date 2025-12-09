package jp.dodododo.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.dodododo.sql.compress.CompressType;
import jp.dodododo.sql.compress.TmpDataMode;

@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Compress {

	/**
	 * default is {@link CompressType#GZIP}
	 */
	CompressType compressType() default CompressType.GZIP;

	/**
	 * default is true
	 */
	boolean autoUncompress() default true;

	/**
	 * default is 8192
	 */
	int bufferSize() default 8192;

	/**
	 * default is {@link TmpDataMode#FILE}
	 */
	TmpDataMode tmpDataMode() default TmpDataMode.FILE;
}
