package jp.dodododo.sql.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapPropertyDesc extends PropertyDesc {

	protected Object value;

	public MapPropertyDesc(MapObjectDesc mapDesc, String propertyName, Object value) {
		super();

		super.objectDesc = mapDesc;
		super.propertyName = propertyName;// StringUtil.camelize(propertyName);
		this.value = value;

		if (value != null) {
			this.genericPropertyType = value.getClass();
			this.propertyType = value.getClass();
		} else {
			this.genericPropertyType = Object.class;
			this.propertyType = Object.class;
		}
		setPropertyType(propertyType);
		super.toString = mapDesc.getKeySet().toString() + propertyName;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V> boolean addValue(Object target, V value) {
		Object element = getValue(target);
		if (element == null) {
			Collection<V> tmp = new ArrayList<>(2);
			tmp.add(value);
			setValue(target, tmp);
			return false;
		} else if (isCollectionType() == true) {
			return ((Collection) element).add(value);
		} else {
			return false;
		}
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotation) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return new Annotation[] {};
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[] {};
	}

	@Override
	public Class<?> getDeclaringClass() {
		return Map.class;
	}

	@Override
	public <A extends Annotation> Class<?> getDeclaringClass(Class<A> annotation) {
		return Map.class;
	}

	@Override
	public Field getField() {
		return null;
	}

	@Override
	public Type getGenericPropertyType() {
		return super.getGenericPropertyType();
	}

	@Override
	public ObjectDesc<?> getObjectDesc() {
		return super.getObjectDesc();
	}

	@Override
	public Class<?> getPropertyType() {
		return super.getPropertyType();
	}

	@Override
	public String getPropertyName() {
		return super.getPropertyName();
	}

	@Override
	public AccessibleObject getReadAccessibleObject() {
		return null;
	}

	@Override
	public Method getReadMethod() {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <VALUE> VALUE getValue(Object target) {
		return (VALUE) ((Map) target).get(getPropertyName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <VALUE> VALUE getValue(Object target, boolean force) {
		return (VALUE) ((Map) target).get(getPropertyName());
	}

	@Override
	public AccessibleObject getWriteAccessibleObject() {
		return null;
	}

	@Override
	public Method getWriteMethod() {
		return null;
	}

	@Override
	public boolean hasField() {
		return false;
	}

	@Override
	public boolean hasReadMethod() {
		return false;
	}

	@Override
	public boolean hasWriteMethod() {
		return false;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return false;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public <K, V> V putValue(Object target, K key, V value) {
		Object element = getValue(target);
		if (element == null) {
			Map<K, V> tmp = new HashMap<>();
			tmp.put(key, value);
			setValue(target, tmp);
			return null;
		} else if (Map.class.isAssignableFrom(getPropertyType())) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			V ret = (V) ((Map) element).put(key, value);
			return ret;
		} else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValue(Object target, Object value) {
		if (value == null) {
			this.genericPropertyType = Object.class;
			this.propertyType = Object.class;
		} else {
			setPropertyType(value.getClass());
		}
		((Map) target).put(propertyName, value);
	}

	@Override
	public void setValue(Object target, Object value, boolean force) {
		setValue(target, value);
	}

}
