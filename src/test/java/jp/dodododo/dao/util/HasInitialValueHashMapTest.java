package jp.dodododo.dao.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import jp.dodododo.dao.util.HasInitialValueHashMap.ALF;
import jp.dodododo.dao.util.HasInitialValueHashMap.HMF;

import org.junit.jupiter.api.Test;

public class HasInitialValueHashMapTest {

	@Test
	public void testGet() {
		Map<Long, List<String>> map = new HasInitialValueHashMap<Long, List<String>>(new ALF<String>());
		map.get(0L).add("a");
		map.get(0L).add("b");
		map.get(1L).add("c");
		assertEquals(2, map.get(0L).size());
		assertEquals(1, map.get(1L).size());

		Map<Long, Map<String, Long>> map2 = new HasInitialValueHashMap<Long, Map<String, Long>>(new HMF<String, Long>());
		map2.get(0L).put("a", 1L);
		map2.get(0L).put("a", map2.get(0L).get("a") + 1L);
		map2.get(0L).put("b", 3L);
		map2.get(1L).put("a", 4L);
		System.out.println(map2);
		assertEquals(2L, (long)map2.get(0L).get("a"));
		assertEquals(3L, (long)map2.get(0L).get("b"));
		assertEquals(4L, (long)map2.get(1L).get("a"));
	}
}
