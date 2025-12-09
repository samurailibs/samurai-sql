package jp.dodododo.sql.impl;

import jp.dodododo.sql.annotation.NumKey;

public enum EnumDept {
	ACCOUNTING(10), RESEARCH(20), SALES(30), OPERATIONS(40);

	private int deptId;

	private EnumDept(int deptId) {
		this.deptId = deptId;
	}

	@NumKey
	public double num() {
		return deptId;
	}
}
