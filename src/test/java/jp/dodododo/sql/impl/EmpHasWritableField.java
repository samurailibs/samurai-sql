package jp.dodododo.sql.impl;

import static jp.dodododo.sql.commons.Bool.*;

import jp.dodododo.sql.access.AccessMode;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Property;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EmpHasWritableField {
	private String EMPNO;

	@Property(AccessMode.WRITE_ONLY)
	private String ENAME;

	private String JOB;

	private String MGR;

	private String HIREDATE;

	private String SAL;

	private String COMM;

	private String DEPTNO;

	@Property(AccessMode.WRITE_ONLY)
	private String TSTAMP;

	public EmpHasWritableField(@Column("EMPNO") String EMPNO) {
		this.EMPNO = EMPNO;
	}

	public EmpHasWritableField(String EMPNO, String ENAME) {
		this.EMPNO = EMPNO;
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
