package jp.dodododo.sql.annotation;

public @interface Rel {
	String table();

	String column();

	String property();
}
