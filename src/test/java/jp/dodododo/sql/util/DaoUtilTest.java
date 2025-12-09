package jp.dodododo.sql.util;

import static jp.dodododo.sql.sql.orderby.SortType.*;
import static jp.dodododo.sql.util.DaoUtil.*;

import java.util.List;
import java.util.Set;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Columns;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.sql.orderby.OrderByArg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DaoUtilTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testOrderBy() {

		List<OrderByArg> orderBy = (List<OrderByArg>) orderBy("a", "b").get(ORDER_BY);
		assertEquals(2, orderBy.size());
		assertEquals("a", orderBy.get(0).getColumnName());
		assertNull(orderBy.get(0).getSortType());
		assertEquals("b", orderBy.get(1).getColumnName());
		assertNull(orderBy.get(1).getSortType());

		orderBy = (List<OrderByArg>) orderBy("a", ASC, "b", DESC).get(ORDER_BY);
		assertEquals(2, orderBy.size());
		assertEquals("a", orderBy.get(0).getColumnName());
		assertEquals(ASC, orderBy.get(0).getSortType());
		assertEquals("b", orderBy.get(1).getColumnName());
		assertEquals(DESC, orderBy.get(1).getSortType());

		orderBy = (List<OrderByArg>) orderBy("a", ASC, "b", "c").get(ORDER_BY);
		assertEquals(3, orderBy.size());
		assertEquals("a", orderBy.get(0).getColumnName());
		assertEquals(ASC, orderBy.get(0).getSortType());
		assertEquals("b", orderBy.get(1).getColumnName());
		assertNull(orderBy.get(1).getSortType());
		assertEquals("c", orderBy.get(2).getColumnName());
		assertNull(orderBy.get(2).getSortType());

		orderBy = (List<OrderByArg>) orderBy("a", "b", ASC, "c").get(ORDER_BY);
		assertEquals(3, orderBy.size());
		assertEquals("a", orderBy.get(0).getColumnName());
		assertNull(orderBy.get(0).getSortType());
		assertEquals("b", orderBy.get(1).getColumnName());
		assertEquals(ASC, orderBy.get(1).getSortType());
		assertEquals("c", orderBy.get(2).getColumnName());
		assertNull(orderBy.get(2).getSortType());

		orderBy = (List<OrderByArg>) orderBy("a", "b", "c", ASC).get(ORDER_BY);
		assertEquals(3, orderBy.size());
		assertEquals("a", orderBy.get(0).getColumnName());
		assertNull(orderBy.get(0).getSortType());
		assertEquals("b", orderBy.get(1).getColumnName());
		assertNull(orderBy.get(1).getSortType());
		assertEquals("c", orderBy.get(2).getColumnName());
		assertEquals(ASC, orderBy.get(2).getSortType());
	}

	@Test
	public void testGetTableNames() {
		PropertyDesc pd = ObjectDescFactory.getObjectDesc(TestBean.class).getPropertyDesc("foo");
		Set<String> tableNames = getTableNames(pd, "col");
		System.out.println(tableNames);
		assertTrue(tableNames.contains("a"));
		assertTrue(tableNames.contains("b"));
		assertFalse(tableNames.contains("c"));
		assertFalse(tableNames.contains("d"));
		assertFalse(tableNames.contains("e_super"));
		assertFalse(tableNames.contains("eSuper"));
		assertFalse(tableNames.contains("x"));

		pd = ObjectDescFactory.getObjectDesc(TestBean2.class).getPropertyDesc("foo");
		tableNames = getTableNames(pd, "col");
		System.out.println(tableNames);
		assertFalse(tableNames.contains("a"));
		assertFalse(tableNames.contains("b"));
		assertTrue(tableNames.contains("c"));
		assertFalse(tableNames.contains("d"));
		assertFalse(tableNames.contains("e_super"));
		assertFalse(tableNames.contains("eSuper"));
		assertFalse(tableNames.contains("x"));

		pd = ObjectDescFactory.getObjectDesc(TestBean3.class).getPropertyDesc("foo");
		tableNames = getTableNames(pd, "col");
		System.out.println(tableNames);
		assertFalse(tableNames.contains("a"));
		assertFalse(tableNames.contains("b"));
		assertFalse(tableNames.contains("c"));
		assertTrue(tableNames.contains("d"));
		assertTrue(tableNames.contains("e_super"));
		assertTrue(tableNames.contains("eSuper"));
		assertFalse(tableNames.contains("x"));
}

	@Table("d")
	public static class TestBean extends Super {

		@Columns({ @Column(table = "a", value = "col"), @Column(table = "x", value = "zxc"), @Column(table = "b", value = "col") })
		@Column(table = "c", value = "col")
		private String foo;

		public String getFoo() {
			return foo;
		}
	}

	@Table("d")
	public static class TestBean2 extends Super {

		@Column(table = "c", value = "col")
		private String foo;

		public String getFoo() {
			return foo;
		}
	}

	@Table("d")
	public static class TestBean3 extends Super {

		private String foo;

		public String getFoo() {
			return foo;
		}
	}

	@Table("e_super")
	public static class Super {
	}

}
