package jp.dodododo.sql.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CaseInsensitiveMapTest {

	@Test
	public void testPutGet() throws Exception {
		CaseInsensitiveMap<String> map = null;

		map = new CaseInsensitiveMap<String>();
		map.put("a", "b");
		assertEquals("b", map.get("a"));
		assertEquals("b", map.get("A"));

		map = new CaseInsensitiveMap<String>();
		map.put("b", "C");
		assertEquals("C", map.get("b"));
		assertEquals("C", map.get("B"));

		map = new CaseInsensitiveMap<String>();
		map.put("a_b", "x");
		assertEquals("x", map.get("a_b"));
		assertEquals("x", map.get("A_b"));
		assertEquals("x", map.get("a_B"));
		assertEquals("x", map.get("A_B"));
		assertEquals("x", map.get("aB"));

		map = new CaseInsensitiveMap<String>();
		map.put("a_B", "x");
		assertEquals("x", map.get("a_b"));
		assertEquals("x", map.get("A_b"));
		assertEquals("x", map.get("a_B"));
		assertEquals("x", map.get("A_B"));
		assertEquals("x", map.get("aB"));

		map = new CaseInsensitiveMap<String>();
		map.put("A_b", "x");
		assertEquals("x", map.get("a_b"));
		assertEquals("x", map.get("A_b"));
		assertEquals("x", map.get("a_B"));
		assertEquals("x", map.get("A_B"));
		assertEquals("x", map.get("aB"));

		map = new CaseInsensitiveMap<String>();
		map.put("A_B", "x");
		assertEquals("x", map.get("a_b"));
		assertEquals("x", map.get("A_b"));
		assertEquals("x", map.get("a_B"));
		assertEquals("x", map.get("A_B"));
		assertEquals("x", map.get("aB"));

		map = new CaseInsensitiveMap<String>();
		map.put("bC", "y");
		assertEquals("y", map.get("b_c"));
		assertEquals("y", map.get("b_C"));
		assertEquals("y", map.get("B_c"));
		assertEquals("y", map.get("bC"));
		assertEquals("y", map.get("B_C"));
		assertEquals("y", map.get("b_c"));
	}
}
