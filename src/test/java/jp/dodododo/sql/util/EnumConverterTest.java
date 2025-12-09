package jp.dodododo.sql.util;

import java.lang.annotation.ElementType;

import jp.dodododo.sql.annotation.NumKey;
import jp.dodododo.sql.annotation.StringKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumConverterTest {

	@Test
	public void testConvert() {

		ElementType result4 = EnumConverter.convert(9, ElementType.class);
		assertSame(ElementType.TYPE_USE, result4);

		try {
			EnumConverter.convert(100, ElementType.class);
			fail();
		} catch (IllegalArgumentException success) {
		}

		ElementType result5 = EnumConverter.convert("LOCAL_VARIABLE", ElementType.class);
		assertSame(ElementType.LOCAL_VARIABLE, result5);

		StringEnum result6 = EnumConverter.convert("cc", StringEnum.class);
		assertSame(StringEnum.C, result6);

		NumEnum result7 = EnumConverter.convert(200, NumEnum.class);
		assertSame(NumEnum.B, result7);

		NumEnum result8 = EnumConverter.convert("200", NumEnum.class);
		assertSame(NumEnum.B, result8);
	}

	@Test
	public void testNull() {
		assertSame(NumContainsNull.A, EnumConverter.convert(1, NumContainsNull.class));
		assertSame(NumContainsNull.B, EnumConverter.convert(2, NumContainsNull.class));
		assertSame(NumContainsNull.Z, EnumConverter.convert(null, NumContainsNull.class));

		assertSame(StrContainsNull.A, EnumConverter.convert("1", StrContainsNull.class));
		assertSame(StrContainsNull.B, EnumConverter.convert("2", StrContainsNull.class));
		assertSame(StrContainsNull.Z, EnumConverter.convert(null, StrContainsNull.class));

		assertNull(EnumConverter.convert(null, NumEnum.class));
		assertNull(EnumConverter.convert(null, StringEnum.class));
		assertNull(EnumConverter.convert(null, ElementType.class));
	}

	public static enum NumContainsNull {
		A(1), B(2), Z(null);

		private Integer num;

		private NumContainsNull(Integer num) {
			this.num = num;
		}

		@NumKey
		public Integer getNum() {
			return num;
		}
	}

	public static enum StrContainsNull {
		A("1"), B("2"), Z(null);

		private String str;

		private StrContainsNull(String str) {
			this.str = str;
		}

		@StringKey
		public String getStr() {
			return str;
		}
	}

	public static enum StringEnum {
		A("aa"), B("bb"), C("cc");

		private String str;

		private StringEnum(String str) {
			this.str = str;
		}

		@jp.dodododo.sql.annotation.StringKey
		public String str() {
			return str;
		}
	}

	public static enum NumEnum {
		A(100), B(200), C(300);

		private long num;

		private NumEnum(long num) {
			this.num = num;
		}

		@NumKey
		public double num() {
			return num;
		}
	}
}
