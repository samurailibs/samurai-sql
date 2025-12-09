package jp.dodododo.sql.dialect;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.dodododo.sql.paging.LimitOffset;

public class DB2 extends Standard {

	@Override
	public String limitOffsetSql(String originalSQL, LimitOffset limitOffset) {
		long offset = limitOffset.getOffset();
		long limit = limitOffset.getLimit();
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("SELECT HOGEHOGE_DATA.* FROM(SELECT ROW_NUMBER() OVER () HOGEHOGE_ROWNUMBER, HOGEHOGE_ORIGINAL_DATA.* FROM (");
		sqlBuf.append(originalSQL);
		sqlBuf.append(") HOGEHOGE_ORIGINAL_DATA) HOGEHOGE_DATA WHERE HOGEHOGE_ROWNUMBER BETWEEN ");
		sqlBuf.append(offset + 1);
		sqlBuf.append(" AND ");
		sqlBuf.append(offset + limit);
		sqlBuf.append(" ORDER BY HOGEHOGE_ROWNUMBER");
		return sqlBuf.toString();
	}

	@Override
	public String identitySelectSql() {
		return "values IDENTITY_VAL_LOCAL()";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "values nextval for " + sequenceName;
	}

	@Override
	public String getSuffix() {
		return "_db2";
	}

	@Override
	public String toDateString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return "DATE(''" + sdf.format(date) + "')";
	}

	@Override
	public String toTimestampString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "TIMESTAMP(''" + sdf.format(date) + "')";
	}
}
