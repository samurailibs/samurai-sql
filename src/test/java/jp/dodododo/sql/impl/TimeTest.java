package jp.dodododo.sql.impl;

import static jp.dodododo.sql.sql.GenericSql.*;
import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.annotation.Zone;
import jp.dodododo.sql.unit.DbTestExtension;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


public class TimeTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	static final long defaultOffsetHour;
	static final long defaultOffsetMinutes;

	static {
		String defaultOffset = new SimpleDateFormat("ZZZ").format(new Date());
		int offset = Integer.valueOf(defaultOffset);
		defaultOffsetHour = (offset / 100);
		defaultOffsetMinutes = (offset % 100);

	}

	@Test
	public void testSelect() {
		dao = newTestClient(getDataSource());

		List<TimeBean1> list1 = dao.select(ALL, TimeBean1.class);
		list1.stream().forEach(bean -> {
			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(bean.date);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

			OffsetDateTime odt = bean.offsetDateTime;
			odt = odt.plusHours(5);
			odt = odt.plusHours(defaultOffsetHour);
			if (defaultOffsetHour <= 0) {
				odt = odt.plusMinutes(defaultOffsetMinutes);
			} else {
				odt = odt.minusMinutes(defaultOffsetMinutes);
			}
			OffsetDateTime odt2 = bean.offsetDateTime2;
			odt2 = odt2.plusHours(10);
			odt2 = odt2.plusHours(defaultOffsetHour);
			if (defaultOffsetHour <= 0) {
				odt2 = odt2.plusMinutes(defaultOffsetMinutes);
			} else {
				odt2 = odt2.minusMinutes(defaultOffsetMinutes);
			}

			ZonedDateTime zdt = bean.zonedDateTime;
			zdt = zdt.minusHours(3);
			zdt = zdt.plusHours(defaultOffsetHour);
			if (defaultOffsetHour <= 0) {
				zdt = zdt.plusMinutes(defaultOffsetMinutes);
			} else {
				zdt = zdt.minusMinutes(defaultOffsetMinutes);
			}
			ZonedDateTime zdt2 = bean.zonedDateTime2;
			zdt2 = zdt2.minusHours(4);
			zdt2 = zdt2.plusHours(defaultOffsetHour);
			if (0 <= defaultOffsetHour) {
				zdt2 = zdt2.minusMinutes(defaultOffsetMinutes);
			} else {
				zdt2 = zdt2.plusMinutes(defaultOffsetMinutes);
			}
			assertEquals(date, odt.format(formatter));
			assertEquals(date, odt2.format(formatter));
			assertEquals(date, zdt.format(formatter));
			assertEquals(date, zdt2.format(formatter));
		});

		List<TimeBean2> list2 = dao.select(ALL, TimeBean2.class);
		list2.stream().forEach(bean -> {
			String yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd").format(bean.date);
			String yyyyMM = new SimpleDateFormat("yyyy/MM").format(bean.date);
			String MMdd = new SimpleDateFormat("MM/dd").format(bean.date);
			DateTimeFormatter formatterYyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			DateTimeFormatter formatterYyyyMM = DateTimeFormatter.ofPattern("yyyy/MM");
			DateTimeFormatter formatterMMdd = DateTimeFormatter.ofPattern("MM/dd");

			assertEquals(yyyyMMdd, bean.localDate.format(formatterYyyyMMdd));
			assertEquals(yyyyMM, bean.yearMonth.format(formatterYyyyMM));
			assertEquals(MMdd, bean.monthDay.format(formatterMMdd));

			assertEquals(bean.calendar.get(Calendar.YEAR), bean.year.getValue());
		});
	}

	@Table("EMP")
	public static class TimeBean1 {

		@Column("EMPNO")
		public int id;

		@Column("TSTAMP")
		public Date date;

		@Column("TSTAMP")
		public Timestamp timestamp;

		@Column("TSTAMP")
		public Instant instant;

		@Column("TSTAMP")
		public LocalDate localDate;

		@Column("TSTAMP")
		public LocalDateTime localDateTime;

		@Column("TSTAMP")
		public LocalTime localTime;

		@Column("TSTAMP")
		public Year year;

		@Column("TSTAMP")
		public YearMonth yearMonth;

		@Column("TSTAMP")
		public MonthDay monthDay;

		@Zone(id = "EST")
		@Column("TSTAMP")
		public OffsetDateTime offsetDateTime;

		@Zone(idPropertyName = "zoneId")
		@Column("TSTAMP")
		public OffsetDateTime offsetDateTime2;

		@Zone(offset = "+01:00")
		@Column("TSTAMP")
		public OffsetTime offsetTime;

		@Zone(offsetPropertyName = "zoneOffset")
		@Column("TSTAMP")
		public OffsetTime offsetTime2;

		@Zone(offset = "+03:00")
		@Column("TSTAMP")
		public ZonedDateTime zonedDateTime;

		@Zone(offsetPropertyName = "zoneOffset2")
		@Column("TSTAMP")
		public ZonedDateTime zonedDateTime2;

		public String getZoneId() {
			return "HST";
		}

		public String getZoneOffset() {
			return "+02:00";
		}

		public String getZoneOffset2() {
			return "+04:00";
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		@Override
		public boolean equals(Object o) {
			return EqualsBuilder.reflectionEquals(this, o);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	@Table("EMP")
	public static class TimeBean2 {
		@Column("EMPNO")
		public int id;

		@Column("HIREDATE")
		public Date date;

		@Column("HIREDATE")
		public Calendar calendar;

		@Column("HIREDATE")
		public LocalDate localDate;

		@Column("HIREDATE")
		public Year year;

		@Column("HIREDATE")
		public YearMonth yearMonth;

		@Column("HIREDATE")
		public MonthDay monthDay;

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		@Override
		public boolean equals(Object o) {
			return EqualsBuilder.reflectionEquals(this, o);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}
}