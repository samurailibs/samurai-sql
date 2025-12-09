package jp.dodododo.sql.util;

import static jp.dodododo.sql.util.CollectionOfString.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.paging.Paging;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionOfStringTest {

	@Test
	public void testList() {
		List<String> list = list("a", "b", "c", "d", "e");
		assertEquals(5, list.size());
		Iterator<String> iterator = list.iterator();
		assertEquals("a", iterator.next());
		assertEquals("b", iterator.next());
		assertEquals("c", iterator.next());
		assertEquals("d", iterator.next());
		assertEquals("e", iterator.next());
	}

	@Test
	public void testMap() {
		Map<String, Object> map = map(new StringBuilder("a"), "b", "c", "d");
		assertEquals(2, map.size());
		assertEquals("b", map.get("a"));
		assertEquals("d", map.get("c"));

		map = map("a", "b", "c", "d", "e");
		assertEquals("e", map.get(null));
	}

	@Test
	public void testMap2() {
		Map<String, Object> map = map(new StringBuilder("a"), "b", "c", "d", map("e", "f"), "g");
		assertEquals("b", map.get("a"));
		assertEquals("d", map.get("c"));
		assertEquals("f", map.get("e"));
		assertEquals("g", map.get(null));
		assertNull(map.get("g"));
	}

	@Test
	public void testMap3() {
		Map<String, Object> map = map(new Paging(1, 1));
		Paging paging = (Paging) map.get(null);
		assertEquals(1, paging.getLimit());
		assertEquals(1, paging.getOffset());
		assertEquals(1, paging.getPageNo());

		map = map("a", "b", new Paging(1, 1));
		assertEquals("b", map.get("a"));
		paging = (Paging) map.get(null);
		assertNotNull(paging);
	}

	@Test
	public void testMap4() {
		Map<String, Object> map = map("null", null, "a", "b");
		assertNull(map.get("null"));
		assertEquals("b", map.get("a"));
	}

}
