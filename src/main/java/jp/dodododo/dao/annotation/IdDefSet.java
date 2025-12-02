package jp.dodododo.dao.annotation;

import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.Default;
import jp.dodododo.dao.id.IdGenerator;

public @interface IdDefSet {
	Class<? extends IdGenerator> strategy();

	String name() default "";

	Class<? extends Dialect> db() default Default.class;
}
