package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.Assert.*;
import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.access.AccessMode;
import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Property;
import jp.dodododo.dao.dialect.MySQL;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.id.Identity;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.impl.Dept;
import jp.dodododo.dao.lazyloading.LazyLoadingProxy;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProxyTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestDao(dbTestExtension.getDataSource());
		DeptProxy.dao = dao;
		SqlLogRegistry logRegistry = dao.getSqlLogRegistry();

		Emp emp = new Emp();
		emp.dept = new Dept();
		emp.dept.setDEPTNO("10");
		emp.dept.setDNAME("dept__name");
		emp.COMM = "2";
		// emp.EMPNO = "1";
		emp.TSTAMP = null;
		emp.TSTAMP = new Date();
		emp.NAME = "ename";
		dao.insert("emp", emp);
		// dao.insert(emp.dept);
		String empNo = emp.EMPNO;

		String sql = "select DEPT.deptno as DEPTNO, ename, comm, tstamp, EMPNO from EMP, DEPT where EMP.deptno = DEPT.deptno and empno = " + empNo;
		List<Emp> select = dao.select(sql, Emp.class);
		assertEquals(empNo, select.get(0).EMPNO);
		assertEquals(Integer.valueOf(2), TypeConverter.convert(select.get(0).COMM, Integer.class));
		assertEquals("ename", select.get(0).NAME);
		assertNotNull(select.get(0).TSTAMP);

		assertEquals(sql, logRegistry.getLast()
				.getCompleteSql());
		assertEquals("10", select.get(0).dept.getDEPTNO());
		assertEqualsIgnoreCase("select * from dept where deptno =10", logRegistry.getLast().getCompleteSql());
	}

	public static class Emp {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) }, targetTables = { "emp" })
		public String EMPNO;

		@Column("ename")
		public String NAME;

		@Column(table = "emp", value = "Tstamp")
		public Date TSTAMP;

		public String JOB;

		public String MGR;

		public String HIREDATE;

		public String SAL;

		public String COMM;

		@Property(AccessMode.READ_ONLY)
		private Dept dept;

		public Emp() {
		}

		public Emp(@Bean(DeptProxy.class) Dept dept) {
			this.dept = dept;
		}

	}

	public static class DeptProxy extends Dept implements LazyLoadingProxy<Dept> {
		private static Dao dao;

		@Column("deptNO")
		public String DEPTNO;

		private Dept real;

		public DeptProxy(@Column("deptNO") String DEPTNO) {
			this.DEPTNO = DEPTNO;
		}

	    @Override
		public Dept lazyLoad() {
			return DeptProxy.dao.selectOne("select * from DEPT where deptno =" + DEPTNO, Dept.class).get();
		}

	    @Override
		public Dept real() {
			if (real == null) {
				real = lazyLoad();
			}
			return real;
		}

		@Override
		public String getDEPTNO() {
			return real().getDEPTNO();
		}

		@Override
		public void setDEPTNO(String deptno) {
			real().setDEPTNO(deptno);
		}

		@Override
		public String getDNAME() {
			return real().getDNAME();
		}

		@Override
		public void setDNAME(String dname) {
			real().setDNAME(dname);
		}

		@Override
		public String getLOC() {
			return real().getLOC();
		}

		@Override
		public void setLOC(String loc) {
			real().setLOC(loc);
		}

		@Override
		public String getVERSIONNO() {
			return real().getVERSIONNO();
		}

		@Override
		public void setVERSIONNO(String versionno) {
			real().setVERSIONNO(versionno);
		}

		@Override
		public String toString() {
			return real().toString();
		}

		@Override
		public boolean equals(Object o) {
			return real().equals(o);
		}

		@Override
		public int hashCode() {
			return real().hashCode();
		}

	    @Override
		public void setReal(Dept real) {
		}

	}
}
