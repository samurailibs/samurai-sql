package jp.dodododo.sql.dialect;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.dodododo.sql.dialect.oracle.OracleResultSet;
import jp.dodododo.sql.paging.LimitOffset;

public class Oracle extends Standard {

	@Override
	public String limitOffsetSql(String originalSQL, LimitOffset limitOffset) {
		long offset = limitOffset.getOffset();
		long limit = limitOffset.getLimit();
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("SELECT * FROM (SELECT ROWNUM AS HOGEHOGE_ROWNUMBER, HOGEHOGE_ORIGINAL_DATA.* FROM (");
		sqlBuf.append(originalSQL);
		sqlBuf.append(") HOGEHOGE_ORIGINAL_DATA) WHERE HOGEHOGE_ROWNUMBER BETWEEN ");
		sqlBuf.append(offset + 1);
		sqlBuf.append(" AND ");
		sqlBuf.append(offset + limit);
		sqlBuf.append(" ORDER BY HOGEHOGE_ROWNUMBER");
		return sqlBuf.toString();
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
	}

	@Override
	public String getSuffix() {
		return "_oracle";
	}

	@Override
	public ResultSet resultSet(ResultSet rs) {
		return new OracleResultSet(rs);
	}

	@Override
	public String toDateString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return "TO_DATE(''" + sdf.format(date) + "', 'YYYY-MM-DD')";
	}

	@Override
	public String toTimestampString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "TO_DATE(''" + sdf.format(date) + "', 'YYYY-MM-DD HH24:MI:SS')";
	}
}
