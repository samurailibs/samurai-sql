package jp.dodododo.sql.impl;

import java.util.List;

public class DeptHasManager {
	private String DEPTNO;

	private String DNAME;

	private List<ManagerHasEmp> manager;

	public String getDEPTNO() {
		return DEPTNO;
	}

	public void setDEPTNO(String deptno) {
		DEPTNO = deptno;
	}

	public String getDNAME() {
		return DNAME;
	}

	public void setDNAME(String dname) {
		DNAME = dname;
	}

	public List<ManagerHasEmp> getManager() {
		return manager;
	}

	public void setManager(List<ManagerHasEmp> manager) {
		this.manager = manager;
	}

}
