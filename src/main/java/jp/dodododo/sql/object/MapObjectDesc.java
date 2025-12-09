package jp.dodododo.sql.object;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import jp.dodododo.sql.types.JavaType;

@SuppressWarnings("rawtypes")
public class MapObjectDesc extends ObjectDesc<Map> {

	private Map<String, Object> target = null;

	private Set<String> keySet = new TreeSet<String>();

	public MapObjectDesc(Map<String, Object> map, boolean isIgnoreCase) {
		super(isIgnoreCase);
		this.target = map;
		init();
	}

	private void init() {
		this.targetClass = Map.class;
		this.classDesc = new ClassDesc<Map>(Map.class);
		setupPropertyDescs();
		super.setupJavaTypeCache();
		for (String key : target.keySet()) {
			if (key != null) {
				keySet.add(key);
			}
		}
	}

	public Set<String> getKeySet() {
		return keySet;
	}

	@Override
	protected void setupPropertyDescs() {
		Set<Entry<String, Object>> entrySet = this.target.entrySet();
		entrySet.forEach(entry -> {
			String propertyName = entry.getKey();
			Object value = entry.getValue();
			MapPropertyDesc propertyDesc = new MapPropertyDesc(this, propertyName, value);
			propertyDescList.add(propertyDesc);
			writablePropertyDescList.add(propertyDesc);
			readablePropertyDescList.add(propertyDesc);

			propertyDescCache.put(propertyName, propertyDesc);
		});
	}

	@Override
	public <T extends Annotation> PropertyDesc getPropertyDesc(Class<T> annotation) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Annotation> List<PropertyDesc> getPropertyDescs(Class<A> annotation) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public <JAVA_TYPE> List<PropertyDesc> getPropertyDescs(JavaType<JAVA_TYPE> javaType) {
		return super.getPropertyDescs(javaType);
	}

	@Override
	public int getPropertyDescSize() {
		return super.getPropertyDescSize();
	}

	@Override
	public List<PropertyDesc> getReadablePropertyDescs() {
		return super.getReadablePropertyDescs();
	}

	@Override
	public Class<?> getTargetClass() {
		return Map.class;
	}

	@Override
	public List<PropertyDesc> getWritablePropertyDescs() {
		return super.getWritablePropertyDescs();
	}

	@Override
	public <A extends Annotation> boolean hasPropertyDesc(Class<A> annotation) {
		return false;
	}

	@Override
	public PropertyDesc getPropertyDesc(String propertyName) {
		return super.getPropertyDesc(propertyName);
	}

	@Override
	public PropertyDesc getPropertyDesc(int index) {
		return super.getPropertyDesc(index);
	}

	@Override
	public PropertyDesc getPropertyDescNoException(String propertyName) {
		return super.getPropertyDescNoException(propertyName);
	}

	@Override
	public ClassDesc<Map> getClassDesc() {
		return super.getClassDesc();
	}

	@Override
	public List<PropertyDesc> getPropertyDescs() {
		return super.getPropertyDescs();
	}

	@Override
	public boolean hasPropertyDesc(String propertyName) {
		return super.hasPropertyDesc(propertyName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> toMap(Object obj) {
		if (obj instanceof Map) {
			return (Map) obj;
		}
		return super.toMap(obj);
	}
}
