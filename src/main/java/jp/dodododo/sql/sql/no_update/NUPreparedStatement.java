package jp.dodododo.sql.sql.no_update;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import jp.dodododo.sql.wrapper.PreparedStatementWrapper;

public class NUPreparedStatement extends PreparedStatementWrapper {

	protected String sql;

	public NUPreparedStatement(PreparedStatement statement, String sql) {
		super(statement);
		this.sql = sql;
	}

	@Override
	public boolean execute() {
		return true;
	}

	@Override
	public int executeUpdate() {
		return 1;
	}

	@Override
	public boolean execute(String sql) {
		return true;
	}

	@Override
	public boolean execute(String sql, int arg1) {
		return true;
	}

	@Override
	public boolean execute(String sql, int[] arg1) {
		return true;
	}

	@Override
	public boolean execute(String sql, String[] arg1) {
		return true;
	}

	@Override
	public int executeUpdate(String sql) {
		return 1;
	}

	@Override
	public int executeUpdate(String sql, int arg1) {
		return 1;
	}

	@Override
	public int executeUpdate(String sql, int[] arg1) {
		return 1;
	}

	@Override
	public int executeUpdate(String sql, String[] arg1) {
		return 1;
	}

	@Override
	public void setObject(int arg0, Object arg1) {
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2) {
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2, int arg3) {
	}

	@Override
	public void setNull(int arg0, int arg1) {
	}

	@Override
	public void setNull(int arg0, int arg1, String arg2) {
	}

	@Override
	public void setArray(int arg0, Array arg1) throws SQLException {
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
	}

	@Override
	public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
	}

	@Override
	public void setBlob(int arg0, Blob arg1) throws SQLException {
	}

	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException {
	}

	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
	}

	@Override
	public void setBoolean(int arg0, boolean arg1) throws SQLException {
	}

	@Override
	public void setByte(int arg0, byte arg1) throws SQLException {
	}

	@Override
	public void setBytes(int arg0, byte[] arg1) throws SQLException {
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
	}

	@Override
	public void setClob(int arg0, Clob arg1) throws SQLException {
	}

	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException {
	}

	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
	}

	@Override
	public void setDate(int arg0, Date arg1) throws SQLException {
	}

	@Override
	public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
	}

	@Override
	public void setDouble(int arg0, double arg1) throws SQLException {
	}

	@Override
	public void setFloat(int arg0, float arg1) throws SQLException {
	}

	@Override
	public void setInt(int arg0, int arg1) throws SQLException {
	}

	@Override
	public void setLong(int arg0, long arg1) throws SQLException {
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
	}

	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException {
	}

	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException {
	}

	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
	}

	@Override
	public void setNString(int arg0, String arg1) throws SQLException {
	}

	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException {
	}

	@Override
	public void setRef(int arg0, Ref arg1) throws SQLException {
	}

	@Override
	public void setShort(int arg0, short arg1) throws SQLException {
	}

	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
	}

	@Override
	public void setString(int arg0, String arg1) throws SQLException {
	}

	@Override
	public void setTime(int arg0, Time arg1) throws SQLException {
	}

	@Override
	public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
	}

	@Deprecated
	@Override
	public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
	}

	@Override
	public void setURL(int arg0, URL arg1) throws SQLException {
	}

	@Override
	public void addBatch() throws SQLException {
	}

	@Override
	public void addBatch(String arg0) throws SQLException {
	}

	@Override
	public int[] executeBatch() throws SQLException {
		return new int[] {};
	}

}
