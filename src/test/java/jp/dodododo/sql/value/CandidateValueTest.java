package jp.dodododo.sql.value;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.impl.RdbDao;
import jp.dodododo.sql.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CandidateValueTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();

	@Test
	public void testSortLevel() {
		List<CandidateValue> values = new ArrayList<CandidateValue>();

		values.add(new CandidateValue(new ValueProxy("PRIORITY_LEVEL_CONVENTION"), true,
				CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("PRIORITY_LEVEL_RELATIONS_ANNOTATION"), false,
				CandidateValue.PRIORITY_LEVEL_RELATIONS_ANNOTATION));
		values.add(new CandidateValue(new ValueProxy("PRIORITY_LEVEL_COLUMN_ANNOTATION"), false,
				CandidateValue.PRIORITY_LEVEL_COLUMN_ANNOTATION));
		values.add(new CandidateValue(new ValueProxy("PRIORITY_LEVEL_COLUMNS_ANNOTATION"), false,
				CandidateValue.PRIORITY_LEVEL_COLUMNS_ANNOTATION));

		Collections.sort(values, new CandidateValue.Comparator());

		assertEquals("PRIORITY_LEVEL_RELATIONS_ANNOTATION", values.get(0).value.getValue());
		assertEquals("PRIORITY_LEVEL_COLUMNS_ANNOTATION", values.get(1).value.getValue());
		assertEquals("PRIORITY_LEVEL_COLUMN_ANNOTATION", values.get(2).value.getValue());
		assertEquals("PRIORITY_LEVEL_CONVENTION", values.get(3).value.getValue());
	}

	@Test
	public void testSortTable() {
		List<CandidateValue> values = new ArrayList<CandidateValue>();

		values.add(new CandidateValue(new ValueProxy("match"), true, CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("not_match"), false,
				CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("match"), true, CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("not_match"), false,
				CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("not_match"), false,
				CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("not_match"), false,
				CandidateValue.PRIORITY_LEVEL_CONVENTION));
		values.add(new CandidateValue(new ValueProxy("match"), true, CandidateValue.PRIORITY_LEVEL_CONVENTION));

		Collections.sort(values, new CandidateValue.Comparator());

		assertEquals("match", values.get(0).value.getValue());
		assertEquals("match", values.get(1).value.getValue());
		assertEquals("match", values.get(2).value.getValue());
		assertEquals("not_match", values.get(3).value.getValue());
		assertEquals("not_match", values.get(4).value.getValue());
		assertEquals("not_match", values.get(5).value.getValue());
		assertEquals("not_match", values.get(6).value.getValue());
	}

	@Test
	public void test() throws Exception {
		Sub child = new Sub();
		Dao dao = new Dao(getConnection());
		List<CandidateValue> values = new ArrayList<CandidateValue>();
		StringBuilder path = new StringBuilder();
		Map<Integer, Object> processedObjects = new HashMap<Integer, Object>();
		dao.gatherValue(child, "aaa", "order_date", values, path, processedObjects);

		Object value = CandidateValue.getValue(values, null, null).value.getValue();
		assertTrue( value instanceof Date);

		child.d = null;
		values = new ArrayList<CandidateValue>();
		path = new StringBuilder();
		processedObjects = new HashMap<Integer, Object>();
		dao.gatherValue(child, "aaa", "order_date", values, path, processedObjects);
		value = CandidateValue.getValue(values, null, null).value.getValue();
		assertNull(value);
	}

	private Connection getConnection() throws SQLException {
		return dbTestExtension.getConnection();
	}

	public static class Dao extends RdbDao {



		public Dao() {
			super();
		}

		public Dao(Connection connection) {
			super(connection);
		}

		@Override
		public void gatherValue(Object entity, String tableName, String columnName, List<CandidateValue> values, StringBuilder path, Map<Integer, Object> processedObjects) {
			super.gatherValue(entity, tableName, columnName, values, path, processedObjects);
		}

	}

	public static class Super {
		@Column("order_date")
		protected Date d = new Date();

		public Date getD() {
			return d;
		}

		public void setD(Date d) {
			this.d = d;
		}
	}

	public static class Sub extends Super {
		protected String orderDate = "123";

		public String getOrderDate() {
			return orderDate;
		}

		public void setOrderDate(String orderDate) {
			this.orderDate = orderDate;
		}
	}

}
