package jp.dodododo.dao.row;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import jp.dodododo.dao.annotation.StringKey;
import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.types.JavaTypes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowTest {
	@BeforeEach
	public void setup() {
		DaoConfig.getDefaultConfig().setFormats("MM/dd yyyy");
	}

	@Test
	public void testGetDate() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("date", JavaTypes.DATE.convert("2000/11/22", "yyyy/MM/dd"));
		map.put("calendar", JavaTypes.CALENDAR.convert("2000/11/22", "yyyy/MM/dd"));
		map.put("str1", "2000/01/02");
		map.put("str2", "2000-01-02");
		Row row = new Row(map);
		row.setFormats("yyyy/MM/dd", "yyyy-MM-dd");
		assertEquals("20001122", row.getString("calendar", "yyyyMMdd"));
		assertEquals("2000-11-22", row.getString("calendar", "yyyy-MM-dd"));
		assertEquals("20001122", row.getString("date", "yyyyMMdd"));
		assertEquals("2000-11-22", row.getString("date", "yyyy-MM-dd"));
		assertEquals("20000102", new SimpleDateFormat("yyyyMMdd").format(row.getDate("str1")));
		assertEquals("20000102", new SimpleDateFormat("yyyyMMdd").format(row.getDate("str2")));
	}

	@Test
	public void testGetNum() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("num", new BigDecimal("1111111111.111111"));
		Row row = new Row(map);
		assertEquals("1111111111", row.getString("num", "0"));
		assertEquals("1111111111.11", row.getString("num", "0.00"));
		assertEquals("1,111,111,111.11", row.getString("num", "#,000.00"));
	}

	@Test
	public void testHasKey() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", null);
		map.put("b", "B");
		Row row = new Row(map);
		assertTrue(row.hasKey("a"));
		assertTrue(row.hasKey("b"));
		assertFalse(row.hasKey("c"));
	}

	@Test
	public void testHasValue() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", null);
		map.put("b", "B");
		Row row = new Row(map);
		assertFalse(row.hasValue("a"));
		assertTrue(row.hasValue("b"));
		assertFalse(row.hasValue("c"));
		assertNull(row.getObject("c"));
	}

	@Test
	public void testIsNull() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", null);
		map.put("b", "B");
		Row row = new Row(map);
		assertTrue(row.isNull("a"));
		assertFalse(row.isNull("b"));
		assertTrue(row.isNull("c"));
	}

	@Test
	public void testGetEnum() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("A", null);
		map.put("B", "b");
		map.put("C", "C");
		map.put("X", E.A);
		Row row = new Row(map);
		assertNull(row.getEnum("A", E.class));
		assertEquals(E.B, row.getEnum("B", E.class));
		try {
			assertNull(row.getEnum("C", E.class));
			fail();
		} catch (IllegalArgumentException success) {
		}
		assertEquals(E.A, row.getEnum("X", E.class));
	}

	public static enum E {
		A("a"), B("b"), C("c");

		private String code;

		private E(String code) {
			this.code = code;
		}

		@StringKey
		public String getCode() {
			return code;
		}

	}

}
