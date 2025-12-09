package jp.dodododo.dao.util;

import jp.dodododo.dao.id.EntityId;
import jp.dodododo.dao.object.ObjectDesc;
import jp.dodododo.dao.object.ObjectDescFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class CollectionOfString {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> map(Object... map) {
		Map<String, Object> ret = new CaseInsensitiveMap<>(){
			@Override
			public Object put(String key, Object val) {
				return super.put(key, normalize(val));
			}
		};
		for (int i = 0; i < map.length; i++) {
			Object o1 = map[i];
			Object o2 = null;
			if (i + 1 < map.length) {
				o2 = map[i + 1];
			}
			if (o1 instanceof Map) {
				Map tmpMap = (Map) o1;
				ret.putAll(tmpMap);
				continue;
			}
			if (o1 instanceof Record) {
				putRecord(ret, o1);
				continue;
			}

			String key = null;
			Object val = o2;
			if (o1 instanceof CharSequence) {
				key = o1.toString();
				ret.put(key, val);
			} else if (o1 != null) {
				ret.put(null, o1);
			}
			if (key != null && val != null) {
				i++;
			} else if (key != null) {
				ret.put(null, key);
			}
		}

		return ret;
	}

	private static void putRecord(Map<String, Object> ret, Object record) {
		ObjectDesc<Object> desc = ObjectDescFactory.getObjectDesc(record);
		desc.getPropertyDescs().forEach(pd -> {
			if (pd.isReadable()) {
				ret.put(pd.getPropertyName(), pd.getValue(record));
			}
		});
	}

	private static Object normalize(Object v) {
		return (v instanceof EntityId id) ? id.raw() : v;
	}

	public static List<String> list(String... list) {
		List<String> ret = new ArrayList<>(list.length);
		for (int i = 0; i < list.length; i++) {
			String value = list[i];
			ret.add(value);
		}
		return ret;
	}
}
