package jp.dodododo.sql.types;

public class Value {
	protected Object value;

	public Value(Object value) {
		this.value = value;
	}

	public <T> T to(Class<T> dest) {
		return TypeConverter.convert(this.value, dest);
	}

	public <T> T as(Class<T> dest) {
		return to(dest);
	}
}
