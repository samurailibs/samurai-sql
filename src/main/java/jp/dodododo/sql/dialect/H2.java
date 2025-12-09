package jp.dodododo.sql.dialect;

public class H2 extends Standard {

	@Override
	public String getSuffix() {
		return "_h2";
	}

	@Override
	public String identitySelectSql() {
		return "CALL IDENTITY()";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "CALL NEXTVAL('" + sequenceName + "')";
	}
}
