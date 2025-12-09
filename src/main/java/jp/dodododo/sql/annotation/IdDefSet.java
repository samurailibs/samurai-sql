package jp.dodododo.sql.annotation;

import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.Default;
import jp.dodododo.sql.id.IdGenerator;

public @interface IdDefSet {
	Class<? extends IdGenerator> strategy();

	String name() default "";

	Class<? extends Dialect> db() default Default.class;
}
