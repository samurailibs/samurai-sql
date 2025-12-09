package jp.dodododo.sql.dialect;

import jp.dodododo.sql.paging.LimitOffset;

public class Derby extends Standard {

	@Override
	public String identitySelectSql() {
		return "values IDENTITY_VAL_LOCAL()";
	}

	@Override
	public String sequenceNextValSql(String sequenceName) {
		return "values IDENTITY_VAL_LOCAL()";
	}

	public boolean isSelfGenerate() {
		return false;
	}

	@Override
	public String getSuffix() {
		return "_derby";
	}

	@Override
	public String limitOffsetSql(String originalSQL, LimitOffset limitOffset) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
