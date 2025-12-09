package jp.dodododo.sql.dialect;

public class Sybase extends Standard {

	@Override
	public String identitySelectSql() {
		return "SELECT @@IDENTITY";
	}

	@Override
	public String getSuffix() {
		return "_sybase";
	}
}