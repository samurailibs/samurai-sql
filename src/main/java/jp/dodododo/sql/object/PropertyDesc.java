package jp.dodododo.sql.object;

import static jp.dodododo.janerics.GenericsTypeUtil.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jp.dodododo.sql.access.AccessMode;
import jp.dodododo.sql.annotation.Bean;
import jp.dodododo.sql.annotation.SqlEntity;
import jp.dodododo.sql.annotation.Internal;
import jp.dodododo.sql.annotation.Property;
import jp.dodododo.sql.exception.IllegalPropertyRuntimeException;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.object.aop.field.FieldAccess;
import jp.dodododo.sql.object.aop.field.FieldAccess.AccessType;
import jp.dodododo.sql.object.aop.field.FieldInterceptor;
import jp.dodododo.sql.object.aop.field.FieldInterceptors;
import jp.dodododo.sql.types.JavaType;
import jp.dodododo.sql.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Internal
public class PropertyDesc implements AnnotatedElement {

	private static final Logger logger = LoggerFactory.getLogger(PropertyDesc.class);

	protected String propertyName;

	protected Class<?> propertyType;

	protected Type genericPropertyType;

	private Method readMethod;

	private Method writeMethod;

	private Field field;

	protected ObjectDesc<?> objectDesc;

	private boolean readable = false;

	private boolean writable = false;

	private boolean isOneDimensionalArray;

	private Collection<Class<? extends Throwable>> ignoreExceptions = new ArrayList<>();

	protected String toString;

	protected PropertyDesc() {
	}

	private PropertyDesc(String propertyName, ObjectDesc<?> objectDesc) {
		if (propertyName == null) {
			throw new NullPointerException("propertyName");
		}
		this.propertyName = StringUtil.decapitalize(propertyName);
		this.objectDesc = objectDesc;
	}

	public Class<?> getDeclaringClass() {
		if (readMethod != null) {
			return readMethod.getDeclaringClass();
		}
		if (writeMethod != null) {
			return writeMethod.getDeclaringClass();
		}
		if (field != null) {
			return field.getDeclaringClass();
		}
		throw new IllegalStateException();
	}

	public <A extends Annotation> Class<?> getDeclaringClass(Class<A> annotation) {
		if (readMethod != null && readMethod.getAnnotation(annotation) != null) {
			return readMethod.getDeclaringClass();
		}
		if (writeMethod != null && writeMethod.getAnnotation(annotation) != null) {
			return writeMethod.getDeclaringClass();
		}
		if (field != null && field.getAnnotation(annotation) != null) {
			return field.getDeclaringClass();
		}
		return null;
	}

	protected PropertyDesc(String propertyName, Method readMethod, Method writeMethod, ObjectDesc<?> objectDesc) {
		this(propertyName, objectDesc);
		setReadMethod(readMethod);
		setWriteMethod(writeMethod);
	}

	protected PropertyDesc(String propertyName, Field f, ObjectDesc<?> objectDesc) {
		this(propertyName, objectDesc);
		this.field = f;
		setPropertyType(f.getType());
		this.genericPropertyType = f.getGenericType();
		SqlEntity sqlEntity = objectDesc.getTargetClass().getAnnotation(SqlEntity.class);
		if (sqlEntity != null) {
			if (!FieldUtil.isFinal(f)) {
				writable = true;
			}
			readable = true;
			f.setAccessible(true);
		}

		setupReadableWritable(f);
	}

	private void setupReadableWritable(Field field) {
		if (objectDesc.isRecord()) {
			this.readable = true;
			this.writable = false;
			field.setAccessible(true);
		}
		Property p = field.getAnnotation(Property.class);
		if (p != null) {
			AccessMode accessMode = p.value();
			this.readable = accessMode.isReadable();
			this.writable = accessMode.isWriteable();
			if (this.writable || this.readable) {
				field.setAccessible(true);
			}
		} else if (Modifier.isPublic(field.getModifiers()) == true && Modifier.isPublic(field.getDeclaringClass().getModifiers()) == true) {
			this.readable = true;
			this.writable = true;
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Field getField() {
		return field;
	}

	public AccessibleObject getReadAccessibleObject() {
		if (isReadable() == false) {
			return null;
		}
		if (readMethod != null) {
			return readMethod;
		} else {
			return field;
		}
	}

	protected void setReadMethod(Method readMethod) {
		this.readMethod = readMethod;
		if (readMethod != null) {
			setPropertyType(readMethod.getReturnType());
			this.genericPropertyType = readMethod.getGenericReturnType();
			readable = true;
		}
	}

	public boolean hasField() {
		return field != null;
	}

	public boolean hasReadMethod() {
		return readMethod != null;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public AccessibleObject getWriteAccessibleObject() {
		if (isWritable() == false) {
			return null;
		}
		if (writeMethod != null) {
			return writeMethod;
		} else {
			return field;
		}
	}

	protected void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
		if (writeMethod != null) {
			setPropertyType(writeMethod.getParameterTypes()[0]);
			this.genericPropertyType = writeMethod.getGenericParameterTypes()[0];
			writable = true;
		}
	}

	public boolean hasWriteMethod() {
		return writeMethod != null;
	}

	public boolean isReadable() {
		return readable;
	}

	public boolean isWritable() {
		return writable;
	}

	@SuppressWarnings("unchecked")
	public <VALUE> VALUE getValue(Object target) {
		return (VALUE) getValue(target, false);
	}

	protected static ThreadLocal<Boolean> cacheMode = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	protected static ThreadLocal<Map<PropertyDesc, Map<Object, Object>>> getValueCache = ThreadLocal.withInitial(() -> new HashMap());

	public static void cacheModeOn() {
		cacheMode.set(true);
	}

	public static void cacheModeOff() {
		cacheMode.set(false);
		ThreadLocalUtil.remove(cacheMode);
		getValueCache.get().clear();
		ThreadLocalUtil.remove(getValueCache);
	}

	@SuppressWarnings("unchecked")
	public <VALUE> VALUE getValue(Object target, boolean force) {
		VALUE ret;
		if (cacheMode.get() == false) {
			ret = getValue(target, force, true);
		} else {
		Map<Object, Object> map = getValueCache.get().get(this);
		Object key = target;
		if (map == null) {
			map = new HashMap<>();
			getValueCache.get().put(this, map);
		}
		if (map.containsKey(key)) {
				ret = (VALUE) map.get(key);
			} else {
				ret = getValue(target, force, true);
				map.put(key, ret);
			}
		}
		if (ret == null && isOptionalType()) {
			return (VALUE) Optional.empty();
		}
		return ret;
	}

	public <VALUE> VALUE getValue(Object target, boolean force, boolean useAOP) {
		if (readable == false && force == false) {
			throw new IllegalStateException(Message.getMessage("00008", propertyName, "readable", target.getClass().getName()));
		}
		try {
			if (readMethod == null) {
				if (force == true && field.isAccessible() == false) {
					field.setAccessible(true);
				}
				if (useAOP == false) {
					@SuppressWarnings("unchecked")
					VALUE ret = (VALUE) field.get(target);
					return ret;
				}
				FieldInterceptor interceptorChain = FieldInterceptors.getInterceptorChain();
				FieldAccess fieldRead = new FieldAccess(target, field, AccessType.READ);

				@SuppressWarnings("unchecked")
				VALUE ret = (VALUE) interceptorChain.get(fieldRead);
				return ret;
			}
			if (force == true && readMethod.isAccessible() == false) {
				readMethod.setAccessible(true);
			}
			try {
				@SuppressWarnings("unchecked")
				VALUE ret = MethodUtil.invoke(readMethod, target, (Object[]) null);
				return ret;
			} catch (Throwable t) {
				if (isIgnoreException(t) == true) {
					return null;
				}
				throw t;
			}

		} catch (Throwable t) {
			throw new IllegalPropertyRuntimeException(objectDesc.getTargetClass(), propertyName, t);
		}
	}

	private boolean isIgnoreException(Throwable t) {
		for (Class<? extends Throwable> clazz : ignoreExceptions) {
			if (t.getClass().isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	public void setValue(Object target, Object value) {
		setValue(target, value, false);
	}

	public void setValue(Object target, Object value, boolean force) {
		setValue(target, value, force, true);
	}

	public void setValue(Object target, Object value, boolean force, boolean useAOP) {
		if (writable == false && force == false) {
			throw new IllegalStateException(Message.getMessage("00008", propertyName, "writable", target.getClass().getName()));
		}
		try {
			Class<?> propertyType = getPropertyType();
			Bean beanAnnotation = getAnnotation(Bean.class);
			if (beanAnnotation != null) {
				// XXX impl some props of Bean.class.
				if (beanAnnotation.defaultBeanClass() != null) {
					propertyType = beanAnnotation.defaultBeanClass();
				}
			}
			if (propertyType.isPrimitive()) {
				propertyType = TypesUtil.toWrapperType(propertyType);
			}
			if (value != null && value instanceof Optional
					&& getRawClass(getTypeParameter(genericPropertyType, 0)).isInstance(((Optional<?>) value).get()) == false) {
				value = ((Optional<?>) value).get();
			}
			if (value != null && propertyType.isInstance(value) == false) {
				if (isOptionalType()) {
					value = Optional.of(convert(value, getRawClass(getTypeParameter(genericPropertyType, 0))));
				} else {
					value = convert(value, propertyType);
				}
			}
			if (writeMethod == null) {
				if (force == true && field.isAccessible() == false) {
					field.setAccessible(true);
				}
				if (useAOP == false) {
					field.set(target, value);
					return;
				}
				FieldInterceptor interceptorChain = FieldInterceptors.getInterceptorChain();
				FieldAccess fieldWrite = new FieldAccess(target, field, AccessType.WRITE, value);

				// field.set(target, value);
				interceptorChain.set(fieldWrite);

				return;
			}
			if (force == true && writeMethod.isAccessible() == false) {
				writeMethod.setAccessible(true);
			}
			try {
				MethodUtil.invoke(writeMethod, target, new Object[] { value });
			} catch (Throwable t) {
				if (isIgnoreException(t) == true) {
					return;
				}
				throw t;
			}
		} catch (Throwable t) {
			throw new IllegalPropertyRuntimeException(objectDesc.getTargetClass(), propertyName, value, t);
		}
	}

	public <V> boolean addValue(Object target, V value) {
		if (isCollectionType() == false) {
			throw new UnsupportedOperationException();
		}
		Collection<V> collection = getValue(target);
		if (collection == null) {
			collection = new ArrayList<>();
			setValue(target, collection);
		}
		return collection.add(value);
	}

	public <K, V> V putValue(Object target, K key, V value) {
		if (isMapType() == false) {
			throw new UnsupportedOperationException();
		}
		Map<K, V> map = getValue(target);
		if (map == null) {
			map = new HashMap<>();
			setValue(target, map);
		}
		return map.put(key, value);
	}

	private Object convert(Object value, Class<?> dest) {
		if (value == null) {
			return null;
		}
		JavaType<?> javaType = TypesUtil.getJavaType(dest);
		return javaType.convert(value);
	}

	public ObjectDesc<?> getObjectDesc() {
		return objectDesc;
	}

	@Override
	public String toString() {
		return toString;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return this.toString().equals(o.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	protected void setPropertyType(Class<?> propertyType) {
		if (this.propertyType != null && this.propertyType.equals(propertyType) == false) {
			logger.info(Message.getMessage("00019", objectDesc.getTargetClass().getName(), getPropertyName()));
		}
		this.propertyType = propertyType;
		isOneDimensionalArray = isOneDimensionalArray(propertyType);
	}

	public Type getGenericPropertyType() {
		return genericPropertyType;
	}

	public boolean isOptionalType() {
		return isTypeOf(getPropertyType(), Optional.class);
	}

	public boolean isCollectionType() {
		return isTypeOf(getPropertyType(), Collection.class);
	}

	public boolean isMapType() {
		return isTypeOf(getPropertyType(), Map.class);
	}

	public boolean isEnumType() {
		return getPropertyType().isEnum();
	}

    @Override
	public <A extends Annotation> A getAnnotation(Class<A> annotation) {
		return getAnnotatedElement().getAnnotation(annotation);
	}

    @Override
	public Annotation[] getAnnotations() {
		return getAnnotatedElement().getAnnotations();
	}

    @Override
	public Annotation[] getDeclaredAnnotations() {
		return getAnnotatedElement().getDeclaredAnnotations();
	}

    @Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getAnnotatedElement().isAnnotationPresent(annotationClass);
	}

	private AnnotatedElement getAnnotatedElement() {
		return new AnnotatedElementImpl(this.readMethod, this.writeMethod, this.field);
	}
	@Override
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return getAnnotatedElement().getDeclaredAnnotationsByType(annotationClass);
	}
	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return getAnnotatedElement().getDeclaredAnnotation(annotationClass);
	}
	@Override
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return getAnnotatedElement().getAnnotationsByType(annotationClass);
	}

	static class AnnotatedElementImpl implements AnnotatedElement {
		private Method readMethod;

		private Method writeMethod;

		private Field field;

		AnnotatedElementImpl(Method readMethod, Method writeMethod, Field field) {
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
			this.field = field;
		}

	    @Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
			if (writeMethod != null) {
				T ret = writeMethod.getAnnotation(annotationType);
				if (ret != null) {
					return ret;
				}
			}
			if (readMethod != null) {
				T ret = readMethod.getAnnotation(annotationType);
				if (ret != null) {
					return ret;
				}
			}
			if (field != null) {
				return field.getAnnotation(annotationType);
			}
			return null;
		}

	    @Override
		public Annotation[] getAnnotations() {
			List<Annotation> ret = new ArrayList<>();
			if (writeMethod != null) {
				ret.addAll(Arrays.asList(writeMethod.getAnnotations()));
			}
			if (readMethod != null) {
				ret.addAll(Arrays.asList(readMethod.getAnnotations()));
			}
			if (field != null) {
				ret.addAll(Arrays.asList(field.getAnnotations()));
			}
			return ret.toArray(new Annotation[ret.size()]);
		}

	    @Override
		public Annotation[] getDeclaredAnnotations() {
			List<Annotation> ret = new ArrayList<>();
			if (writeMethod != null) {
				ret.addAll(Arrays.asList(writeMethod.getDeclaredAnnotations()));
			}
			if (readMethod != null) {
				ret.addAll(Arrays.asList(readMethod.getDeclaredAnnotations()));
			}
			if (field != null) {
				ret.addAll(Arrays.asList(field.getDeclaredAnnotations()));
			}
			return ret.toArray(new Annotation[0]);
		}

	    @Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
			if (writeMethod != null) {
				boolean ret = writeMethod.isAnnotationPresent(annotationType);
				if (ret == true) {
					return ret;
				}
			}
			if (readMethod != null) {
				boolean ret = readMethod.isAnnotationPresent(annotationType);
				if (ret == true) {
					return ret;
				}
			}
			if (field != null) {
				return field.isAnnotationPresent(annotationType);
			}
			return false;
		}
		@Override
		public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
			Annotation[] declaredAnnotations = getDeclaredAnnotations();
			List<T> results = new ArrayList<>();
			for (Annotation a : declaredAnnotations) {
				if (annotationClass.isInstance(a)) {
					results.add(annotationClass.cast(a));
				}
			}

			T[] out = (T[]) Array.newInstance(annotationClass, results.size());
			return results.toArray(out);
		}
		@Override
		public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
	        return getAnnotation(annotationClass);
		}
		@Override
		public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
			T[] result = getDeclaredAnnotationsByType(annotationClass);
			if (result.length > 0) {
				return result;
			}
			return (T[]) Array.newInstance(annotationClass, 0);
		}
	}

	public boolean isOneDimensionalArrayType() {
		return isOneDimensionalArray;
	}

	private boolean isOneDimensionalArray(Class<?> propertyType) {
		boolean isArray = propertyType.isArray();
		if (isArray == false) {
			return false;
		}
		boolean isArrayComponentType = propertyType.getComponentType().isArray();
		return isArrayComponentType == false;
	}

	public void init() {
		initWithReadMethod();
		initWithWriteMethod();
		initWithField();
		this.toString = getDeclaringClass().getName() + "#" + getPropertyName();
	}

	private void initWithField() {
		if (hasField() == false) {
			return;
		}
		Property p = field.getAnnotation(Property.class);
		if (p == null) {
			return;
		}
		AccessMode accessMode = p.value();
		this.readable = accessMode.isReadable();
		this.writable = accessMode.isWriteable();
		this.ignoreExceptions.addAll(Arrays.asList(p.ignoreExceptions()));
	}

	private void initWithWriteMethod() {
		if (hasWriteMethod() == false) {
			return;
		}
		Property p = writeMethod.getAnnotation(Property.class);
		if (p == null) {
			return;
		}
		this.writable = p.value().isWriteable();
		this.ignoreExceptions.addAll(Arrays.asList(p.ignoreExceptions()));
	}

	private void initWithReadMethod() {
		if (hasReadMethod() == false) {
			return;
		}
		Property p = readMethod.getAnnotation(Property.class);
		if (p == null) {
			return;
		}
		this.readable = p.value().isReadable();
		this.ignoreExceptions.addAll(Arrays.asList(p.ignoreExceptions()));
	}

}
