package jp.dodododo.sql.issue;

import static jp.dodododo.sql.util.SqlUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.row.Row;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Issue8Test {

	@BeforeEach
	public void setUp() throws Exception {
		SqlConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss");
	}

	@Test
	public void test() throws Exception {
		Row row = new Row(map("bool", true, "num", "12,345,678.90", "date1", "2000/01/23 12:23:34", "date2", "2000/01/23"));
		assertTrue(row.getBoolean("bool"));
		assertEquals(12345678.90D, row.getDouble("num").doubleValue(), 2);
		assertEquals(12345678.90F, row.getFloat("num").doubleValue(), 2);
		assertEquals(new BigDecimal("12345678.90"), row.getBigDecimal("num"));
		assertEquals(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2000/01/23 12:23:34"), row.getDate("date1"));
		assertEquals(new SimpleDateFormat("yyyy/MM/dd").parse("2000/01/23"), row.getDate("date2"));
	}

}
