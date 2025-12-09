package jp.dodododo.sql.dialect;

import jp.dodododo.sql.paging.LimitOffset;

public class MSSQLServer extends Standard {

	@Override
	public String limitOffsetSql(String originalSQL, LimitOffset limitOffset) {
		long offset = limitOffset.getOffset();
		long limit = limitOffset.getLimit();
		if (0 < offset) {
			throw new UnsupportedOperationException();
		}
		String afterSelect = getAfterSelect(originalSQL);
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("SELECT TOP " + limit + " ");
		sqlBuf.append(afterSelect);
		return sqlBuf.toString();
	}

	private String getAfterSelect(String originalSQL) {
		String lowerCaseSql = originalSQL.toLowerCase();
		int index = lowerCaseSql.indexOf("select ");
		return originalSQL.substring(index + "select ".length());
	}

	@Override
	public String getSuffix() {
		return "_mssql";
	}

	@Override
	public String identitySelectSql() {
		return "select @@identity";
	}
}
