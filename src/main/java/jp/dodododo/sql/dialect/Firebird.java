package jp.dodododo.sql.dialect;

public class Firebird extends Standard {

	@Override
	public String getSuffix() {
		return "_firebird";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "select gen_id( " + sequenceName + ", 1 ) from RDB$DATABASE";
	}

	@Override
	public String getReplaceCommand() {
		return "UPDATE OR INSERT";
	}

	@Override
	public String getReplaceOption() {
		return "";
	}
}
