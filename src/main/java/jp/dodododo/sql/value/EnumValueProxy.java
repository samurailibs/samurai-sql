package jp.dodododo.sql.value;

import jp.dodododo.sql.util.EnumConverter;

public class EnumValueProxy extends ValueProxy {

	public EnumValueProxy(Enum<?> data) {
		super(Objects.UNDEFINE);
		this.target = data;
	}

	@Override
	public Object getValue() {
		if (this.value != Objects.UNDEFINE) {
			return this.value;
		}
		Enum<?> enumValue = Enum.class.cast(target);
		Object value = EnumConverter.toNumberByAnnotationOrInterface(enumValue);
		if (value == null) {
			value = EnumConverter.toStringByAnnotationOrInterface(enumValue);
		}
		if (value == null) {
			value = enumValue;
		}
		this.value = value;
		return this.value;
	}
}
