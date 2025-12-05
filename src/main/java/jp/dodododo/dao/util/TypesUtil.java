package jp.dodododo.dao.util;

import static jp.dodododo.dao.types.SQLTypes.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import jp.dodododo.dao.id.EntityId;
import jp.dodododo.dao.types.JavaType;
import jp.dodododo.dao.types.JavaTypes;
import jp.dodododo.dao.types.SQLType;
import jp.dodododo.dao.types.SQLTypes;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class TypesUtil {

	private static final Map<Class<?>, SQLType> SQL_TYPES = new HashMap<>();

	private static final Map<Class<?>, JavaType<?>> JAVA_TYPES = new HashMap<>();

	static {
		addType(EntityId.class, SQLTypes.OBJECT, JavaTypes.ENTITY_ID);
	}

	static {
		addType(Object.class, SQLTypes.OBJECT, JavaTypes.OBJECT);
	}

	static {
		addType(CharSequence.class, STRING, JavaTypes.STRING);
		addType(String.class, STRING, JavaTypes.STRING);
		addType(StringBuilder.class, STRING, JavaTypes.STRING_BUILDER);
		addType(StringBuffer.class, STRING, JavaTypes.STRING_BUFFER);
		addType(CharBuffer.class, STRING, JavaTypes.CHAR_BUFFER);
	}

	static {
		addType(char.class, STRING, JavaTypes.CHARACTER);
		addType(Character.class, STRING, JavaTypes.CHARACTER);
	}

	static {
		 addType(boolean.class, BOOLEAN, JavaTypes.PRIMITIVE_BOOLEAN);
		 addType(byte.class, NUMBER, JavaTypes.PRIMITIVE_BYTE);
		 addType(short.class, NUMBER, JavaTypes.PRIMITIVE_SHORT);
		 addType(int.class, NUMBER, JavaTypes.PRIMITIVE_INT);
		 addType(long.class, NUMBER, JavaTypes.PRIMITIVE_LONG);
		 addType(float.class, NUMBER, JavaTypes.PRIMITIVE_FLOAT);
		 addType(double.class, NUMBER, JavaTypes.PRIMITIVE_DOUBLE);

		 addType(Boolean.TYPE, BOOLEAN, JavaTypes.PRIMITIVE_BOOLEAN);
		 addType(Byte.TYPE, NUMBER, JavaTypes.PRIMITIVE_BYTE);
		 addType(Short.TYPE, NUMBER, JavaTypes.PRIMITIVE_SHORT);
		 addType(Integer.TYPE, NUMBER, JavaTypes.PRIMITIVE_INT);
		 addType(Long.TYPE, NUMBER, JavaTypes.PRIMITIVE_LONG);
		 addType(Float.TYPE, NUMBER, JavaTypes.PRIMITIVE_FLOAT);
		 addType(Double.TYPE, NUMBER, JavaTypes.PRIMITIVE_DOUBLE);

//		addType(boolean.class, BOOLEAN, JavaTypes.BOOLEAN);
//		addType(byte.class, NUMBER, JavaTypes.BYTE);
//		addType(short.class, NUMBER, JavaTypes.SHORT);
//		addType(int.class, NUMBER, JavaTypes.INTEGER);
//		addType(long.class, NUMBER, JavaTypes.LONG);
//		addType(float.class, NUMBER, JavaTypes.FLOAT);
//		addType(double.class, NUMBER, JavaTypes.DOUBLE);
	}
	static {
		addType(Byte.class, NUMBER, JavaTypes.BYTE);
		addType(Short.class, NUMBER, JavaTypes.SHORT);
		addType(Integer.class, NUMBER, JavaTypes.INTEGER);
		addType(Long.class, NUMBER, JavaTypes.LONG);
		addType(Float.class, NUMBER, JavaTypes.FLOAT);
		addType(Double.class, NUMBER, JavaTypes.DOUBLE);
	}

	static {
		addType(BigDecimal.class, NUMBER, JavaTypes.BIG_DECIMAL);
		addType(BigInteger.class, NUMBER, JavaTypes.BIG_INTEGER);
	}

	static {
		addType(AtomicInteger.class, NUMBER, JavaTypes.ATOMIC_INTEGER);
		addType(AtomicLong.class, NUMBER, JavaTypes.ATOMIC_LONG);
	}

	static {
		addType(Boolean.class, BOOLEAN, JavaTypes.BOOLEAN);
	}

	static {
		addType(java.sql.Date.class, SQL_DATE, JavaTypes.SQL_DATE);
		addType(java.sql.Time.class, TIME, JavaTypes.TIME);

		addType(Timestamp.class, TIMESTAMP, JavaTypes.TIMESTAMP);
		addType(java.util.Date.class, DATE, JavaTypes.DATE);
		addType(Calendar.class, TIMESTAMP, JavaTypes.CALENDAR);
	}

	static {
		addType(Instant.class, TIMESTAMP, JavaTypes.INSTANT);
		addType(LocalDate.class, SQL_DATE, JavaTypes.LOCAL_DATE);
		addType(LocalDateTime.class, TIMESTAMP, JavaTypes.LOCAL_DATE_TIME);
		addType(LocalTime.class, OBJECT, JavaTypes.LOCAL_TIME);
		addType(MonthDay.class, OBJECT, JavaTypes.MONTH_DAY);
		addType(OffsetDateTime.class, TIMESTAMP, JavaTypes.OFFSET_DATE_TIME);
		addType(OffsetTime.class, OBJECT, JavaTypes.OFFSET_TIME);
		addType(Year.class, OBJECT, JavaTypes.YEAR);
		addType(YearMonth.class, OBJECT, JavaTypes.YEAR_MONTH);
		addType(ZonedDateTime.class, TIMESTAMP, JavaTypes.ZONED_DATE_TIME);

		addType(ZoneId.class, OBJECT, JavaTypes.ZONE_ID);
		addType(ZoneOffset.class, OBJECT, JavaTypes.ZONE_OFFSET);
	}

	static {
		addType(byte[].class, BINARY, JavaTypes.BYTE_ARRAY);
		addType(InputStream.class, BINARY, JavaTypes.INPUT_STREAM);
		addType(Blob.class, BLOB, JavaTypes.BLOB);
		addType(Clob.class, CLOB, JavaTypes.CLOB);
		addType(NClob.class, NCLOB, JavaTypes.NCLOB);
	}

	static {
		try {
			addType(SQLXML.class, SQLXML, JavaTypes.SQLXML);
		} catch (NoClassDefFoundError ignore) {
		}
	}

	static {
		addType(null, NULL, JavaTypes.NULL);
	}

	static {
		addType(Collection.class, null, JavaTypes.COLLECTION);
		addType(Map.class, null, JavaTypes.MAP);
	}

	public static void addType(Class<?> clazz, SQLType sqlType, JavaType<?> javaType) {
		if (SQL_TYPES.containsKey(clazz) == false) {
			SQL_TYPES.put(clazz, sqlType);
		}
		if (JAVA_TYPES.containsKey(clazz) == false) {
			JAVA_TYPES.put(clazz, javaType);
		}
	}

	public static SQLType getSQLType(int type) {
		switch (type) {
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return NUMBER;
		case Types.CHAR:
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
		case Types.NCHAR:
		case Types.DATALINK:
			return STRING;
		case Types.DATE:
			return SQL_DATE;
		case Types.TIME:
			return TIME;
		case Types.TIMESTAMP:
			return TIMESTAMP;
		case Types.BOOLEAN:
		case Types.BIT:
			return BOOLEAN;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			return BINARY;
		case Types.BLOB:
			return BLOB;
		case Types.CLOB:
			return CLOB;
		case Types.NCLOB:
			return NCLOB;
		case Types.JAVA_OBJECT:
		case Types.SQLXML:
			return SQLXML;
		case Types.STRUCT:
			return STRUCT;
		case Types.ARRAY:
			return ARRAY;
		case Types.REF:
			return REF;
		case Types.ROWID:
			return ROW_ID;
		case Types.DISTINCT:
		case Types.OTHER:
			return OBJECT;
		default:
			return OBJECT;
		}
	}

	public static SQLType getSQLType(Object value) {
		if (value == null) {
			return NULL;
		}
		return getSQLType(value.getClass());
	}

	public static SQLType getSQLType(Class<?> clazz) {
		SQLType type = SQL_TYPES.get(clazz);
		if (type != null) {
			return type;
		}
		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces != null) {
			for (Class<?> interfaceClass : interfaces) {
				type = getSQLType(interfaceClass);
				if (type != null) {
					SQL_TYPES.put(clazz, type);
					return type;
				}
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null) {
			SQL_TYPES.put(clazz, OBJECT);
			return OBJECT;
		} else {
			SQLType sqlType = getSQLType(superClass);
			SQL_TYPES.put(clazz, sqlType);
			return sqlType;
		}
	}

	public static JavaType<?> getJavaType(Class<?> clazz) {
		JavaType<?> ret = JAVA_TYPES.get(clazz);
		if (ret != null) {
			return ret;
		}
		if (clazz.isEnum() == true) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			JavaType<?> type = new JavaTypes.EnumType(clazz);
			JAVA_TYPES.put(clazz, type);
			return type;
		}
		if (clazz.isArray() == true) {
			JAVA_TYPES.put(clazz, JavaTypes.ARRAY);
			return JavaTypes.ARRAY;
		}
		if (Number.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.NUMBER);
			return JavaTypes.NUMBER;
		}
		if (Collection.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.COLLECTION);
			return JavaTypes.COLLECTION;
		}
		if (Map.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.MAP);
			return JavaTypes.MAP;
		}
		if (Class.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.CLASS);
			return JavaTypes.CLASS;
		}
		if (ClassLoader.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.CLASS_LOADER);
			return JavaTypes.CLASS_LOADER;
		}
		if (Constructor.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.CONSTRUCTOR);
			return JavaTypes.CONSTRUCTOR;
		}
		if (Method.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.METHOD);
			return JavaTypes.METHOD;
		}
		if (Field.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.FIELD);
			return JavaTypes.FIELD;
		}
		if (Annotation.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.ANNOTATION);
			return JavaTypes.ANNOTATION;
		}
		if (Connection.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.CONNECTION);
			return JavaTypes.CONNECTION;
		}
		if (DataSource.class.isAssignableFrom(clazz)) {
			JAVA_TYPES.put(clazz, JavaTypes.DATA_SOURCE);
			return JavaTypes.DATA_SOURCE;
		}

		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces != null) {
			for (Class<?> interfaceClass : interfaces) {
				JavaType<?> type = getJavaType(interfaceClass);
				if (type != null) {
					JAVA_TYPES.put(clazz, type);
					return type;
				}
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null) {
			JAVA_TYPES.put(clazz, JavaTypes.OBJECT);
			return JavaTypes.OBJECT;
		} else {
			JavaType<?> javaType = getJavaType(superClass);
			JAVA_TYPES.put(clazz, javaType);
			return javaType;
		}
	}


	private static final Map<Class<?>, Class<?>> wrapperTypes = new HashMap<>();

	static {
		wrapperTypes.put(boolean.class, Boolean.class);
		wrapperTypes.put(byte.class, Byte.class);
		wrapperTypes.put(short.class, Short.class);
		wrapperTypes.put(int.class, Integer.class);
		wrapperTypes.put(long.class, Long.class);
		wrapperTypes.put(float.class, Float.class);
		wrapperTypes.put(double.class, Double.class);
		wrapperTypes.put(char.class, Character.class);
	}

	public static Class<?> toWrapperType(Class<?> type) {
		if (type.isPrimitive() == false) {
			return type;
		}
		return wrapperTypes.get(type);
	}

}
