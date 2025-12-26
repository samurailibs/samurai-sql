package jp.dodododo.sql.issue;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.annotation.NumKey;
import jp.dodododo.sql.annotation.StringKey;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.types.JavaTypes.EnumType;
import jp.dodododo.sql.unit.DbTestExtension;
import jp.dodododo.sql.util.EnumConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Issue12Test {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@BeforeEach
	public void setUp() throws Exception {
		SqlConfig.getDefaultConfig().setFormats("yyyy/MM/dd", "yyyy-MM-dd");
	}

	@Test
	public void test() throws Exception {
		Connection connection = dbTestExtension.getConnection();
		test(connection, Emp.SMITH);
		test(connection, Emp.ALLEN);
		connection.close();
	}

	@Test
	public void test2() {
		try {
			EnumConverter.convert(1, Emp.class);
			fail();
		} catch (IllegalArgumentException success) {
		}
		try {
			EnumConverter.convert("1", Emp.class);
			fail();
		} catch (IllegalArgumentException success) {
		}
	}

	private void test(Connection connection, Emp emp) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM EMP WHERE empno = " + emp.getEMPNO());
		ResultSet rs = ps.executeQuery();
		rs.next();
		EnumType<Emp> enumType = new EnumType<Emp>(Emp.class);
		Emp value1 = enumType.getValue(rs, "EMPNO");
		Emp value2 = enumType.getValue(rs, "ENAME");
		assertEquals(emp, value1);
		assertEquals(emp, value2);
		rs.close();
		ps.close();
	}

	public static enum Emp {
		SMITH(7369, "SMITH"), ALLEN(7499, "ALLEN");

		private int EMPNO;
		private String ENAME;

		private Emp(int EMPNO, String ENAME) {
			this.EMPNO = EMPNO;
			this.ENAME = ENAME;
		}

		@NumKey
		public int getEMPNO() {
			return EMPNO;
		}

		@StringKey
		public String getENAME() {
			return ENAME;
		}

	}

}
