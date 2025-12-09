package jp.dodododo.sql.wrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author Satoshi Kimura
 */
public class ResultSetWrapper implements ResultSet {
	protected ResultSet rs;
	private String sql;

	protected ResultSetWrapper(ResultSet rs) {
		this(rs, null);
	}

	public ResultSetWrapper(ResultSet rs, String sql) {
		this.rs = rs;
		this.sql = sql;
	}

	public String getSql() {
		if (sql != null) {
			return sql;
		}
		if (rs instanceof ResultSetWrapper) {
			ResultSetWrapper wrapper = (ResultSetWrapper) rs;
			return wrapper.getSql();
		}
		return null;
	}

    @Override
	public boolean absolute(int i) throws SQLException {
		return rs.absolute(i);
	}

	@Override
    public void afterLast() throws SQLException {
		rs.afterLast();
	}

    @Override
	public void beforeFirst() throws SQLException {
		rs.beforeFirst();
	}

    @Override
	public void cancelRowUpdates() throws SQLException {
		rs.cancelRowUpdates();
	}

    @Override
	public void clearWarnings() throws SQLException {
		rs.clearWarnings();
	}

    @Override
	public void close() throws SQLException {
		rs.close();
	}

    @Override
	public void deleteRow() throws SQLException {
		rs.deleteRow();
	}

    @Override
	public int findColumn(String s) throws SQLException {
		return rs.findColumn(s);
	}

    @Override
	public boolean first() throws SQLException {
		return rs.first();
	}

    @Override
	public Array getArray(int i) throws SQLException {
		return rs.getArray(i);
	}

    @Override
	public Array getArray(String s) throws SQLException {
		return rs.getArray(s);
	}

    @Override
	public InputStream getAsciiStream(int i) throws SQLException {
		return rs.getAsciiStream(i);
	}

    @Override
	public InputStream getAsciiStream(String s) throws SQLException {
		return rs.getAsciiStream(s);
	}

    @Override
	@Deprecated
	public BigDecimal getBigDecimal(int i, int j) throws SQLException {
		return rs.getBigDecimal(i, j);
	}

    @Override
	public BigDecimal getBigDecimal(int i) throws SQLException {
		return rs.getBigDecimal(i);
	}

    @Override
	@Deprecated
	public BigDecimal getBigDecimal(String s, int i) throws SQLException {
		return rs.getBigDecimal(s, i);
	}

    @Override
	public BigDecimal getBigDecimal(String s) throws SQLException {
		return rs.getBigDecimal(s);
	}

    @Override
	public InputStream getBinaryStream(int i) throws SQLException {
		return rs.getBinaryStream(i);
	}

    @Override
	public InputStream getBinaryStream(String s) throws SQLException {
		return rs.getBinaryStream(s);
	}

    @Override
	public Blob getBlob(int i) throws SQLException {
		return rs.getBlob(i);
	}

    @Override
	public Blob getBlob(String s) throws SQLException {
		return rs.getBlob(s);
	}

    @Override
	public boolean getBoolean(int i) throws SQLException {
		return rs.getBoolean(i);
	}

    @Override
	public boolean getBoolean(String s) throws SQLException {
		return rs.getBoolean(s);
	}

    @Override
	public byte getByte(int i) throws SQLException {
		return rs.getByte(i);
	}

    @Override
	public byte getByte(String s) throws SQLException {
		return rs.getByte(s);
	}

    @Override
	public byte[] getBytes(int i) throws SQLException {
		return rs.getBytes(i);
	}

    @Override
	public byte[] getBytes(String s) throws SQLException {
		return rs.getBytes(s);
	}

    @Override
	public Reader getCharacterStream(int i) throws SQLException {
		return rs.getCharacterStream(i);
	}

    @Override
	public Reader getCharacterStream(String s) throws SQLException {
		return rs.getCharacterStream(s);
	}

    @Override
	public Clob getClob(int i) throws SQLException {
		return rs.getClob(i);
	}

    @Override
	public Clob getClob(String s) throws SQLException {
		return rs.getClob(s);
	}

    @Override
	public int getConcurrency() throws SQLException {
		return rs.getConcurrency();
	}

    @Override
	public String getCursorName() throws SQLException {
		return rs.getCursorName();
	}

    @Override
	public Date getDate(int i, Calendar calendar) throws SQLException {
		return rs.getDate(i, calendar);
	}

    @Override
	public Date getDate(int i) throws SQLException {
		return rs.getDate(i);
	}

    @Override
	public Date getDate(String s, Calendar calendar) throws SQLException {
		return rs.getDate(s, calendar);
	}

    @Override
	public Date getDate(String s) throws SQLException {
		return rs.getDate(s);
	}

    @Override
	public double getDouble(int i) throws SQLException {
		return rs.getDouble(i);
	}

    @Override
	public double getDouble(String s) throws SQLException {
		return rs.getDouble(s);
	}

    @Override
	public int getFetchDirection() throws SQLException {
		return rs.getFetchDirection();
	}

    @Override
	public int getFetchSize() throws SQLException {
		return rs.getFetchSize();
	}

    @Override
	public float getFloat(int i) throws SQLException {
		return rs.getFloat(i);
	}

    @Override
	public float getFloat(String s) throws SQLException {
		return rs.getFloat(s);
	}

    @Override
	public int getHoldability() throws SQLException {
		return rs.getHoldability();
	}

    @Override
	public int getInt(int i) throws SQLException {
		return rs.getInt(i);
	}

    @Override
	public int getInt(String s) throws SQLException {
		return rs.getInt(s);
	}

    @Override
	public long getLong(int i) throws SQLException {
		return rs.getLong(i);
	}

    @Override
	public long getLong(String s) throws SQLException {
		return rs.getLong(s);
	}

    @Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return rs.getMetaData();
	}

    @Override
	public Reader getNCharacterStream(int i) throws SQLException {
		return rs.getNCharacterStream(i);
	}

    @Override
	public Reader getNCharacterStream(String s) throws SQLException {
		return rs.getNCharacterStream(s);
	}

    @Override
	public NClob getNClob(int i) throws SQLException {
		return rs.getNClob(i);
	}

    @Override
	public NClob getNClob(String s) throws SQLException {
		return rs.getNClob(s);
	}

    @Override
	public String getNString(int i) throws SQLException {
		return rs.getNString(i);
	}

    @Override
	public String getNString(String s) throws SQLException {
		return rs.getNString(s);
	}

    @Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		return rs.getObject(arg0, arg1);
	}

    @Override
	public Object getObject(int i) throws SQLException {
		return rs.getObject(i);
	}

    @Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		return rs.getObject(arg0, arg1);
	}

    @Override
	public Object getObject(String s) throws SQLException {
		return rs.getObject(s);
	}

    @Override
	public Ref getRef(int i) throws SQLException {
		return rs.getRef(i);
	}

    @Override
	public Ref getRef(String s) throws SQLException {
		return rs.getRef(s);
	}

    @Override
	public int getRow() throws SQLException {
		return rs.getRow();
	}

    @Override
	public RowId getRowId(int i) throws SQLException {
		return rs.getRowId(i);
	}

    @Override
	public RowId getRowId(String s) throws SQLException {
		return rs.getRowId(s);
	}

    @Override
	public short getShort(int i) throws SQLException {
		return rs.getShort(i);
	}

    @Override
	public short getShort(String s) throws SQLException {
		return rs.getShort(s);
	}

    @Override
	public SQLXML getSQLXML(int i) throws SQLException {
		return rs.getSQLXML(i);
	}

    @Override
	public SQLXML getSQLXML(String s) throws SQLException {
		return rs.getSQLXML(s);
	}

    @Override
	public Statement getStatement() throws SQLException {
		return rs.getStatement();
	}

    @Override
	public String getString(int i) throws SQLException {
		return rs.getString(i);
	}

    @Override
	public String getString(String s) throws SQLException {
		return rs.getString(s);
	}

    @Override
	public Time getTime(int i, Calendar calendar) throws SQLException {
		return rs.getTime(i, calendar);
	}

    @Override
	public Time getTime(int i) throws SQLException {
		return rs.getTime(i);
	}

    @Override
	public Time getTime(String s, Calendar calendar) throws SQLException {
		return rs.getTime(s, calendar);
	}

    @Override
	public Time getTime(String s) throws SQLException {
		return rs.getTime(s);
	}

    @Override
	public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
		return rs.getTimestamp(i, calendar);
	}

    @Override
	public Timestamp getTimestamp(int i) throws SQLException {
		return rs.getTimestamp(i);
	}

    @Override
	public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
		return rs.getTimestamp(s, calendar);
	}

    @Override
	public Timestamp getTimestamp(String s) throws SQLException {
		return rs.getTimestamp(s);
	}

    @Override
	public int getType() throws SQLException {
		return rs.getType();
	}

    @Override
	@Deprecated
	public InputStream getUnicodeStream(int i) throws SQLException {
		return rs.getUnicodeStream(i);
	}

    @Override
	@Deprecated
	public InputStream getUnicodeStream(String s) throws SQLException {
		return rs.getUnicodeStream(s);
	}

    @Override
	public URL getURL(int i) throws SQLException {
		return rs.getURL(i);
	}

    @Override
	public URL getURL(String s) throws SQLException {
		return rs.getURL(s);
	}

    @Override
	public SQLWarning getWarnings() throws SQLException {
		return rs.getWarnings();
	}

    @Override
	public void insertRow() throws SQLException {
		rs.insertRow();
	}

    @Override
	public boolean isAfterLast() throws SQLException {
		return rs.isAfterLast();
	}

    @Override
	public boolean isBeforeFirst() throws SQLException {
		return rs.isBeforeFirst();
	}

    @Override
	public boolean isClosed() throws SQLException {
		return rs.isClosed();
	}

    @Override
	public boolean isFirst() throws SQLException {
		return rs.isFirst();
	}

    @Override
	public boolean isLast() throws SQLException {
		return rs.isLast();
	}

    @Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return rs.isWrapperFor(arg0);
	}

    @Override
	public boolean last() throws SQLException {
		return rs.last();
	}

    @Override
	public void moveToCurrentRow() throws SQLException {
		rs.moveToCurrentRow();
	}

    @Override
	public void moveToInsertRow() throws SQLException {
		rs.moveToInsertRow();
	}

    @Override
	public boolean next() throws SQLException {
		return rs.next();
	}

    @Override
	public boolean previous() throws SQLException {
		return rs.previous();
	}

    @Override
	public void refreshRow() throws SQLException {
		rs.refreshRow();
	}

    @Override
	public boolean relative(int i) throws SQLException {
		return rs.relative(i);
	}

    @Override
	public boolean rowDeleted() throws SQLException {
		return rs.rowDeleted();
	}

    @Override
	public boolean rowInserted() throws SQLException {
		return rs.rowInserted();
	}

    @Override
	public boolean rowUpdated() throws SQLException {
		return rs.rowUpdated();
	}

    @Override
	public void setFetchDirection(int i) throws SQLException {
		rs.setFetchDirection(i);
	}

    @Override
	public void setFetchSize(int i) throws SQLException {
		rs.setFetchSize(i);
	}

    @Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return rs.unwrap(arg0);
	}

    @Override
	public void updateArray(int i, Array array) throws SQLException {
		rs.updateArray(i, array);
	}

    @Override
	public void updateArray(String s, Array array) throws SQLException {
		rs.updateArray(s, array);
	}

    @Override
	public void updateAsciiStream(int i, InputStream inputstream, int j) throws SQLException {
		rs.updateAsciiStream(i, inputstream, j);
	}

    @Override
	public void updateAsciiStream(int i, InputStream inputstream, long l) throws SQLException {
		rs.updateAsciiStream(i, inputstream, l);
	}

    @Override
	public void updateAsciiStream(int i, InputStream inputstream) throws SQLException {
		rs.updateAsciiStream(i, inputstream);
	}

    @Override
	public void updateAsciiStream(String s, InputStream inputstream, int i) throws SQLException {
		rs.updateAsciiStream(s, inputstream, i);
	}

    @Override
	public void updateAsciiStream(String s, InputStream inputstream, long l) throws SQLException {
		rs.updateAsciiStream(s, inputstream, l);
	}

    @Override
	public void updateAsciiStream(String s, InputStream inputstream) throws SQLException {
		rs.updateAsciiStream(s, inputstream);
	}

    @Override
	public void updateBigDecimal(int i, BigDecimal bigdecimal) throws SQLException {
		rs.updateBigDecimal(i, bigdecimal);
	}

    @Override
	public void updateBigDecimal(String s, BigDecimal bigdecimal) throws SQLException {
		rs.updateBigDecimal(s, bigdecimal);
	}

    @Override
	public void updateBinaryStream(int i, InputStream inputstream, int j) throws SQLException {
		rs.updateBinaryStream(i, inputstream, j);
	}

    @Override
	public void updateBinaryStream(int i, InputStream inputstream, long l) throws SQLException {
		rs.updateBinaryStream(i, inputstream, l);
	}

    @Override
	public void updateBinaryStream(int i, InputStream inputstream) throws SQLException {
		rs.updateBinaryStream(i, inputstream);
	}

    @Override
	public void updateBinaryStream(String s, InputStream inputstream, int i) throws SQLException {
		rs.updateBinaryStream(s, inputstream, i);
	}

    @Override
	public void updateBinaryStream(String s, InputStream inputstream, long l) throws SQLException {
		rs.updateBinaryStream(s, inputstream, l);
	}

    @Override
	public void updateBinaryStream(String s, InputStream inputstream) throws SQLException {
		rs.updateBinaryStream(s, inputstream);
	}

    @Override
	public void updateBlob(int i, Blob blob) throws SQLException {
		rs.updateBlob(i, blob);
	}

    @Override
	public void updateBlob(int i, InputStream inputstream, long l) throws SQLException {
		rs.updateBlob(i, inputstream, l);
	}

    @Override
	public void updateBlob(int i, InputStream inputstream) throws SQLException {
		rs.updateBlob(i, inputstream);
	}

    @Override
	public void updateBlob(String s, Blob blob) throws SQLException {
		rs.updateBlob(s, blob);
	}

    @Override
	public void updateBlob(String s, InputStream inputstream, long l) throws SQLException {
		rs.updateBlob(s, inputstream, l);
	}

    @Override
	public void updateBlob(String s, InputStream inputstream) throws SQLException {
		rs.updateBlob(s, inputstream);
	}

    @Override
	public void updateBoolean(int i, boolean flag) throws SQLException {
		rs.updateBoolean(i, flag);
	}

    @Override
	public void updateBoolean(String s, boolean flag) throws SQLException {
		rs.updateBoolean(s, flag);
	}

    @Override
	public void updateByte(int i, byte byte0) throws SQLException {
		rs.updateByte(i, byte0);
	}

    @Override
	public void updateByte(String s, byte byte0) throws SQLException {
		rs.updateByte(s, byte0);
	}

    @Override
	public void updateBytes(int i, byte[] bytes) throws SQLException {
		rs.updateBytes(i, bytes);
	}

    @Override
	public void updateBytes(String s, byte[] bytes) throws SQLException {
		rs.updateBytes(s, bytes);
	}

    @Override
	public void updateCharacterStream(int i, Reader reader, int j) throws SQLException {
		rs.updateCharacterStream(i, reader, j);
	}

    @Override
	public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
		rs.updateCharacterStream(i, reader, l);
	}

    @Override
	public void updateCharacterStream(int i, Reader reader) throws SQLException {
		rs.updateCharacterStream(i, reader);
	}

    @Override
	public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {
		rs.updateCharacterStream(s, reader, i);
	}

    @Override
	public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {
		rs.updateCharacterStream(s, reader, l);
	}

    @Override
	public void updateCharacterStream(String s, Reader reader) throws SQLException {
		rs.updateCharacterStream(s, reader);
	}

    @Override
	public void updateClob(int i, Clob clob) throws SQLException {
		rs.updateClob(i, clob);
	}

    @Override
	public void updateClob(int i, Reader reader, long l) throws SQLException {
		rs.updateClob(i, reader, l);
	}

    @Override
	public void updateClob(int i, Reader reader) throws SQLException {
		rs.updateClob(i, reader);
	}

    @Override
	public void updateClob(String s, Clob clob) throws SQLException {
		rs.updateClob(s, clob);
	}

    @Override
	public void updateClob(String s, Reader reader, long l) throws SQLException {
		rs.updateClob(s, reader, l);
	}

    @Override
	public void updateClob(String s, Reader reader) throws SQLException {
		rs.updateClob(s, reader);
	}

    @Override
	public void updateDate(int i, Date date) throws SQLException {
		rs.updateDate(i, date);
	}

    @Override
	public void updateDate(String s, Date date) throws SQLException {
		rs.updateDate(s, date);
	}

    @Override
	public void updateDouble(int i, double d) throws SQLException {
		rs.updateDouble(i, d);
	}

    @Override
	public void updateDouble(String s, double d) throws SQLException {
		rs.updateDouble(s, d);
	}

    @Override
	public void updateFloat(int i, float f) throws SQLException {
		rs.updateFloat(i, f);
	}

    @Override
	public void updateFloat(String s, float f) throws SQLException {
		rs.updateFloat(s, f);
	}

    @Override
	public void updateInt(int i, int j) throws SQLException {
		rs.updateInt(i, j);
	}

    @Override
	public void updateInt(String s, int i) throws SQLException {
		rs.updateInt(s, i);
	}

    @Override
	public void updateLong(int i, long l) throws SQLException {
		rs.updateLong(i, l);
	}

    @Override
	public void updateLong(String s, long l) throws SQLException {
		rs.updateLong(s, l);
	}

    @Override
	public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
		rs.updateNCharacterStream(i, reader, l);
	}

    @Override
	public void updateNCharacterStream(int i, Reader reader) throws SQLException {
		rs.updateNCharacterStream(i, reader);
	}

    @Override
	public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {
		rs.updateNCharacterStream(s, reader, l);
	}

    @Override
	public void updateNCharacterStream(String s, Reader reader) throws SQLException {
		rs.updateNCharacterStream(s, reader);
	}

    @Override
	public void updateNClob(int i, NClob nclob) throws SQLException {
		rs.updateNClob(i, nclob);
	}

    @Override
	public void updateNClob(int i, Reader reader, long l) throws SQLException {
		rs.updateNClob(i, reader, l);
	}

    @Override
	public void updateNClob(int i, Reader reader) throws SQLException {
		rs.updateNClob(i, reader);
	}

    @Override
	public void updateNClob(String s, NClob nclob) throws SQLException {
		rs.updateNClob(s, nclob);
	}

    @Override
	public void updateNClob(String s, Reader reader, long l) throws SQLException {
		rs.updateNClob(s, reader, l);
	}

    @Override
	public void updateNClob(String s, Reader reader) throws SQLException {
		rs.updateNClob(s, reader);
	}

    @Override
	public void updateNString(int i, String s) throws SQLException {
		rs.updateNString(i, s);
	}

    @Override
	public void updateNString(String s, String s1) throws SQLException {
		rs.updateNString(s, s1);
	}

    @Override
	public void updateNull(int i) throws SQLException {
		rs.updateNull(i);
	}

    @Override
	public void updateNull(String s) throws SQLException {
		rs.updateNull(s);
	}

    @Override
	public void updateObject(int i, Object obj, int j) throws SQLException {
		rs.updateObject(i, obj, j);
	}

    @Override
	public void updateObject(int i, Object obj) throws SQLException {
		rs.updateObject(i, obj);
	}

    @Override
	public void updateObject(String s, Object obj, int i) throws SQLException {
		rs.updateObject(s, obj, i);
	}

    @Override
	public void updateObject(String s, Object obj) throws SQLException {
		rs.updateObject(s, obj);
	}

    @Override
	public void updateRef(int i, Ref ref) throws SQLException {
		rs.updateRef(i, ref);
	}

    @Override
	public void updateRef(String s, Ref ref) throws SQLException {
		rs.updateRef(s, ref);
	}

    @Override
	public void updateRow() throws SQLException {
		rs.updateRow();
	}

    @Override
	public void updateRowId(int i, RowId rowid) throws SQLException {
		rs.updateRowId(i, rowid);
	}

    @Override
	public void updateRowId(String s, RowId rowid) throws SQLException {
		rs.updateRowId(s, rowid);
	}

    @Override
	public void updateShort(int i, short word0) throws SQLException {
		rs.updateShort(i, word0);
	}

    @Override
	public void updateShort(String s, short word0) throws SQLException {
		rs.updateShort(s, word0);
	}

    @Override
	public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
		rs.updateSQLXML(i, sqlxml);
	}

    @Override
	public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
		rs.updateSQLXML(s, sqlxml);
	}

    @Override
	public void updateString(int i, String s) throws SQLException {
		rs.updateString(i, s);
	}

    @Override
	public void updateString(String s, String s1) throws SQLException {
		rs.updateString(s, s1);
	}

    @Override
	public void updateTime(int i, Time time) throws SQLException {
		rs.updateTime(i, time);
	}

    @Override
	public void updateTime(String s, Time time) throws SQLException {
		rs.updateTime(s, time);
	}

    @Override
	public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {
		rs.updateTimestamp(i, timestamp);
	}

    @Override
	public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {
		rs.updateTimestamp(s, timestamp);
	}

    @Override
	public boolean wasNull() throws SQLException {
		return rs.wasNull();
	}

    @Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return rs.getObject(columnIndex, type);
	}

    @Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return rs.getObject(columnLabel, type);
	}

}
