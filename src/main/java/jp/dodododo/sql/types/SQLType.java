package jp.dodododo.sql.types;

public interface SQLType {
	int getType();

	Object convert(Object value);

	Object convert(Object value, String... fromats);

	JavaType<?> toJavaType();

	boolean isBinary();
}
