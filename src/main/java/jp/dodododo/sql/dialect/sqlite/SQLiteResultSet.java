package jp.dodododo.sql.dialect.sqlite;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.wrapper.ResultSetWrapper;

public class SQLiteResultSet extends ResultSetWrapper {

	public SQLiteResultSet(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public InputStream getBinaryStream(int i) throws SQLException {
		byte[] bytes = super.getBytes(i);
		return JavaTypes.INPUT_STREAM.convert(bytes);
	}

	@Override
	public InputStream getBinaryStream(String s) throws SQLException {
		byte[] bytes = super.getBytes(s);
		return JavaTypes.INPUT_STREAM.convert(bytes);
	}

	@Override
	public BigDecimal getBigDecimal(int i) throws SQLException {
		int val = super.rs.getInt(i);
		if (super.rs.wasNull()) {
			return null;
		}
		return JavaTypes.BIG_DECIMAL.convert(val);
	}

	@Override
	public BigDecimal getBigDecimal(String s) throws SQLException {
		int val = super.rs.getInt(s);
		if (super.rs.wasNull()) {
			return null;
		}
		return JavaTypes.BIG_DECIMAL.convert(val);
	}
}