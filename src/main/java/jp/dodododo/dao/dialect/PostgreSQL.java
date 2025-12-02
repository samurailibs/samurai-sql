package jp.dodododo.dao.dialect;

public class PostgreSQL extends Standard {

	@Override
	public String getSuffix() {
		return "_postgre";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "select nextval ('" + sequenceName + "')";
	}

	@Override
	public TableNameResolver getTableNameResolver() {
		return StrictTableNameResolver.INSTANCE;
	}
}