package jp.dodododo.sql.types;

import static jp.dodododo.janerics.GenericsTypeUtil.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.flyweight.FlyweightFactory;
import jp.dodododo.sql.id.EntityId;
import jp.dodododo.sql.util.BigDecimalUtil;
import jp.dodododo.sql.util.CloseableUtil;
import jp.dodododo.sql.util.EmptyUtil;
import jp.dodododo.sql.util.EnumConverter;
import jp.dodododo.sql.util.FieldUtil;
import jp.dodododo.sql.util.FileInputStreamUtil;
import jp.dodododo.sql.util.IOUtil;
import jp.dodododo.sql.util.StringUtil;
import jp.dodododo.sql.util.TimeUtil;
import jp.dodododo.sql.util.URIUtil;
import jp.dodododo.sql.util.URLUtil;
import jp.dodododo.sql.util.ZoneUtil;

public class JavaTypes<T> implements JavaType<T> {

	public static final JavaType<String> STRING = new JavaTypes<>() {
		@Override
		protected String doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getString(columnIndex);
		}

		@Override
		protected String doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getString(columnLabel);
		}

		@Override
		protected String doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected  String doConvert(Object value, String... formats) {
			if (value == null) {
				return null;
			}
			if (value instanceof Enum) {
				value = EnumConverter.toString((Enum<?>) value);
			} else if (value instanceof Number) {
				if(EmptyUtil.isNotEmpty(formats)) {
					return new DecimalFormat(formats[0]).format(value);
				}
				return value.toString();
			} else if (value instanceof Date) {
				if(EmptyUtil.isNotEmpty(formats)) {
					return new SimpleDateFormat(formats[0]).format(value);
				}
				return DATE.convert(value).toString();
			} else if (value instanceof Calendar) {
				return convert(DATE.convert(value), formats);
			} else if (value instanceof Class) {
				return Class.class.cast(value).getName();
			} else if (value instanceof byte[]) {
				return StringUtil.newString((byte[]) value, "UTF-8");
			} else if (value instanceof Byte[]) {
				return StringUtil.newString(BYTE_ARRAY.convert(value), "UTF-8");
			} else if (value instanceof InputStream) {
				return doConvert(BYTE_ARRAY.convert(value));
			}
			return value.toString();
		}
	};

	public static final JavaType<StringBuffer> STRING_BUFFER = new JavaTypes<>() {
		@Override
		protected StringBuffer doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return new StringBuffer(string);
		}

		@Override
		protected StringBuffer doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return new StringBuffer(string);
		}

		@Override
		protected StringBuffer doConvert(Object value) {
			if (value == null) {
				return null;
			}
			return new StringBuffer(STRING.convert(value));
		}
	};

	public static final JavaType<StringBuilder> STRING_BUILDER = new JavaTypes<>() {
		@Override
		protected StringBuilder doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return new StringBuilder(string);
		}

		@Override
		protected StringBuilder doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return new StringBuilder(string);
		}

		@Override
		protected StringBuilder doConvert(Object value) {
			if (value == null) {
				return null;
			}
			return new StringBuilder(STRING.convert(value));
		}
	};

	public static final JavaType<CharBuffer> CHAR_BUFFER = new JavaTypes<>() {
		@Override
		protected CharBuffer doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return CharBuffer.wrap(string);
		}

		@Override
		protected CharBuffer doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return CharBuffer.wrap(string);
		}

		@Override
		protected CharBuffer doConvert(Object value) {
			if (value == null) {
				return null;
			}
			return CharBuffer.wrap(STRING.convert(value));
		}
	};

	public static final JavaType<Character> CHARACTER = new JavaTypes<>() {
		@Override
		protected Character doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return string.charAt(0);
		}

		@Override
		protected Character doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return string.charAt(0);
		}

		@Override
		protected Character doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Enum) {
				Enum<?> e = (Enum<?>) value;
				value = EnumConverter.toString(e);
			} else if (value instanceof Class) {
				return convert(Class.class.cast(value).getSimpleName());
			}
			return STRING.convert(value).charAt(0);
		}
	};

	//
	public static final JavaType<Character> PRIMITIVE_CHARACTER = new JavaTypes<>() {
		@Override
		protected Character doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Character v = CHARACTER.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Character doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Character v = CHARACTER.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Character doConvert(Object value) {
			Character v = CHARACTER.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Character> getWrapperType() {
			return Character.class;
		}
	};

	private static Map<String, Boolean> STRING_BOOLEAN_TABLE = new HashMap<>();
	static {
		STRING_BOOLEAN_TABLE.put("yes", true);
		STRING_BOOLEAN_TABLE.put("no", false);
		STRING_BOOLEAN_TABLE.put("on", true);
		STRING_BOOLEAN_TABLE.put("off", false);
		STRING_BOOLEAN_TABLE.put("t", true);
		STRING_BOOLEAN_TABLE.put("f", false);
		STRING_BOOLEAN_TABLE.put("1", true);
		STRING_BOOLEAN_TABLE.put("0", false);
		STRING_BOOLEAN_TABLE.put(Boolean.TRUE.toString().toLowerCase(), true);
		STRING_BOOLEAN_TABLE.put(Boolean.FALSE.toString().toLowerCase(), false);
	}
	//
	public static final JavaType<Boolean> BOOLEAN = new JavaTypes<>() {

		@Override
		protected Boolean doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getBoolean(columnIndex);
		}

		@Override
		protected Boolean doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getBoolean(columnLabel);
		}

		@Override
		protected Boolean doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Boolean) {
				return (Boolean) value;
			}
			if (value instanceof Enum) {
				Enum<?> e = (Enum<?>) value;
				value = EnumConverter.toNumber(e);
			}
			if (value instanceof Number) {
				Number num = (Number) value;
				if (num.intValue() == 0) {
					return false;
				} else if (num.intValue() == 1) {
					return true;
				}
			}
			if (value instanceof CharSequence || value instanceof Character) {
				Boolean ret = STRING_BOOLEAN_TABLE.get(value.toString().toLowerCase());
				if (ret != null) {
					return ret;
				}
				try {
					Number num = toBigDecimal(value.toString());
					return convert(num);
				} catch (NumberFormatException ignore) {
				}
				return toBoolean(value.toString());
			}
			if (value instanceof InputStream) {
				return convert(NUMBER.convert(value));
			}
			return super.convert(value);
		}
	};

	//
	public static final JavaType<Boolean> PRIMITIVE_BOOLEAN = new JavaTypes<>() {
		@Override
		protected Boolean doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Boolean v = BOOLEAN.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Boolean doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Boolean v = BOOLEAN.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Boolean doConvert(Object value) {
			Boolean v = BOOLEAN.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Boolean> getWrapperType() {
			return Boolean.class;
		}
	};

	//
	public static final JavaType<Byte> BYTE = new JavaTypes<>() {
		@Override
		protected Byte doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.byteValue();
		}

		@Override
		protected Byte doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.byteValue();
		}

		@Override
		protected Byte doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.byteValue();
		}
	};

	//
	public static final JavaType<Byte> PRIMITIVE_BYTE = new JavaTypes<>() {
		@Override
		protected Byte doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Byte v = BYTE.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Byte doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Byte v = BYTE.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Byte doConvert(Object value) {
			Byte v = BYTE.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Byte> getWrapperType() {
			return Byte.class;
		}
	};

	//
	public static final JavaType<Integer> INTEGER = new JavaTypes<>() {
		@Override
		protected Integer doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.intValue();
		}

		@Override
		protected Integer doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.intValue();
		}

		@Override
		protected Integer doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.intValue();
		}
	};
	//
	public static final JavaType<Integer> PRIMITIVE_INT = new JavaTypes<>() {
		@Override
		protected Integer doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Integer v = INTEGER.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Integer doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Integer v = INTEGER.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Integer doConvert(Object value) {
			if (value == null) {
				value = 0;
			}
			Integer v = INTEGER.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Integer> getWrapperType() {
			return Integer.class;
		}
	};
	//
	public static final JavaType<BigDecimal> BIG_DECIMAL = new JavaTypes<>() {
		@Override
		protected BigDecimal doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			return num;
		}

		@Override
		protected BigDecimal doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			return num;
		}

		@Override
		protected BigDecimal doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Enum) {
				Enum<?> e = (Enum<?>) value;
				value = EnumConverter.toNumber(e);
			}
			if (value instanceof Date) {
				return toBigDecimal(((Date) value).getTime());
			}
			if (value instanceof Boolean) {
				return ((Boolean) value) ? toBigDecimal(1) : toBigDecimal(0);
			}
			if (value instanceof Calendar) {
				return convert(JavaTypes.DATE.convert(value));
			}
			if (value instanceof InputStream) {
				return convert(JavaTypes.STRING.convert(value));
			}
			return toBigDecimal(value.toString());
		}
	};

	//
	public static final JavaType<BigInteger> BIG_INTEGER = new JavaTypes<>() {
		@Override
		protected BigInteger doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return BigInteger.valueOf(num.longValue());
		}

		@Override
		protected BigInteger doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return BigInteger.valueOf(num.longValue());
		}

		@Override
		protected BigInteger doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return BigInteger.valueOf(num.longValue());
		}
	};

	//
	public static final JavaType<Double> DOUBLE = new JavaTypes<Double>() {
		@Override
		protected Double doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.doubleValue();
		}

		@Override
		protected Double doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.doubleValue();
		}

		@Override
		protected Double doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.doubleValue();
		}
	};

	//
	public static final JavaType<Double> PRIMITIVE_DOUBLE = new JavaTypes<Double>() {
		@Override
		protected Double doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Double v = DOUBLE.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Double doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Double v = DOUBLE.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Double doConvert(Object value) {
			Double v = DOUBLE.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Double> getWrapperType() {
			return Double.class;
		}
	};

	//
	public static final JavaType<Float> FLOAT = new JavaTypes<Float>() {
		@Override
		protected Float doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.floatValue();
		}

		@Override
		protected Float doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.floatValue();
		}

		@Override
		protected Float doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.floatValue();
		}
	};

	//
	public static final JavaType<Float> PRIMITIVE_FLOAT = new JavaTypes<Float>() {
		@Override
		protected Float doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Float v = FLOAT.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Float doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Float v = FLOAT.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Float doConvert(Object value) {
			Float v = FLOAT.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Float> getWrapperType() {
			return Float.class;
		}
	};

	//
	public static final JavaType<Long> LONG = new JavaTypes<Long>() {
		@Override
		protected Long doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.longValue();
		}

		@Override
		protected Long doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.longValue();
		}

		@Override
		protected Long doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.longValue();
		}
	};

	//
	public static final JavaType<Long> PRIMITIVE_LONG = new JavaTypes<Long>() {
		@Override
		protected Long doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Long v = LONG.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Long doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Long v = LONG.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Long doConvert(Object value) {
			Long v = LONG.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Long> getWrapperType() {
			return Long.class;
		}
	};

	//
	public static final JavaType<Number> NUMBER = new JavaTypes<Number>() {
		@Override
		protected Number doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			return num;
		}

		@Override
		protected Number doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			return num;
		}

		@Override
		protected Number doConvert(Object value) {
			BigDecimal num = BIG_DECIMAL.convert(value);
			if (num == null) {
				return null;
			}
			return num;
		}
	};

	//
	public static final JavaType<Short> SHORT = new JavaTypes<Short>() {
		@Override
		protected Short doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return num.shortValue();
		}

		@Override
		protected Short doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return num.shortValue();
		}

		@Override
		protected Short doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return num.shortValue();
		}
	};

	//
	public static final JavaType<Short> PRIMITIVE_SHORT = new JavaTypes<Short>() {
		@Override
		protected Short doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Short v = SHORT.getValue(rs, columnIndex);
			return toPrimitive(v);
		}

		@Override
		protected Short doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Short v = SHORT.getValue(rs, columnLabel);
			return toPrimitive(v);
		}

		@Override
		protected Short doConvert(Object value) {
			if (value == null) {
				value = 0;
			}
			Short v = SHORT.convert(value);
			return toPrimitive(v);
		}

		@Override
		public Class<Short> getWrapperType() {
			return Short.class;
		}
	};

	//
	public static final JavaType<AtomicInteger> ATOMIC_INTEGER = new JavaTypes<AtomicInteger>() {
		@Override
		protected AtomicInteger doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return new AtomicInteger(num.intValue());
		}

		@Override
		protected AtomicInteger doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return new AtomicInteger(num.intValue());
		}

		@Override
		protected AtomicInteger doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return new AtomicInteger(num.intValue());
		}
	};

	//
	public static final JavaType<AtomicLong> ATOMIC_LONG = new JavaTypes<AtomicLong>() {
		@Override
		protected AtomicLong doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnIndex);
			if (num == null) {
				return null;
			}
			return new AtomicLong(num.longValue());
		}

		@Override
		protected AtomicLong doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			BigDecimal num = rs.getBigDecimal(columnLabel);
			if (num == null) {
				return null;
			}
			return new AtomicLong(num.longValue());
		}

		@Override
		protected AtomicLong doConvert(Object value) {
			Number num = NUMBER.convert(value);
			if (num == null) {
				return null;
			}
			return new AtomicLong(num.longValue());
		}
	};

	//
	public static final JavaType<Object> OBJECT = new JavaTypes<Object>() {
		@Override
		protected Object doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getObject(columnIndex);
		}

		@Override
		protected Object doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getObject(columnLabel);
		}

		@Override
		protected Object doConvert(Object value) {
			return value;
		}
	};

	//
	public static final JavaType<java.sql.Date> SQL_DATE = new JavaTypes<java.sql.Date>() {
		@Override
		protected java.sql.Date doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			java.sql.Date date = rs.getDate(columnIndex);
			return date;
		}

		@Override
		protected java.sql.Date doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			java.sql.Date date = rs.getDate(columnLabel);
			return date;
		}

		@Override
		protected java.sql.Date doConvert(Object value, String... formats) {
			if (value == null) {
				return null;
			}
			if (value instanceof java.sql.Date) {
				return (java.sql.Date) value;
			}
			Date date = DATE.convert(value, formats);
			return new java.sql.Date(date.getTime());
		}

		@Override
		protected java.sql.Date doConvert(Object value) {
			return convert(value, new String[0]);
		}
	};

	//
	public static final JavaType<Time> TIME = new JavaTypes<Time>() {
		@Override
		protected Time doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Time time = rs.getTime(columnIndex);
			return time;
		}

		@Override
		protected Time doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Time time = rs.getTime(columnLabel);
			return time;
		}

		@Override
		protected Time doConvert(Object value, String... formats) {
			if (value == null) {
				return null;
			}
			if (value instanceof Time) {
				return (Time) value;
			}
			Date date = DATE.convert(value, formats);
			return new Time(date.getTime());
		}

		@Override
		protected Time doConvert(Object value) {
			return convert(value, new String[0]);
		}
	};

	//
	public static final JavaType<Timestamp> TIMESTAMP = new JavaTypes<Timestamp>() {
		@Override
		protected Timestamp doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getTimestamp(columnIndex);
		}

		@Override
		protected Timestamp doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getTimestamp(columnLabel);
		}

		@Override
		protected Timestamp doConvert(Object value, String... formats) {
			if (value == null) {
				return null;
			}
			if (value instanceof Timestamp) {
				return (Timestamp) value;
			}
			Date date = DATE.convert(value, formats);
			return new Timestamp(date.getTime());
		}

		@Override
		protected Timestamp doConvert(Object value) {
			return doConvert(value, new String[0]);
		}
	};

	//
	public static final JavaType<Calendar> CALENDAR = new JavaTypes<Calendar>() {
		@Override
		protected Calendar doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			if (timestamp == null) {
				return null;
			}
			return doConvert(timestamp);
		}

		@Override
		protected Calendar doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			if (timestamp == null) {
				return null;
			}
			return doConvert(timestamp);
		}

		@Override
		protected Calendar doConvert(Object value) {
			return convert(value, new String[0]);
		}

		@Override
		protected Calendar doConvert(Object value, String... formats) {
			if (value == null) {
				return null;
			}
			if (value instanceof Calendar) {
				return (Calendar) value;
			}
			if (value instanceof Date) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime((Date) value);
				return calendar;
			}
			Date date = DATE.convert(value, formats);
			return convert(date);
		}
	};

	//
	public static final JavaType<java.util.Date> DATE = new JavaTypes<java.util.Date>() {
		@Override
		protected java.util.Date doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected java.util.Date doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected java.util.Date doConvert(Object value, String... formats) {
			return toDate(value, true, formats);
		}

		/**
		 * @deprecated use {@link #doConvert(Object, String...)} .
		 */
		@Deprecated
		@Override
		protected java.util.Date doConvert(Object value) {
			return doConvert(value, SqlConfig.getDefaultConfig().getFormats());
		}
	};

	//
	public static final JavaType<Instant> INSTANT = new JavaTypes<Instant>() {
		@Override
		protected Instant doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected Instant doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected Instant doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected Instant doConvert(Object value, String... formats) {
			return toDate(value, true, formats).toInstant();
		}
	};
	//
	public static final JavaType<LocalDate> LOCAL_DATE = new JavaTypes<LocalDate>() {
		@Override
		protected LocalDate doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected LocalDate doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected LocalDate doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected LocalDate doConvert(Object value, String... formats) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		}
	};

	//
	public static final JavaType<LocalDateTime> LOCAL_DATE_TIME = new JavaTypes<LocalDateTime>() {
		@Override
		protected LocalDateTime doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected LocalDateTime doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, (String[]) null);
		}

		@Override
		protected LocalDateTime doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected LocalDateTime doConvert(Object value, String... formats) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),//
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),//
					calendar.get(Calendar.MILLISECOND) * 1000000// nanoOfSecond
					);
		}
	};

	//
	public static final JavaType<LocalTime> LOCAL_TIME = new JavaTypes<LocalTime>() {
		@Override
		protected LocalTime doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Object o = rs.getObject(columnIndex);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected LocalTime doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Object o = rs.getObject(columnLabel);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected LocalTime doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected LocalTime doConvert(Object value, String... formats) {
			if (value instanceof CharSequence) {
				if (formats == null) {
					formats = defaultFormats(new String[] { "HHmm", "HHmmss", "HHmmssSSS", "HH:mm:ss", "HH:mm:ss.SSS" });
				}
				for (String format : formats) {
					try {
						return LocalTime.parse((CharSequence) value, DateTimeFormatter.ofPattern(format));
					} catch (DateTimeParseException | IllegalArgumentException ignore) {
					}
				}
			}
			if (value instanceof Number) {
				String str = value.toString();
				if (str.length() == 3 || str.length() == 5) {
					str = "0" + str;
				}
				if (str.length() == 4) {
					return LocalTime.of(Integer.parseInt(str.substring(0, 2)), Integer.parseInt(str.substring(2, 4)));
				} else if (str.length() == 6) {
					return LocalTime.of(Integer.parseInt(str.substring(0, 2)), Integer.parseInt(str.substring(2, 4)),
							Integer.parseInt(str.substring(4, 6)));
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),//
					calendar.get(Calendar.MILLISECOND) * 1000000// nanoOfSecond
					);
		}
	};

	//
	public static final JavaType<Year> YEAR = new JavaTypes<Year>() {
		@Override
		protected Year doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Object o = rs.getObject(columnIndex);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected Year doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Object o = rs.getObject(columnLabel);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected Year doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected Year doConvert(Object value, String... formats) {
			if (value instanceof CharSequence) {
				if (formats == null) {
					formats = defaultFormats(new String[] { "yyyy", "uuuu", "yy", "uu" });
				}
				for (String format : formats) {
					try {
						return Year.parse((CharSequence) value, DateTimeFormatter.ofPattern(format));
					} catch (DateTimeParseException | IllegalArgumentException ignore) {
					}
				}
			}
			if (value instanceof Number) {
				return Year.of(((Number) value).intValue());
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return Year.of(calendar.get(Calendar.YEAR));
		}
	};

	//
	public static final JavaType<YearMonth> YEAR_MONTH = new JavaTypes<YearMonth>() {
		@Override
		protected YearMonth doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Object o = rs.getObject(columnIndex);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected YearMonth doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Object o = rs.getObject(columnLabel);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected YearMonth doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected YearMonth doConvert(Object value, String... formats) {
			if (value instanceof CharSequence) {
				if (formats == null) {
					formats = defaultFormats(TimeUtil.getYearMonthFormats());
				}
				for (String format : formats) {
					try {
						CharSequence text = (CharSequence) value;
						if (format.length() == text.length()) {
							return YearMonth.parse(text, DateTimeFormatter.ofPattern(format));
						}
					} catch (DateTimeParseException | IllegalArgumentException ignore) {
					}
				}
			}
			if (value instanceof Number) {
				String str = value.toString();
				if (str.length() == 3 || str.length() == 5) {
					str = "0" + str;
				}
				if (str.length() == 4) {
					return YearMonth.of(Integer.parseInt(str.substring(0, 2)), Integer.parseInt(str.substring(2, 4)));
				} else if (str.length() == 6) {
					return YearMonth.of(Integer.parseInt(str.substring(0, 4)), Integer.parseInt(str.substring(4, 6)));
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
		}
	};

	//
	public static final JavaType<MonthDay> MONTH_DAY = new JavaTypes<MonthDay>() {
		@Override
		protected MonthDay doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			Object o = rs.getObject(columnIndex);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected MonthDay doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			Object o = rs.getObject(columnLabel);
			return doConvert(o, (String[]) null);
		}

		@Override
		protected MonthDay doConvert(Object value) {
			return doConvert(value, (String[]) null);
		}

		@Override
		protected MonthDay doConvert(Object value, String... formats) {
			if (value instanceof CharSequence) {
				if (formats == null) {
					formats = defaultFormats(TimeUtil.getMonthDayFormats());
				}
				for (String format : formats) {
					try {
						CharSequence text = (CharSequence) value;
						if (format.length() == text.length()) {
							return MonthDay.parse(text, DateTimeFormatter.ofPattern(format, Locale.US));
						}
					} catch (DateTimeParseException | IllegalArgumentException ignore) {
					}
				}
			}
			if (value instanceof Number) {
				String str = value.toString();
				if (str.length() == 3) {
					str = "0" + str;
				}
				if (str.length() == 4) {
					return doConvert(str, formats);
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate(value, true, formats));
			return MonthDay.of(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		}
	};

	//
	public static final JavaType<OffsetDateTime> OFFSET_DATE_TIME = new JavaTypes<OffsetDateTime>() {
		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			throw new RuntimeException("ZoneId was null.");
		}

		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			throw new RuntimeException("ZoneId was null.");
		}

		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, zoneId, (String[]) null);
		}
		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
			return doGetValue(rs, columnIndex, (ZoneId) zoneOffset);
		}

		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, zoneId, (String[]) null);
		}

		@Override
		protected OffsetDateTime doGetValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
			return doGetValue(rs, columnLabel, (ZoneId) zoneOffset);
		}

		@Override
		protected OffsetDateTime doConvert(Object value) {
			return doConvert(value, (ZoneId) null, (String[]) null);
		}

		@Override
		protected OffsetDateTime doConvert(Object value, ZoneId zoneId , String... formats) {
			return OffsetDateTime.ofInstant(INSTANT.convert(value, formats), zoneId);
		}
	};

	//
	public static final JavaType<OffsetTime> OFFSET_TIME = new JavaTypes<OffsetTime>() {//
		@Override
		protected OffsetTime doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			throw new RuntimeException("ZoneOffset was null.");
		}

		@Override
		protected OffsetTime doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			throw new RuntimeException("ZoneOffset was null.");
		}

		@Override
		protected OffsetTime doGetValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, zoneOffset, (String[]) null);
		}
		@Override
		protected OffsetTime doGetValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
			return doGetValue(rs, columnIndex, ZoneUtil.zoneOffset(zoneId));
		}

		@Override
		protected OffsetTime doGetValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, zoneOffset, (String[]) null);
		}
		@Override
		protected OffsetTime doGetValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
			return doGetValue(rs, columnLabel, ZoneUtil.zoneOffset(zoneId));
		}

		@Override
		protected OffsetTime doConvert(Object value) {
			return doConvert(value, (ZoneOffset) null, (String[]) null);
		}

		@Override
		protected OffsetTime doConvert(Object value, ZoneOffset zoneOffset, String... formats) {
			return OffsetTime.of(LOCAL_TIME.convert(value, formats), zoneOffset);
		}
	};

	//
	public static final JavaType<ZonedDateTime> ZONED_DATE_TIME = new JavaTypes<ZonedDateTime>() {
		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			throw new RuntimeException("ZoneId was null.");
		}

		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			throw new RuntimeException("ZoneId was null.");
		}

		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnIndex);
			return doConvert(timestamp, zoneId, (String[]) null);
		}

		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
			return doGetValue(rs, columnIndex, (ZoneId) zoneOffset);
		}

		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(columnLabel);
			return doConvert(timestamp, zoneId, (String[]) null);
		}

		@Override
		protected ZonedDateTime doGetValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
			return doGetValue(rs, columnLabel, (ZoneId) zoneOffset);
		}

		@Override
		protected ZonedDateTime doConvert(Object value) {
			return doConvert(value, (ZoneId) null, (String[]) null);
		}

		@Override
		protected ZonedDateTime doConvert(Object value, ZoneId zoneId, String... formats) {
			return ZonedDateTime.ofInstant(INSTANT.convert(value, formats), zoneId);
		}
	};

	//
	public static final JavaType<ZoneId> ZONE_ID = new JavaTypes<ZoneId>() {
		@Override
		protected ZoneId doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof ZoneId) {
				return (ZoneId) value;
			}
			if (value instanceof Number) {
				String s = String.valueOf(((Number) value).intValue());
				if(!s.startsWith("-")) {
					s = "+"+ s;
				}
				return ZoneId.of(s);
			}
			return ZoneUtil.zoneId(value.toString());
		}
	};

	//
	public static final JavaType<ZoneOffset> ZONE_OFFSET = new JavaTypes<ZoneOffset>() {
		@Override
		protected ZoneOffset doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof ZoneOffset) {
				return (ZoneOffset) value;
			}
			if (value instanceof ZoneId) {
				return ZoneUtil.zoneOffset((ZoneId) value);
			}
			if (value instanceof Number) {
				return ZoneUtil.zoneOffset(ZONE_ID.convert(value));
			}
			try {
				return ZoneOffset.of(value.toString());
			} catch (DateTimeException e) {
				return ZoneUtil.zoneOffset(ZONE_ID.convert(value));
			}
		}
	};

	//
	public static final JavaType<InputStream> INPUT_STREAM = new JavaTypes<InputStream>() {
		@Override
		protected InputStream doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getBinaryStream(columnIndex);
		}

		@Override
		protected InputStream doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getBinaryStream(columnLabel);
		}

		@Override
		protected InputStream doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof InputStream) {
				return (InputStream) value;
			}
			if (value instanceof byte[]) {
				return new ByteArrayInputStream((byte[]) value);
			}
			if (value instanceof Byte) {
				return new ByteArrayInputStream(new byte[] { (Byte) value });
			}
			try {
				if (value instanceof Blob) {
					return ((Blob) value).getBinaryStream();
				}
				if (value instanceof NClob) {
					return ((NClob) value).getAsciiStream();
				}
				if (value instanceof java.sql.SQLXML) {
					return ((java.sql.SQLXML) value).getBinaryStream();
				}
			} catch (SQLException e) {
				throw new SQLRuntimeException(e);
			}
			if (value instanceof File) {
				return FileInputStreamUtil.newFileInputStream((File) value);
			}
			if (value instanceof URL) {
				return URLUtil.openStream(((URL) value));
			}
			if (value instanceof URI) {
				return URLUtil.openStream(URL.convert(value));
			}
			String str = STRING.convert(value);
			return new ByteArrayInputStream(BYTE_ARRAY.convert(str));
		}
	};

	//
	public static final JavaType<Blob> BLOB = new JavaTypes<Blob>() {
		@Override
		protected Blob doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getBlob(columnIndex);
		}

		@Override
		protected Blob doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getBlob(columnLabel);
		}

		@Override
		protected Blob doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Blob) {
				return (Blob) value;
			}
			return super.convert(value);
		}
	};

	//
	public static final JavaType<Clob> CLOB = new JavaTypes<Clob>() {
		@Override
		protected Clob doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getClob(columnIndex);
		}

		@Override
		protected Clob doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getClob(columnLabel);
		}

		@Override
		protected Clob doConvert(Object value) {
			if (value == null) {
				return null;
			}
			return (Clob) value;
		}
	};

	//
	public static final JavaType<NClob> NCLOB = new JavaTypes<NClob>() {
		@Override
		protected NClob doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getNClob(columnIndex);
		}

		@Override
		protected NClob doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getNClob(columnLabel);
		}

		@Override
		protected NClob doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof NClob) {
				return (NClob) value;
			}
			return super.convert(value);
		}
	};

	//
	public static final JavaType<byte[]> BYTE_ARRAY = new JavaTypes<byte[]>() {
		@Override
		protected byte[] doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getBytes(columnIndex);
		}

		@Override
		protected byte[] doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getBytes(columnLabel);
		}

		@Override
		protected byte[] doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Byte) {
				return new byte[] { (Byte) value };
			}
			if (value instanceof byte[]) {
				return (byte[]) value;
			}
			if (value instanceof InputStream || value instanceof Blob || value instanceof Clob || value instanceof NClob) {
				InputStream is = INPUT_STREAM.convert(value);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtil.copy(is, out);
				return out.toByteArray();
			}
			if (value instanceof File) {
				InputStream is = null;
				try {
					File file = (File) value;
					is = FileInputStreamUtil.newFileInputStream(file);
					return convert(is);
				} finally {
					CloseableUtil.close(is);
				}
			}
			try {
				return value.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	};

	public static final JavaType<java.sql.SQLXML> SQLXML = new JavaTypes<java.sql.SQLXML>() {
		@Override
		protected java.sql.SQLXML doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return rs.getSQLXML(columnIndex);
		}

		@Override
		protected java.sql.SQLXML doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return rs.getSQLXML(columnLabel);
		}
	};

	public static final JavaType<File> FILE = new JavaTypes<File>() {
		@Override
		protected File doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof File) {
				return (File) value;
			}
			if (value instanceof String) {
				return new File((String) value);
			}
			if (value instanceof URI) {
				return new File((URI) value);
			}
			if (value instanceof URL) {
				return new File(URI.convert(value));
			}
			throw new IllegalArgumentException();
		}
	};

	public static final JavaType<java.net.URI> URI = new JavaTypes<java.net.URI>() {
		@Override
		protected URI doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof URI) {
				return (URI) value;
			}
			if (value instanceof String) {
				return URIUtil.newURI((String) value);
			}
			if (value instanceof URL) {
				return URLUtil.toURI((URL) value);
			}
			if (value instanceof File) {
				return ((File) value).toURI();
			}
			throw new IllegalArgumentException();
		}
	};

	public static final JavaType<java.net.URL> URL = new JavaTypes<java.net.URL>() {
		@Override
		protected URL doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof URL) {
				return (URL) value;
			}
			if (value instanceof String) {
				return URLUtil.newURL((String) value);
			}
			if (value instanceof URI) {
				return URIUtil.toURL((URI) value);
			}
			if (value instanceof File) {
				return URIUtil.toURL(((File) value).toURI());
			}
			throw new IllegalArgumentException();
		}
	};

	public static final JavaType<EntityId> ENTITY_ID = new JavaTypes<>() {
		@Override
		protected EntityId doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return new EntityId(string);
		}

		@Override
		protected EntityId doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return new EntityId(string);
		}

		@Override
		protected EntityId doConvert(Object value) {
			if (value == null) {
				return null;
			}
			if(value instanceof EntityId) {
				return (EntityId) value;
			}
			return new EntityId(STRING.convert(value));
		}
	};

	public static final JavaType<java.util.UUID> UUID = new JavaTypes<>() {
		@Override
		protected java.util.UUID doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			String string = rs.getString(columnIndex);
			if (string == null) {
				return null;
			}
			return java.util.UUID.fromString(string);
		}

		@Override
		protected java.util.UUID doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			String string = rs.getString(columnLabel);
			if (string == null) {
				return null;
			}
			return java.util.UUID.fromString(string);
		}

		@Override
		protected java.util.UUID doConvert(Object value) {
			if (value == null) {
				return null;
			}
			return java.util.UUID.fromString(STRING.convert(value));
		}
	};

	public static final JavaType<Object[]> ARRAY = new JavaTypes<Object[]>() {
	};

	@SuppressWarnings("rawtypes")
	public static final JavaType<Collection> COLLECTION = new JavaTypes<Collection>() {
	};

	@SuppressWarnings("rawtypes")
	public static final JavaType<Map> MAP = new JavaTypes<Map>() {
	};

	@SuppressWarnings("rawtypes")
	public static final JavaType<Class> CLASS = new JavaTypes<Class>() {
	};

	public static final JavaType<ClassLoader> CLASS_LOADER = new JavaTypes<ClassLoader>() {
	};

	@SuppressWarnings("rawtypes")
	public static final JavaType<Constructor> CONSTRUCTOR = new JavaTypes<Constructor>() {
	};

	public static final JavaType<Method> METHOD = new JavaTypes<Method>() {
	};

	public static final JavaType<Field> FIELD = new JavaTypes<Field>() {
	};

	public static final JavaType<Annotation> ANNOTATION = new JavaTypes<Annotation>() {
	};

	public static final JavaType<Connection> CONNECTION = new JavaTypes<Connection>() {
	};

	public static final JavaType<DataSource> DATA_SOURCE = new JavaTypes<DataSource>() {
	};

	public static final JavaType<Object> NULL = new JavaTypes<Object>() {
		@Override
		protected Object doGetValue(ResultSet rs, int columnIndex) throws SQLException {
			return null;
		}

		@Override
		protected Object doGetValue(ResultSet rs, String columnLabel) throws SQLException {
			return null;
		}

		@Override
		protected Object doConvert(Object value) {
			return null;
		}
	};

	public static final class EnumType<T extends Enum<T>> implements JavaType<T> {
		private Class<T> enumClass;

		public EnumType(Class<T> enumClass) {
			this.enumClass = enumClass;
		}

		@Override
		public T getValue(ResultSet rs, int columnIndex) throws SQLException {
			Object value = rs.getObject(columnIndex);
			return EnumConverter.convert(value, enumClass);
		}

		@Override
		public T getValue(ResultSet rs, String columnLabel) throws SQLException {
			Object value = rs.getObject(columnLabel);
			return EnumConverter.convert(value, enumClass);
		}

		@Override
		public T getValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
			return getValue(rs, columnIndex);
		}

		@Override
		public T getValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
			return getValue(rs, columnIndex);
		}

		@Override
		public T getValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
			return getValue(rs, columnLabel);
		}

		@Override
		public T getValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
			return getValue(rs, columnLabel);
		}

		@Override
		public T convert(Object value) {
			if (value == null) {
				return null;
			}
			return EnumConverter.convert(value, enumClass);
		}

		@Override
		public T convert(Object value, String... formats) {
			return this.convert(value);
		}

		@Override
		public Class<T> getType() {
			return enumClass;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof EnumType == false) {
				return false;
			}
			return this.enumClass.equals(((EnumType) obj).enumClass);
		}

		@Override
		public Class<?> getWrapperType() {
			return null;
		}

	    @Override
		public T convert(Object value, ZoneId zoneId) {
			return this.convert(value);
		}

	    @Override
		public T convert(Object value, ZoneOffset zoneOffset) {
			return this.convert(value);
		}

	    @Override
		public T convert(Object value, ZoneId zoneId, String... formats) {
			return this.convert(value);
		}

	    @Override
		public T convert(Object value, ZoneOffset zoneOffset, String... formats) {
			return this.convert(value);
		}
	}

	@Override
	public final T getValue(ResultSet rs, int columnIndex) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnIndex));
	}
	@Override
	public final T getValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnIndex, zoneId));
	}
	@Override
	public final T getValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnIndex, zoneOffset));
	}

	protected T doGetValue(ResultSet rs, int columnIndex) throws SQLException {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + ", " + columnIndex);
	}

	protected T doGetValue(ResultSet rs, int columnIndex, ZoneId zoneId) throws SQLException {
		return doGetValue(rs, columnIndex);
	}

	protected T doGetValue(ResultSet rs, int columnIndex, ZoneOffset zoneOffset) throws SQLException {
		return doGetValue(rs, columnIndex);
	}

	@Override
	public final T getValue(ResultSet rs, String columnLabel) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnLabel));
	}
	@Override
	public final T getValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnLabel, zoneId));
	}
	@Override
	public final T getValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
		return FlyweightFactory.get(doGetValue(rs, columnLabel, zoneOffset));
	}

	protected T doGetValue(ResultSet rs, String columnLabel) throws SQLException {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + ", " + columnLabel);
	}

	protected T doGetValue(ResultSet rs, String columnLabel, ZoneId zoneId) throws SQLException {
		return doGetValue(rs, columnLabel);
	}

	protected T doGetValue(ResultSet rs, String columnLabel, ZoneOffset zoneOffset) throws SQLException {
		return doGetValue(rs, columnLabel);
	}

	@Override
	public final T convert(Object value) {
		return FlyweightFactory.get(doConvert(value));
	}

	@Override
	public final T convert(Object value, ZoneId zoneId) {
		return FlyweightFactory.get(doConvert(value, zoneId));
	}

	@Override
	public final T convert(Object value, ZoneOffset zoneOffset) {
		return FlyweightFactory.get(doConvert(value, zoneOffset));
	}

	@Override
	public final T convert(Object value, String... formats) {
		return FlyweightFactory.get(this.doConvert(value, formats));
	}

    @Override
	public T convert(Object value, ZoneId zoneId, String... formats) {
		return FlyweightFactory.get(doConvert(value, zoneId, formats));
	}

    @Override
	public T convert(Object value, ZoneOffset zoneOffset, String... formats) {
		return FlyweightFactory.get(doConvert(value, zoneOffset, formats));
	}

	protected T doConvert(Object value) {
		if (value == null) {
			return null;
		}
		throw new UnsupportedOperationException(value + " => " + this.getClass());
	}

	protected T doConvert(Object value, String... formats) {
		return this.convert(value);
	}

	protected T doConvert(Object value, ZoneId zoneId, String... formats) {
		return this.convert(value);
	}

	protected T doConvert(Object value, ZoneOffset zoneOffset, String... formats) {
		return this.convert(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getType() {
		return (Class<T>) getRawClass(getTypeParameter(this.getClass().getGenericSuperclass(), 0));
	}

	private static Boolean toBoolean(String s) {
		if (s == null) {
			return null;
		}
		if (s.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		if (s.equalsIgnoreCase(Boolean.FALSE.toString())) {
			return false;
		}
		return null;
	}

	private static BigDecimal toBigDecimal(long l) {
		return BigDecimal.valueOf(l);
	}

	private static BigDecimal toBigDecimal(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() == 1) {
			char c = s.charAt(0);
			if (c == 0) {
				return BigDecimalUtil.ZERO;
			}
			if (c == 1) {
				return BigDecimalUtil.ONE;
			}
		}
		try {
			return new BigDecimal(s.replaceAll(",", ""));
		} catch (NumberFormatException e) {
			Boolean bool = toBoolean(s);
			if (bool != null) {
				return BIG_DECIMAL.convert(bool);
			}
			Date date = toDate(s, false, SqlConfig.getDefaultConfig().getFormats());
			if (date != null) {
				return BIG_DECIMAL.convert(NUMBER.convert(date));
			}
			throw (NumberFormatException) new NumberFormatException("For input string: \"" + s + "\"").initCause(e);
		}
	}

	private static java.util.Date toDate(Object value, boolean tryToNumber, String... formats) {
		if (value == null) {
			return null;
		}
		if (value.getClass().equals(java.util.Date.class)) {
			return (Date) value;
		}
		if (value instanceof Date) {
			return new Date(((Date) value).getTime());
		}
		if (value instanceof Calendar) {
			return ((Calendar) value).getTime();
		}
		if (value instanceof Boolean) {
			throw new UnsupportedOperationException(value + " => " + java.util.Date.class);
		}
		if (value instanceof CharSequence) {
			if (formats == null) {
				formats = defaultFormats();
			}
			for (String format : formats) {
				try {
					Date date = new SimpleDateFormat(format).parse(value.toString());
					if (new SimpleDateFormat(format).format(date).equals(value)) {
						return date;
					}
				} catch (ParseException ignore) {
				}
			}
		}
		if (tryToNumber == false) {
			return null;
		}
		Number num = NUMBER.convert(value);
		if (num == null) {
			return null;
		}
		return new Date(num.longValue());
	}

	private static String[] defaultFormats(String... addFormats) {
		if (EmptyUtil.isNotEmpty(addFormats)) {
			List<String> list = new ArrayList<>(addFormats.length);
			for (String format : addFormats) {
				list.add(format);
			}
			list.addAll(Arrays.asList(TimeUtil.getDefaultFormats()));
			return list.toArray(new String[list.size()]);
		}
		return TimeUtil.getDefaultFormats();
	}

	public static JavaType<?>[] values() {
		Field[] fields = JavaTypes.class.getFields();
		List<JavaType<?>> values = new ArrayList<>(fields.length);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) == false) {
				continue;
			}
			Object fieldValue = FieldUtil.get(field, null);
			if (fieldValue instanceof JavaType) {
				values.add((JavaType<?>) fieldValue);

			}
		}
		return values.toArray(new JavaType<?>[values.size()]);
	}

	private static byte toPrimitive(Byte value) {
		if (value == null) {
			return 0;
		}
		return value;
	}

	private static short toPrimitive(Short value) {
		if (value == null) {
			return 0;
		}
		return value;
	}

	private static int toPrimitive(Integer value) {
		if (value == null) {
			return 0;
		}
		return value;
	}

	private static long toPrimitive(Long value) {
		if (value == null) {
			return 0L;
		}
		return value;
	}

	private static float toPrimitive(Float value) {
		if (value == null) {
			return 0f;
		}
		return value;
	}

	private static double toPrimitive(Double value) {
		if (value == null) {
			return 0d;
		}
		return value;
	}

	private static char toPrimitive(Character value) {
		if (value == null) {
			return 0;
		}
		return value;
	}

	private static boolean toPrimitive(Boolean value) {
		if (value == null) {
			return false;
		}
		return value;
	}

	@Override
	public Class<?> getWrapperType() {
		return null;
	}
}
