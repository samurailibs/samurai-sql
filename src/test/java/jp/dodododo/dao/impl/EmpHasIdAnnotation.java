package jp.dodododo.dao.impl;

import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.dialect.HSQL;
import jp.dodododo.dao.id.Sequence;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Table("emp")
public class EmpHasIdAnnotation {
	private String EMPNO;

	private String ENAME;

	private String JOB;

	private String MGR;

	private String HIREDATE;

	private String SAL;

	private String COMM;

	private String DEPTNO;

	private String TSTAMP;

	@Id( { @IdDefSet(strategy = Sequence.class, name = "sequence", db = HSQL.class) })
	public void setEMPNO(String empno) {
		EMPNO = empno;
	}

	public String getCOMM() {
		return COMM;
	}

	public void setCOMM(String comm) {
		COMM = comm;
	}

	public String getDEPTNO() {
		return DEPTNO;
	}

	public void setDEPTNO(String deptno) {
		DEPTNO = deptno;
	}

	public String getEMPNO() {
		return EMPNO;
	}

	public String getENAME() {
		return ENAME;
	}

	public void setENAME(String ename) {
		ENAME = ename;
	}

	public String getHIREDATE() {
		return HIREDATE;
	}

	public void setHIREDATE(String hiredate) {
		HIREDATE = hiredate;
	}

	public String getJOB() {
		return JOB;
	}

	public void setJOB(String job) {
		JOB = job;
	}

	public String getMGR() {
		return MGR;
	}

	public void setMGR(String mgr) {
		MGR = mgr;
	}

	public String getSAL() {
		return SAL;
	}

	public void setSAL(String sal) {
		SAL = sal;
	}

	public String getTSTAMP() {
		return TSTAMP;
	}

	public void setTSTAMP(String tstamp) {
		TSTAMP = tstamp;
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
