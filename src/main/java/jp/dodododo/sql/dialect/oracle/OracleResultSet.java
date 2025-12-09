package jp.dodododo.sql.dialect.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.wrapper.ResultSetWrapper;

public class OracleResultSet extends ResultSetWrapper {

	/**
	 * WAVE DASHです。
	 */
	public static final int WAVE_DASH = 0x301c;

	/**
	 * FULLWIDTH TILDEです。
	 */
	public static final int FULLWIDTH_TILDE = 0xff5e;

	public OracleResultSet(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return convert(super.getString(columnIndex));
	}

	@Override
	public String getString(String columnName) throws SQLException {
		return convert(super.getString(columnName));
	}

	protected String convert(String source) {
		if (source == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(source.length());
		for (int i = 0; i < source.length(); i++) {
			char ch = source.charAt(i);
			ch = convert(ch);
			result.append(ch);
		}

		return result.toString();
	}

	protected char convert(char source) {
		switch (source) {
		case WAVE_DASH:
			return FULLWIDTH_TILDE;
		default:
			return source;
		}
	}
}