package jp.dodododo.sql.util;

import java.util.Properties;

public class TimeUtil {

	private static String[] yearMonthFormats;
	private static String[] monthDayFormats;
	private static String[] defaultFormats;

	static {
		Properties timeProperties = PropertiesUtil.getProperties("time.properties");
		String yearMonthType = timeProperties.get("YearMonth").toString();
		String monthDayType = timeProperties.get("MonthDay").toString();
		defaultFormats = toArray(timeProperties.get("defaultFormats").toString());

		Properties formatsProperties = PropertiesUtil.getProperties("jp/dodododo/sql/formats.properties");
		yearMonthFormats = toArray(formatsProperties.get(yearMonthType).toString());
		monthDayFormats = toArray(formatsProperties.get(monthDayType).toString());

	}

	public static String[] getYearMonthFormats() {
		return yearMonthFormats;
	}

	public static String[] getMonthDayFormats() {
		return monthDayFormats;
	}

	public static String[] getDefaultFormats() {
		return defaultFormats;
	}

	public static void setMonthDayFormats(String[] monthDayFormats) {
		TimeUtil.monthDayFormats = monthDayFormats;
	}

	public static void setYearMonthFormats(String[] yearMonthFormats) {
		TimeUtil.yearMonthFormats = yearMonthFormats;
	}

	public static void setDefaultFormats(String[] defaultFormats) {
		TimeUtil.defaultFormats = defaultFormats;
	}

	private static String[] toArray(String string) {
		return string.split(",");
	}
}
