package jp.dodododo.dao.dialect;

public class HSQL extends Standard {

	@Override
	public String getSuffix() {
		return "_hsql";
	}

	@Override
	public String identitySelectSql() {
		return "CALL IDENTITY()";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "CALL NEXT VALUE FOR " + sequenceName;
	}

}
