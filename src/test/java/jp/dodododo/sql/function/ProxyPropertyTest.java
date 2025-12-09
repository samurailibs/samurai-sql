package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.Assert.*;
import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.impl.Dept;
import jp.dodododo.sql.lazyloading.LazyLoadingProxy;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProxyPropertyTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@Test
	public void testInsertAndSelect() {
		Dao dao = newTestClient(dbTestExtension.getDataSource());
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

		String sql = "select EMPNO, EMP.DEPTNO as DEPTNO,COMM, ENAME, TSTAMP from EMP, DEPT where EMP.deptno = DEPT.deptno and empno = "
				+ empNo + " order by EMPNO";
		List<Emp> select = dao.select(sql, Emp.class);
		assertEquals(empNo, select.get(0).EMPNO);
		assertEquals(Integer.valueOf(2), TypeConverter.convert(select.get(0).COMM, Integer.class));
		assertEquals("ename", select.get(0).NAME);
		assertNotNull(select.get(0).TSTAMP);

		assertEquals(sql, logRegistry.getLast().getCompleteSql());
		assertEquals("10", select.get(0).dept.getDEPTNO());
		assertEqualsIgnoreCase("select * from dept where deptno = 10", logRegistry.getLast().getCompleteSql());
	}

	public static class Emp {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)},
				targetTables = { "emp" })
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

		@Bean(DeptProxy.class)
		public Dept dept;

		public Emp() {
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
			return DeptProxy.dao.selectOne("select * from DEPT where deptno = " + DEPTNO, Dept.class).orElse(null);
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
