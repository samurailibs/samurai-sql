package jp.dodododo.dao.util;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.dodododo.dao.metadata.TableMetaData;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DBUtilTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@Test
	public void testGetTableNames() throws Exception {
		Map<String, String> tableNames = DBUtil.getTableNames(getConnection());
		assertTrue(0 < tableNames.size());
	}

	@Test
	public void testGetViewNames() throws Exception {
		Map<String, String> viewNames = DBUtil.getViewNames(getConnection());
		List<String> viewList = new ArrayList<String>(viewNames.values());
		assertTrue(viewList.contains("EMP_VIEW") ||viewList.contains("emp_view") );

		TableMetaData tableMetaData = new TableMetaData(getConnection(), "EMP_VIEW");
		List<String> columnNames = tableMetaData.getColumnNames();
		assertTrue(columnNames.contains("EMPNO"));
		assertTrue(columnNames.contains("ENAME"));
		assertTrue(columnNames.contains("JOB"));
		assertTrue(columnNames.contains("MGR"));
		assertTrue(columnNames.contains("HIREDATE"));
		assertTrue(columnNames.contains("SAL"));
		assertTrue(columnNames.contains("COMM"));
		assertTrue(columnNames.contains("DEPTNO"));
		assertTrue(columnNames.contains("TSTAMP"));
	}

	private Connection getConnection() {
		return dbTestExtension.getConnection();
	}
}
