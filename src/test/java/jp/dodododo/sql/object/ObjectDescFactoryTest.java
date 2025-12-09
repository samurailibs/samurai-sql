package jp.dodododo.sql.object;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ObjectDescFactoryTest extends ObjectDescFactory {

	@Test
	public void testGetObjectDescMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("cDe", 2);
		map.put("e_fg", 2);

		ObjectDesc<Map<String, Object>> objectDesc = ObjectDescFactory.getObjectDesc(map);
		assertTrue(MapObjectDesc.class.isAssignableFrom(objectDesc.getClass()));

		Set<String> result = new HashSet<String>();
		PropertyDesc propertyDesc = objectDesc.getPropertyDesc(0);
		result.add(propertyDesc.getPropertyName());
		PropertyDesc propertyDesc2 = objectDesc.getPropertyDesc(1);
		result.add(propertyDesc2.getPropertyName());
		PropertyDesc propertyDesc3 = objectDesc.getPropertyDesc(2);
		result.add(propertyDesc3.getPropertyName());
		PropertyDesc propertyDesc4 = objectDesc.getPropertyDesc(3);
		result.add(propertyDesc4.getPropertyName());
		assertTrue(result.contains("a"));
		assertTrue(result.contains("b"));
		assertTrue(result.contains("cDe"));
		assertTrue(result.contains("e_fg"));
	}
}
