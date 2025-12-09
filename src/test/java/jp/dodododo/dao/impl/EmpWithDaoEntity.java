package jp.dodododo.dao.impl;

import jp.dodododo.dao.annotation.DaoEntity;
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
@DaoEntity
public class EmpWithDaoEntity {
	@Id({@IdDefSet(strategy = Sequence.class, name = "sequence", db = HSQL.class)})
	private String empno;

	private String ename;

	private String job;

	private String mgr;

	private String hiredate;

	private String sal;

	private String comm;

	private Dept dept;

	private String tstamp;

	public EmpWithDaoEntity(String empno, Dept dept) {
		this.empno = empno;
		this.dept = dept;
	}

	public void init(String name , String job, String mgr){
		this.ename = name;
		this.job = job;
		this.mgr = mgr;
	}

	public EmpWithDaoEntity() {
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

	public String getEmpNo() {
		return empno;
	}

	public String jobName() {
		return job;
	}

	public String ename() {
		return this.ename;
	}

	public Dept dept() {
		return dept;
	}
}
