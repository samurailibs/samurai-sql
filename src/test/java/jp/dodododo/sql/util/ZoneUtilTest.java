package jp.dodododo.sql.util;

import static jp.dodododo.sql.util.ZoneUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class ZoneUtilTest {

	@Test
	public void testZoneOffset() {
		assertEquals("+09:00", zoneOffset(zoneId("Asia/Tokyo")).getId());
		assertEquals("+09:00", zoneOffset(zoneId("JST")).getId());
		assertEquals("+09:00", zoneOffset(zoneId("+9")).getId());
		assertEquals("-10:00", zoneOffset(zoneId("-10")).getId());

		if (TimeZone.getTimeZone("America/Los_Angeles").inDaylightTime(new Date())) {
			assertEquals("-07:00", zoneOffset(zoneId("America/Los_Angeles")).getId());
			assertEquals("-07:00", zoneOffset(zoneId("US/Pacific")).getId());
			assertEquals("-08:00", zoneOffset(zoneId("America/Anchorage")).getId());
		} else {
			assertEquals("-08:00", zoneOffset(zoneId("America/Los_Angeles")).getId());
			assertEquals("-08:00", zoneOffset(zoneId("US/Pacific")).getId());
			assertEquals("-09:00", zoneOffset(zoneId("America/Anchorage")).getId());
		}
		if (TimeZone.getTimeZone("ECT").inDaylightTime(new Date())) {
			assertEquals("+02:00", zoneOffset(zoneId("ECT")).getId());
		} else {
			assertEquals("+01:00", zoneOffset(zoneId("ECT")).getId());
		}
	}
}