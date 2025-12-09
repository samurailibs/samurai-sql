package jp.dodododo.sql.impl;

import java.util.List;

public class ManagerHasEmp {
	private String MGR_NO;

	private String manager_name;

	private List<Emp> empList;

	public List<Emp> getEmpList() {
		return empList;
	}

	public void setEmpList(List<Emp> empList) {
		this.empList = empList;
	}

	public String getManager_name() {
		return manager_name;
	}

	public void setManager_name(String manager_name) {
		this.manager_name = manager_name;
	}

	public String getMGR_NO() {
		return MGR_NO;
	}

	public void setMGR_NO(String mgr_no) {
		MGR_NO = mgr_no;
	}

}
