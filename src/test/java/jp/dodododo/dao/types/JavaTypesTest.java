package jp.dodododo.dao.types;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import jp.dodododo.dao.util.TmpFileUtil;

import org.junit.jupiter.api.Test;

public class JavaTypesTest {

	@Test
	public void testINTEGER() throws Exception {
		Integer integer = JavaTypes.INTEGER.convert("11.00");
		assertEquals(11, integer.intValue());
	}

	@Test
	public void testBOOLEAN() throws Exception {
		assertEquals(true, JavaTypes.BOOLEAN.convert("true"));
		assertEquals(false, JavaTypes.BOOLEAN.convert("false"));
		assertEquals(true, JavaTypes.BOOLEAN.convert("1"));
		assertEquals(false, JavaTypes.BOOLEAN.convert("0"));
		assertEquals(true, JavaTypes.BOOLEAN.convert(1));
		assertEquals(false, JavaTypes.BOOLEAN.convert(0));
	}

	@Test
	public void testFileToByteArray() throws Exception {
		File file = TmpFileUtil.createTempFile();
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		randomAccessFile.writeBytes("abc");
		randomAccessFile.close();
		byte[] bs = JavaTypes.BYTE_ARRAY.convert(file);
		assertEquals(3, bs.length);
		assertEquals('a', bs[0]);
		assertEquals('b', bs[1]);
		assertEquals('c', bs[2]);
		TmpFileUtil.deleteThreadLocalTmpFiles();
	}

	@Test
	public void testINSTANT() throws Exception {
		Date date = new Date();
		Instant instant = JavaTypes.INSTANT.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals(simpleDateFormat.format(date), DateTimeFormatter.ISO_INSTANT.format(instant).replaceAll("T|Z", ""));
	}

	@Test
	public void testLOCAL_DATE() throws Exception {
		Date date = new Date();
		LocalDate localDate = JavaTypes.LOCAL_DATE.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals(simpleDateFormat.format(date), DateTimeFormatter.ISO_LOCAL_DATE.format(localDate).replaceAll("T|Z", ""));

		localDate = JavaTypes.LOCAL_DATE.convert("20150123");
		assertEquals("2015-01-23", DateTimeFormatter.ISO_LOCAL_DATE.format(localDate).replaceAll("T|Z", ""));
		localDate = JavaTypes.LOCAL_DATE.convert("2015/01/23");
		assertEquals("2015-01-23", DateTimeFormatter.ISO_LOCAL_DATE.format(localDate).replaceAll("T|Z", ""));
		localDate = JavaTypes.LOCAL_DATE.convert(0);
		assertEquals("1970-01-01", DateTimeFormatter.ISO_LOCAL_DATE.format(localDate).replaceAll("T|Z", ""));
	}

	@Test
	public void testLOCAL_DATE_TIME() throws Exception {
		Date date = new Date();
		LocalDateTime localDateTime = JavaTypes.LOCAL_DATE_TIME.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss.SSS");
		assertEquals(simpleDateFormat.format(date), dateTimeFormatter.format(localDateTime));

		localDateTime = JavaTypes.LOCAL_DATE_TIME.convert("201501230405");
		assertEquals("2015-01-2304:05:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime).replaceAll("T|Z", ""));
		localDateTime = JavaTypes.LOCAL_DATE_TIME.convert("2015/01/23 04:05");
		assertEquals("2015-01-2304:05:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime).replaceAll("T|Z", ""));
		localDateTime = JavaTypes.LOCAL_DATE_TIME.convert(0);
		assertEquals("1970-01-0109:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime).replaceAll("T|Z", ""));
	}

	@Test
	public void testLOCAL_TIME() throws Exception {
		Date date = new Date();
		LocalTime localTime = JavaTypes.LOCAL_TIME.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		assertEquals(simpleDateFormat.format(date), dateTimeFormatter.format(localTime));

		localTime = JavaTypes.LOCAL_TIME.convert("0405");
		assertEquals("04:05:00", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert("040506");
		assertEquals("04:05:06", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert(405);
		assertEquals("04:05:00", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert(40506);
		assertEquals("04:05:06", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert(130506);
		assertEquals("13:05:06", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert("04:05:06");
		assertEquals("04:05:06", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
		localTime = JavaTypes.LOCAL_TIME.convert(0);
		assertEquals("09:00:00", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
	}

	@Test
	public void testYEAR() throws Exception {
		Date date = new Date();
		Year year = JavaTypes.YEAR.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
		assertEquals(simpleDateFormat.format(date), String.valueOf(year));

		year = JavaTypes.YEAR.convert("1974");
		assertEquals(1974, year.getValue());
		year = JavaTypes.YEAR.convert(2015);
		assertEquals(2015, year.getValue());
		year = JavaTypes.YEAR.convert("26");
		assertEquals(2026, year.getValue());
		year = JavaTypes.YEAR.convert("99");
		assertEquals(2099, year.getValue());
	}

	@Test
	public void testYEAR_MONTH() throws Exception {
		Date date = new Date();
		YearMonth value = JavaTypes.YEAR_MONTH.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		assertEquals(simpleDateFormat.format(date), DateTimeFormatter.ofPattern("yyyyMM").format(value));

		value = JavaTypes.YEAR_MONTH.convert("197401");
		assertEquals(1974, value.getYear());
		assertEquals(Month.JANUARY, value.getMonth());
		value = JavaTypes.YEAR_MONTH.convert("1974/01");
		assertEquals(1974, value.getYear());
		value = JavaTypes.YEAR_MONTH.convert("01/1974");
		assertEquals(1974, value.getYear());
		assertEquals(Month.JANUARY, value.getMonth());
		value = JavaTypes.YEAR_MONTH.convert(201502);
		assertEquals(2015, value.getYear());
		assertEquals(Month.FEBRUARY, value.getMonth());
		value = JavaTypes.YEAR_MONTH.convert("2603");
		assertEquals(2026, value.getYear());
		assertEquals(Month.MARCH, value.getMonth());
		value = JavaTypes.YEAR_MONTH.convert("9904");
		assertEquals(2099, value.getYear());
		assertEquals(Month.APRIL, value.getMonth());
	}

	@Test
	public void testMONTH_DAY() throws Exception {
		Date date = new Date();
		MonthDay value = JavaTypes.MONTH_DAY.convert(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");
		assertEquals(simpleDateFormat.format(date), DateTimeFormatter.ofPattern("MMdd").format(value));

		value = JavaTypes.MONTH_DAY.convert("0123");
		assertEquals(Month.JANUARY, value.getMonth());
		assertEquals(23, value.getDayOfMonth());
		value = JavaTypes.MONTH_DAY.convert("01/23");
		assertEquals(Month.JANUARY, value.getMonth());
		assertEquals(23, value.getDayOfMonth());
		value = JavaTypes.MONTH_DAY.convert("01-23");
		assertEquals(Month.JANUARY, value.getMonth());
		assertEquals(23, value.getDayOfMonth());
		value = JavaTypes.MONTH_DAY.convert(1230);
		assertEquals(Month.DECEMBER, value.getMonth());
		assertEquals(30, value.getDayOfMonth());
	}
}
