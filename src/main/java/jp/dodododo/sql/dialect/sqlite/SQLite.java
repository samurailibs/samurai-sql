package jp.dodododo.sql.dialect.sqlite;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.dialect.Standard;
import jp.dodododo.sql.paging.LimitOffset;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.types.TypeConverter;

public class SQLite extends Standard {

	@Override
	public String getSuffix() {
		return "_sqlite";
	}

	@Override
	public String limitOffsetSql(String originalSQL, LimitOffset limitOffset) {
		return super.limitOffsetSql(originalSQL, limitOffset);
	}

	@Override
	public BigDecimal getBigDecimal(ResultSet rs, int columnIndex) throws SQLException {
		double value = rs.getDouble(columnIndex);
		return TypeConverter.convert(value, BigDecimal.class);
	}

	@Override
	public String identitySelectSql() {
		return "SELECT LAST_INSERT_ROWID()";
	}

	@Override
	public boolean isPrepareIdentitySelectSql() {
		return false;
	}

	@Override
	public void setBinaryStream(PreparedStatement ps, int index, InputStream inputStream) throws SQLException {
		byte[] bs = JavaTypes.BYTE_ARRAY.convert(inputStream);
		ps.setBytes(index, bs);
	}

	@Override
	public ResultSet resultSet(ResultSet rs) {
		return new SQLiteResultSet(rs);
	}

	@Override
	public boolean isSupportMultiRowInsert() {
		return false;
	}

	@Override
	public String getReplaceCommand() {
		return "INSERT OR REPLACE";
	}

	@Override
	public String getReplaceOption() {
		return "";
	}
}
