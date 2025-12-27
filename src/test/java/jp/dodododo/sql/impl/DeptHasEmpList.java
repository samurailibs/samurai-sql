package jp.dodododo.sql.impl;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DeptHasEmpList {
	private String DEPTNO;

	private String DNAME;

	private String LOC;

	private String VERSIONNO;

	private List<Emp> empList;

	public List<Emp> getEmpList() {
		return empList;
	}

	public DeptHasEmpList(String DEPTNO, List<Emp> empList) {
		this.DEPTNO= DEPTNO;
		this.empList= empList;
	}

	public void setEmpList(List<Emp> empList) {
		this.empList = empList;
	}

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

	public String getLOC() {
		return LOC;
	}

	public void setLOC(String loc) {
		LOC = loc;
	}

	public String getVERSIONNO() {
		return VERSIONNO;
	}

	public void setVERSIONNO(String versionno) {
		VERSIONNO = versionno;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
