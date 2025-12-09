package jp.dodododo.sql.types;

import static jp.dodododo.sql.types.TypeConverter.*;
import static jp.dodododo.sql.util.StringUtil.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.config.DaoConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypeConverterTest {

	@Test
	public void testGet() throws Exception {
		Map<String, Object> map = new HashMap<>();
		TypeConverter typeConverter = new TypeConverter(map);

		map.put("b1", "true");
		map.put("b2", "false");
		map.put("b3", "1");
		map.put("b4", "0");
		Boolean boolean1 = typeConverter.getBoolean("b1");
		assertEquals(Boolean.TRUE, boolean1);
		int b1Num = typeConverter.getInteger("b1");
		assertEquals(1, b1Num);
		Boolean boolean2 = typeConverter.getBoolean("b2");
		assertEquals(Boolean.FALSE, boolean2);
		Boolean boolean3 = typeConverter.getBoolean("b3");
		assertEquals(Boolean.TRUE, boolean3);
		Boolean boolean4 = typeConverter.getBoolean("b4");
		assertEquals(Boolean.FALSE, boolean4);

		map.put("char", "1");
		Character character = typeConverter.getCharacter("char");
		assertEquals('1', character.charValue());

		map.put("byte", "" + Byte.MAX_VALUE);
		Byte byte1 = typeConverter.getByte("byte");
		assertEquals(Byte.MAX_VALUE, byte1.intValue());

		map.put("s", "" + Short.MAX_VALUE);
		Short short1 = typeConverter.getShort("s");
		assertEquals(Short.MAX_VALUE, short1.intValue());

		map.put("i", "" + Integer.MAX_VALUE);
		Integer integer = typeConverter.getInteger("i");
		assertEquals(Integer.MAX_VALUE, integer.intValue());

		map.put("f", "" + Float.MAX_VALUE);
		Float float1 = typeConverter.getFloat("f");
		assertEquals(Float.MAX_VALUE, float1.floatValue());

		map.put("d", "" + Double.MAX_VALUE);
		Double double1 = typeConverter.getDouble("d");
		assertEquals(Double.MAX_VALUE, double1.doubleValue());

		map.put("l", "" + Long.MAX_VALUE);
		Long long1 = typeConverter.getLong("l");
		assertEquals(Long.MAX_VALUE, long1.longValue());

		map.put("n", "111");
		Number number = typeConverter.getNumber("n");
		assertEquals(111, number.intValue());

		map.put("bi", "22222");
		BigInteger bigInteger = typeConverter.getBigInteger("bi");
		assertEquals(22222, bigInteger.intValue());

		map.put("bd", "333333");
		BigDecimal bigDecimal = typeConverter.getBigDecimal("bd");
		assertEquals(333333, bigDecimal.intValue());

		map.put("str", "string");
		String string = typeConverter.getString("str");
		assertEquals("string", string);
		try {
			typeConverter.getNumber("str");
			fail();
		} catch (NumberFormatException success) {
		}

		map.put("c", new java.util.Date());
		map.put("sd", new java.util.Date());
		map.put("t", new java.util.Date());
		map.put("ts", new java.util.Date());
		Calendar calendar = typeConverter.getCalendar("c");
		assertNotNull(calendar);
		Date sqlDate = typeConverter.getSqlDate("sd");
		assertNotNull(sqlDate);
		Time time = typeConverter.getTime("t");
		assertNotNull(time);
		Timestamp timestamp = typeConverter.getTimestamp("ts");
		assertNotNull(timestamp);

		map.put("is", "inputStream");
		InputStream inputStream = typeConverter.getInputStream("is");
		byte[] bytes = new byte[32];
		inputStream.read(bytes);
		assertEquals("inputStream", newString(bytes, "UTF-8").trim());

		map.put("is2", "inputStream2".getBytes("UTF-8"));
		InputStream inputStream2 = typeConverter.getInputStream("is2");
		byte[] bytes2 = new byte[32];
		inputStream2.read(bytes2);
		assertEquals("inputStream2", newString(bytes2, "UTF-8").trim());

		map.put("bytes", "bytes");
		bytes = typeConverter.getBytes("bytes");
		assertEquals("bytes", newString(bytes, "UTF-8").trim());

		map.put("bytes2", typeConverter.getInputStream("is"));
		bytes2 = typeConverter.getBytes("bytes2");
		assertEquals("inputStream", newString(bytes2, "UTF-8").trim());
	}

	@Test
	public void testConvert() throws Exception {
		byte[] bytes = convert("abc", byte[].class);
		assertEquals("abc", newString(bytes, "UTF-8").trim());

		java.util.Date date = convert("1000", java.util.Date.class);
		assertEquals(1000, date.getTime());

		Object o = convert("1000", Object.class);
		assertEquals("1000", o);

		int i = convert("1000", Integer.class);
		assertEquals(1000, i);

		i = convert(null, Integer.TYPE);
		assertEquals(0, i);
	}

	@Test
	public void testZone() {
		ZoneId zoneId = convert(9, ZoneId.class);
		assertEquals("+09:00", zoneId.getId());
		zoneId = convert(-9, ZoneId.class);
		assertEquals("-09:00", zoneId.getId());
		zoneId = convert("JST", ZoneId.class);
		assertEquals("Asia/Tokyo", zoneId.getId());
		zoneId = convert("Asia/Tokyo", ZoneId.class);
		assertEquals("Asia/Tokyo", zoneId.getId());

		ZoneOffset zoneOffset = convert(9, ZoneOffset.class);
		assertEquals("+09:00", zoneOffset.getId());
		zoneOffset = convert(-9, ZoneOffset.class);
		assertEquals("-09:00", zoneOffset.getId());
		zoneOffset = convert("JST", ZoneOffset.class);
		assertEquals("+09:00", zoneOffset.getId());
		zoneOffset = convert("Asia/Tokyo", ZoneOffset.class);
		assertEquals("+09:00", zoneOffset.getId());
	}

	@Test
	public void testNumComma() {
		Map<String, Object> map = new HashMap<>();
		map.put("num", "1,000,000.000");

		TypeConverter converter = new TypeConverter(map);
		int num = converter.getInteger("num");
		assertEquals(1000000, num);
	}

	@Test
	public void testFormattedDate() {
		DaoConfig.getDefaultConfig().setFormats(new String[0]);

		Map<String, Object> map = new HashMap<>();
		map.put("date", "2000/11/22");

		TypeConverter converter = new TypeConverter(map, "yyyy/MM/dd");

		java.util.Date date = converter.getDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(date));

		Date sqlDate = converter.getSqlDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(sqlDate));

		Time time = converter.getTime("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(time));

		Timestamp timestamp = converter.getTimestamp("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(timestamp));

		Calendar calendar = converter.getCalendar("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()));
	}

	@Test
	public void testFormattedDate2() {
		DaoConfig.getDefaultConfig().setFormats(new String[0]);

		Map<String, Object> map = new HashMap<>();
		map.put("date", "2000/1122");

		TypeConverter converter = new TypeConverter(map, "yyyy/MM/dd", "yyyy/MMdd");

		java.util.Date date = converter.getDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(date));

		Date sqlDate = converter.getSqlDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(sqlDate));

		Time time = converter.getTime("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(time));

		Timestamp timestamp = converter.getTimestamp("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(timestamp));

		Calendar calendar = converter.getCalendar("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()));
	}

	@Test
	public void testFormattedDateByDefault() {
		DaoConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy/MMdd", "yyyyMMdd");

		Map<String, Object> map = new HashMap<>();
		map.put("date", "20001122");

		TypeConverter converter = new TypeConverter(map);

		java.util.Date date = converter.getDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(date));

		Date sqlDate = converter.getSqlDate("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(sqlDate));

		Time time = converter.getTime("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(time));

		Timestamp timestamp = converter.getTimestamp("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(timestamp));

		Calendar calendar = converter.getCalendar("Date");
		assertEquals("20001122", new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()));
	}

	@Test
	public void testInputStreamToSomeType() throws Exception {
		assertEquals(Integer.valueOf(0), TypeConverter.convert(new ByteArrayInputStream(new byte[] { 0 }), Integer.class));
		assertEquals(Integer.valueOf(1), TypeConverter.convert(new ByteArrayInputStream(new byte[] { 1 }), Integer.class));

		assertFalse(TypeConverter.convert(new ByteArrayInputStream(new byte[] { 0 }), Boolean.class));
		assertTrue(TypeConverter.convert(new ByteArrayInputStream(new byte[] { 1 }), Boolean.class));

		assertEquals("0", TypeConverter.convert(new ByteArrayInputStream("0".getBytes("UTF-8")), String.class));
		assertEquals("1", TypeConverter.convert(new ByteArrayInputStream("1".getBytes("UTF-8")), String.class));

		assertEquals(Integer.valueOf(0), TypeConverter.convert(new ByteArrayInputStream("0".getBytes("UTF-8")), Integer.class));
		assertEquals(Integer.valueOf(1), TypeConverter.convert(new ByteArrayInputStream("1".getBytes("UTF-8")), Integer.class));

		assertTrue(TypeConverter.convert(new ByteArrayInputStream("1".getBytes("UTF-8")), Boolean.class));
		assertTrue(TypeConverter.convert(new ByteArrayInputStream("true".getBytes("UTF-8")), Boolean.class));
		assertFalse(TypeConverter.convert(new ByteArrayInputStream("0".getBytes("UTF-8")), Boolean.class));
		assertFalse(TypeConverter.convert(new ByteArrayInputStream("false".getBytes("UTF-8")), Boolean.class));
	}

	@Test
	public void testSrc() {
		assertEquals("1", src(1).to(String.class));
		assertEquals(1000L, (long) src("1,000.00").to(Long.class));

		assertEquals(null, src(null).to(String.class));
		assertEquals(null, src(null).to(Long.class));
		assertEquals(0L, (long) src(null).to(Long.TYPE));
	}

	@Test
	public void testVal() {
		assertEquals("1", val(1).as(String.class));
		assertEquals(1000L, (long) val("1,000.00").as(Long.class));

		assertEquals(null, val(null).as(String.class));
		assertEquals(null, val(null).as(Long.class));
		assertEquals(0L, (long) val(null).as(Long.TYPE));
	}
}
