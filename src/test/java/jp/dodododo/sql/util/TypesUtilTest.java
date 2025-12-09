package jp.dodododo.sql.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import jp.dodododo.sql.id.EntityId;
import jp.dodododo.sql.types.JavaType;
import jp.dodododo.sql.types.JavaTypes;

import org.junit.jupiter.api.Test;

public class TypesUtilTest {

	@Test
	public void testGetJavaType() {
		JavaType<?> actual = TypesUtil.getJavaType(String[].class);
		assertEquals(JavaTypes.ARRAY, actual);

		actual = TypesUtil.getJavaType(CharSequence2.class);
		assertEquals(JavaTypes.STRING, actual);

		actual = TypesUtil.getJavaType(ArrayList.class);
		assertEquals(JavaTypes.COLLECTION, actual);

		actual = TypesUtil.getJavaType(HashMap.class);
		assertEquals(JavaTypes.MAP, actual);

		actual = TypesUtil.getJavaType(FileInputStream.class);
		assertEquals(JavaTypes.INPUT_STREAM, actual);

		actual = TypesUtil.getJavaType(Class.class);
		assertEquals(JavaTypes.CLASS, actual);

		actual = TypesUtil.getJavaType(URLClassLoader.class);
		assertEquals(JavaTypes.CLASS_LOADER, actual);

		actual = TypesUtil.getJavaType(Thread.State.class);
		assertEquals(new JavaTypes.EnumType<>(Thread.State.class), actual);

		actual = TypesUtil.getJavaType(EntityId.class);
		assertEquals(JavaTypes.ENTITY_ID, actual);

		actual = TypesUtil.getJavaType(TypesUtil.class);
		assertEquals(JavaTypes.OBJECT, actual);
		actual = TypesUtil.getJavaType(Object.class);
		assertEquals(JavaTypes.OBJECT, actual);
	}

	public abstract class CharSequence2 implements CharSequence {

	}

}
