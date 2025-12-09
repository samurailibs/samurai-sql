package jp.dodododo.sql.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.impl.Emp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapPropertyDescTest {

	@Test
	public void testMapPropertyDesc() {

		Map<String, Object> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", "str");
		map.put("c", new Emp());
		map.put("d", null);
		map.put("list", new ArrayList<String>());
		map.put("map", new HashMap<String, String>());

		MapObjectDesc objectDesc = new MapObjectDesc(map, true);
		PropertyDesc aPropertyDesc = objectDesc.getPropertyDesc("a");
		PropertyDesc bPropertyDesc = objectDesc.getPropertyDesc("b");
		@SuppressWarnings("unused")
		PropertyDesc cPropertyDesc = objectDesc.getPropertyDesc("c");
		PropertyDesc dPropertyDesc = objectDesc.getPropertyDesc("d");
		PropertyDesc listPropertyDesc = objectDesc.getPropertyDesc("list");
		PropertyDesc mapPropertyDesc = objectDesc.getPropertyDesc("map");

		assertEquals(1, (int)aPropertyDesc.getValue(map));
		assertEquals("str", bPropertyDesc.getValue(map));

		aPropertyDesc.setValue(map, 2);
		assertEquals(2, (int)aPropertyDesc.getValue(map));
		aPropertyDesc.setValue(map, 1);
		assertEquals(1, (int)aPropertyDesc.getValue(map));

		listPropertyDesc.addValue(map, "a");
		listPropertyDesc.addValue(map, "b");
		List<String> list = listPropertyDesc.getValue(map);
		assertEquals(2, list.size());
		assertEquals("a", list.get(0));
		assertEquals("b", list.get(1));

		assertNull(map.get("d"));
		dPropertyDesc.addValue(map, "a");
		dPropertyDesc.addValue(map, "b");
		List<String> dList = dPropertyDesc.getValue(map);
		assertEquals(2, dList.size());
		assertEquals("a", dList.get(0));
		assertEquals("b", dList.get(1));
		dPropertyDesc.setValue(map, null);

		mapPropertyDesc.putValue(map, "key1", "val1");
		mapPropertyDesc.putValue(map, "key2", "val2");
		Map<String, String> map2 = mapPropertyDesc.getValue(map);
		assertEquals(2, map2.size());
		assertEquals("val1", map2.get("key1"));
		assertEquals("val2", map2.get("key2"));

		assertNull(map.get("d"));
		dPropertyDesc.putValue(map, "key1", "val1");
		dPropertyDesc.putValue(map, "key2", "val2");
		Map<String, String> dMap = dPropertyDesc.getValue(map);
		assertEquals(2, dMap.size());
		assertEquals("val1", dMap.get("key1"));
		assertEquals("val2", dMap.get("key2"));
		dPropertyDesc.setValue(map, null);
	}

}
