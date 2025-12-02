package jp.dodododo.dao.impl;


import static jp.dodododo.dao.commons.Bool.*;

import jp.dodododo.dao.access.AccessMode;
import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Property;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EmpConstructorHasBeanAnnotatedDept {
	private String EMPNO;

	@Property(AccessMode.WRITE_ONLY)
	private String ENAME;

	@Property(AccessMode.WRITE_ONLY)
	private String JOB;

	@Property(AccessMode.WRITE_ONLY)
	private String MGR;

	@Property(AccessMode.WRITE_ONLY)
	private String HIREDATE;

	@Property(AccessMode.WRITE_ONLY)
	private String SAL;

	@Property(AccessMode.WRITE_ONLY)
	private String COMM;

	@Property(AccessMode.WRITE_ONLY)
	private String DEPTNO;

	@Property(AccessMode.WRITE_ONLY)
	private String TSTAMP;

	private Dept dept;

	private Dept dept2;

	public EmpConstructorHasBeanAnnotatedDept(@Column("EMPNO") String EMPNO, Dept dept, @Bean(TestDept.class) Dept dept2) {
		this.EMPNO = EMPNO;
		this.dept = dept;
		this.dept2 = dept2;
	}

	public EmpConstructorHasBeanAnnotatedDept(int EMPNO, String ENAME) {
	}

	public String getCOMM() {
		return COMM;
	}

	public String getDEPTNO() {
		return DEPTNO;
	}

	public String getEMPNO() {
		return EMPNO;
	}

	public String getENAME() {
		return ENAME;
	}

	public String getHIREDATE() {
		return HIREDATE;
	}

	public String getJOB() {
		return JOB;
	}

	public String getMGR() {
		return MGR;
	}

	public String getSAL() {
		return SAL;
	}

	public String getTSTAMP() {
		return TSTAMP;
	}

	public Dept getDept() {
		return dept;
	}

	public Dept getDept2() {
		return dept2;
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

	public static class TestDept extends Dept {

	}
}
