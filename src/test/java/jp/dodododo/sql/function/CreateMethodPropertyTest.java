package jp.dodododo.sql.function;

import static jp.dodododo.sql.unit.UnitTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.impl.Dept;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CreateMethodPropertyTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();


	private Dao dao;

	@Test
	public void testInsertAndSelect() {
		dao = newTestClient(dbTestExtension.getDataSource());

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

		List<Emp> select = dao.select(
				"select EMP.deptno as deptno from EMP, DEPT where EMP.deptno = DEPT.deptno and empno = "
						+ empNo, Emp.class);

		assertTrue(select.get(0).dept instanceof Dept2);
		assertEquals("10", select.get(0).dept.getDEPTNO());
	}

	public static class Emp {
		@Id(value = {@IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)},
				targetTables = {"emp"})
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

		@Bean(createMethod = "@jp.dodododo.sql.function.CreateMethodPropertyTest@createDept(resultSet, columnList)")
		public Dept dept;

		public Emp() {
		}
	}

	public static Dept createDept(ResultSet rs, List<ResultSetColumn> columnList) throws SQLException {
		String deptno = rs.getString("DEPTNO");
		assertEquals(1, columnList.size());
		return new Dept2(deptno);
	}

	public static class Dept2 extends Dept {

		public String DEPTNO;

		// this constructor don't have @Column
		public Dept2(String DEPTNO) {
			this.DEPTNO = DEPTNO;
		}

		@Override
		public String getDEPTNO() {
			return super.getDEPTNO();
		}

	}
}
