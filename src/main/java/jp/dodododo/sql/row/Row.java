package jp.dodododo.sql.row;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import jp.dodododo.sql.config.DaoConfig;
import jp.dodododo.sql.types.TypeConverter;

public class Row {

	protected TypeConverter typeConverter;

	public Row(Object beanOrMap) {
		this(beanOrMap, DaoConfig.getDefaultConfig().getFormats());
	}

	public Row(Object beanOrMap, String... formats) {
		typeConverter = new TypeConverter(beanOrMap, formats);
	}

	public String getString(String key) {
		return typeConverter.getString(key);
	}

	public String getString(String key, String format) {
		return typeConverter.getString(key, format);
	}

	public Number getNumber(String key) {
		return typeConverter.getNumber(key);
	}

	public Short getShort(String key) {
		return typeConverter.getShort(key);
	}

	public Byte getByte(String key) {
		return typeConverter.getByte(key);
	}

	public Integer getInteger(String key) {
		return typeConverter.getInteger(key);
	}

	public Long getLong(String key) {
		return typeConverter.getLong(key);
	}

	public Float getFloat(String key) {
		return typeConverter.getFloat(key);
	}

	public Double getDouble(String key) {
		return typeConverter.getDouble(key);
	}

	public BigDecimal getBigDecimal(String key) {
		return typeConverter.getBigDecimal(key);
	}

	public BigInteger getBigInteger(String key) {
		return typeConverter.getBigInteger(key);
	}

	public Character getCharacter(String key) {
		return typeConverter.getCharacter(key);
	}

	public Boolean getBoolean(String key) {
		return typeConverter.getBoolean(key);
	}

	public java.sql.Date getSqlDate(String key) {
		return typeConverter.getSqlDate(key);
	}

	public SQLXML getSQLXML(String key) {
		return typeConverter.getSQLXML(key);
	}

	public java.util.Date getDate(String key) {
		return typeConverter.getDate(key);
	}

	public Time getTime(String key) {
		return typeConverter.getTime(key);
	}

	public Timestamp getTimestamp(String key) {
		return typeConverter.getTimestamp(key);
	}

	public Calendar getCalendar(String key) {
		return typeConverter.getCalendar(key);
	}

	public InputStream getInputStream(String key) {
		return typeConverter.getInputStream(key);
	}

	public byte[] getBytes(String key) {
		return typeConverter.getBytes(key);
	}

	public Object getObject(String key) {
		return typeConverter.getObject(key);
	}

	public boolean hasKey(String key) {
		return typeConverter.hasKey(key);
	}

	public boolean hasValue(String key) {
		return typeConverter.hasValue(key);
	}

	public boolean isNull(String key) {
		return typeConverter.isNull(key);
	}

	public <ENUM extends Enum<ENUM>> ENUM getEnum(String key, Class<ENUM> enumClass) {
		return typeConverter.getEnum(key, enumClass);
	}

	public <T> T get(String key, Class<T> as) {
		return TypeConverter.convert(getObject(key), as);
	}

	public void setFormats(String... formats) {
		this.typeConverter.setFormats(formats);
	}

	@Override
	public String toString() {
		return this.typeConverter.toMap().toString();
	}
}
