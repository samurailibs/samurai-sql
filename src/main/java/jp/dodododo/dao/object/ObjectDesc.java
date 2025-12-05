package jp.dodododo.dao.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.dao.annotation.Internal;
import jp.dodododo.dao.exception.PropertyNotFoundRuntimeException;
import jp.dodododo.dao.message.Message;
import jp.dodododo.dao.types.JavaType;
import jp.dodododo.dao.util.CaseInsensitiveMap;
import jp.dodododo.dao.util.EmptyUtil;
import jp.dodododo.dao.util.TypesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beanではなくて、プロパティー名をDB寄りとして扱う。<br>
 * コンストラクタの引数によって、大文字、小文字を無視。キャメルケース、スネークケースとかも無視。<br>
 * なので、BeanDescではなくて、ObjectDesc。
 *
 * @author Satoshi Kimura
 */
@Internal
public class ObjectDesc<OBJ> {

	private static final Logger logger = LoggerFactory.getLogger(ObjectDesc.class);

	protected Class<OBJ> targetClass;

	protected ClassDesc<OBJ> classDesc;

	protected Map<String, PropertyDesc> propertyDescCache;
	protected Map<String, FieldDesc> fieldDescCache = new HashMap<>();

	protected List<PropertyDesc> propertyDescList = new ArrayList<>();
	protected List<PropertyDesc> writablePropertyDescList = new ArrayList<>();
	protected List<PropertyDesc> readablePropertyDescList = new ArrayList<>();
	protected List<FieldDesc> fieldDescList = new ArrayList<>();

	protected Map<Class<? extends Annotation>, List<PropertyDesc>> annotationCache = new HashMap<>();
	protected Map<JavaType<?>, List<PropertyDesc>> javaTypeCache = new HashMap<>();

	protected ObjectDesc(boolean isIgnoreCase) {
		if (isIgnoreCase == true) {
			propertyDescCache = new CaseInsensitiveMap<>();
		} else {
			propertyDescCache = new HashMap<>();
		}
	}

	public ObjectDesc(Class<OBJ> targetClass, boolean isIgnoreCase) {
		this(isIgnoreCase);
		if (targetClass == null) {
			throw new NullPointerException("targetClass");
		}
		this.targetClass = targetClass;
		this.classDesc = new ClassDesc<>(targetClass);
		setupPropertyDescs();
		setupAnnotationCache();
		setupJavaTypeCache();
		setupWritablePropertyDescs();
		setupReadablePropertyDescs();
	}

	protected void setupReadablePropertyDescs() {
		propertyDescList.stream().filter(pd -> pd.isReadable()).forEach(pd -> readablePropertyDescList.add(pd));
		this.readablePropertyDescList = Collections.unmodifiableList(readablePropertyDescList);
	}

	protected void setupWritablePropertyDescs() {
		propertyDescList.stream().filter(pd -> pd.isWritable()).forEach(pd -> writablePropertyDescList.add(pd));
		this.writablePropertyDescList = Collections.unmodifiableList(writablePropertyDescList);
	}

	protected void setupJavaTypeCache() {
		int size = getPropertyDescSize();
		for (int i = 0; i < size; i++) {
			PropertyDesc propertyDesc = getPropertyDesc(i);
			JavaType<?> javaType = TypesUtil.getJavaType(propertyDesc.getPropertyType());
			List<PropertyDesc> list = javaTypeCache.get(javaType);
			if (list == null) {
				list = new ArrayList<>(size);
				javaTypeCache.put(javaType, list);
			}
			list.add(propertyDesc);
		}
	}

	private void setupAnnotationCache() {
		int size = getPropertyDescSize();
		for (int i = 0; i < size; i++) {
			PropertyDesc propertyDesc = getPropertyDesc(i);
			Annotation[] annotations = propertyDesc.getAnnotations();
			for (Annotation annotation : annotations) {
				List<PropertyDesc> list = annotationCache.get(annotation.getClass());
				if (list == null) {
					list = new ArrayList<>(size);
					annotationCache.put(annotation.getClass(), list);
				}
				list.add(propertyDesc);
			}
		}
	}

	public ClassDesc<OBJ> getClassDesc() {
		return classDesc;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public boolean hasPropertyDesc(String propertyName) {
		return this.propertyDescCache.containsKey(propertyName);
	}

	public <A extends Annotation> boolean hasPropertyDesc(Class<A> annotation) {
		boolean contains = annotationCache.containsKey(annotation);
		if (contains == false) {
			return false;
		}
		List<PropertyDesc> list = annotationCache.get(annotation);
		if (0 < list.size()) {
			return true;
		} else {
			return false;
		}
	}

	public PropertyDesc getPropertyDesc(String propertyName) {
		PropertyDesc pd = getPropertyDescNoException(propertyName);
		if (pd == null) {
			throw new PropertyNotFoundRuntimeException(targetClass, propertyName);
		}
		return pd;
	}

	public PropertyDesc getPropertyDescNoException(String propertyName) {
		return propertyDescCache.get(propertyName);
	}

	protected void setupPropertyDescs() {
		Map<String, PropertyDesc> propertyDescTmpCache = new CaseInsensitiveMap<>();
		Map<String, FieldDesc> fieldDescTmpCache = new CaseInsensitiveMap<>();
		List<PropertyDesc> propertyDescTmpList = new ArrayList<>();
		List<FieldDesc> fieldDescTmpList = new ArrayList<>();

		for (Field f : getAllDeclaredFields()) {
			setupPropertyDescs(f, propertyDescTmpCache, propertyDescTmpList, fieldDescTmpCache, fieldDescTmpList);
		}
		for (Method m : targetClass.getMethods()) {
			setupPropertyDescs(m, propertyDescTmpCache, propertyDescTmpList);
		}
		propertyDescTmpList.forEach(pd ->{
			propertyDescCache.put(pd.getPropertyName(), pd);
			propertyDescList.add(pd);
		});
		fieldDescTmpList.forEach(fd -> {
			fieldDescCache.put(fd.getFieldName(), fd);
			fieldDescList.add(fd);
		});
		for (PropertyDesc propertyDesc : propertyDescList) {
			Annotation[] annotations = propertyDesc.getAnnotations();
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationClass = annotation.annotationType();
				List<PropertyDesc> list = annotationCache.get(annotationClass);
				if (list == null) {
					list = new ArrayList<>();
					annotationCache.put(annotationClass, list);
				}
				list.add(propertyDesc);
			}
			propertyDesc.init();
		}
		this.propertyDescList = Collections.unmodifiableList(propertyDescList);
		this.fieldDescList = Collections.unmodifiableList(fieldDescList);
	}

	private Field[] getAllDeclaredFields() {
		return getAllDeclaredFields(targetClass, new HashMap<>());
	}

	private Field[] getAllDeclaredFields(Class<?> clazz, Map<String, Field> fields) {
		if (clazz == null) {
			return fields.values().toArray(new Field[fields.size()]);
		}
		Field[] fs = clazz.getDeclaredFields();
		for (Field field : fs) {
			String name = field.getName();
			if (fields.containsKey(name) == false) {
				fields.put(name, field);
			}
		}
		return getAllDeclaredFields(clazz.getSuperclass(), fields);
	}

	private void setupPropertyDescs(Field f, //
			Map<String, PropertyDesc> propertyDescTmpCache, List<PropertyDesc> propertyDescTmpList, //
			Map<String, FieldDesc> fieldDescTmpCache, List<FieldDesc> fieldDescTmpList //
	) {
		if (f.isSynthetic()) {
			return;
		}
		if (Modifier.isStatic(f.getModifiers()) == true) {
			return;
		}
		String propertyName = f.getName();
		PropertyDesc propDesc = new PropertyDesc(propertyName, f, this);
		FieldDesc fieldDesc = new FieldDesc(propertyName, f, this);
		addPropertyDesc(propertyName, propDesc, propertyDescTmpCache, propertyDescTmpList);
		addFieldDesc(propertyName, fieldDesc, fieldDescTmpCache, fieldDescTmpList);
	}

	private void setupPropertyDescs(Method m, Map<String, PropertyDesc> propertyDescTmpCache,
			List<PropertyDesc> propertyDescTmpList) {
		if (m.isBridge() || m.isSynthetic() || Modifier.isStatic(m.getModifiers()) == true || m.isVarArgs()
				|| Modifier.isPublic(m.getDeclaringClass().getModifiers()) == false) {
			return;
		}
		String methodName = m.getName();
		Class<?> returnType = m.getReturnType();
		if (methodName.startsWith("get")) {
			if (EmptyUtil.isEmpty(m.getParameterTypes()) == true && returnType.equals(void.class) == false
					&& methodName.equals("getClass") == false) {
				String propertyName = methodName.substring(3);
				addReadMethod(m, propertyName, returnType, propertyDescTmpCache, propertyDescTmpList);
			}
		} else if (methodName.startsWith("is")) {
			if (EmptyUtil.isEmpty(m.getParameterTypes()) == true && (returnType.equals(Boolean.TYPE) || returnType.equals(Boolean.class))) {
				String propertyName = methodName.substring(2);
				addReadMethod(m, propertyName, returnType, propertyDescTmpCache, propertyDescTmpList);
			}
		} else if (methodName.startsWith("set")) {
			if (m.getParameterTypes().length == 1 && returnType.equals(void.class)) {
				String propertyName = methodName.substring(3);
				addWriteMethod(m, propertyName, m.getParameterTypes()[0], propertyDescTmpCache, propertyDescTmpList);
			}
		}
	}

	private void addPropertyDesc(String propertyName, PropertyDesc propertyDesc, Map<String, PropertyDesc> propertyDescTmpCache,
			List<PropertyDesc> propertyDescTmpList) {
		if (propertyName == null) {
			throw new NullPointerException("propertyName");
		}
		if (propertyDesc == null) {
			throw new NullPointerException("propertyDesc");
		}
		propertyDescTmpCache.put(propertyName, propertyDesc);
		propertyDescTmpList.add(propertyDesc);
	}

	private void addFieldDesc(String fieldName, FieldDesc fieldDesc, Map<String, FieldDesc> fieldDescTmpCache,
			List<FieldDesc> fieldDescTmpList) {
		if (fieldName == null) {
			throw new NullPointerException("propertyName");
		}
		if (fieldDesc == null) {
			throw new NullPointerException("fieldDesc");
		}

		fieldDescTmpCache.put(fieldName, fieldDesc);
		fieldDescTmpList.add(fieldDesc);
	}

	private <PROPERTY> void addReadMethod(Method readMethod, String propertyName, Class<PROPERTY> propertyType,
			Map<String, PropertyDesc> propertyDescTmpCache, List<PropertyDesc> propertyDescTmpList) {
		PropertyDesc propDesc = propertyDescTmpCache.get(propertyName);
		if (propDesc != null) {
			if (propDesc.getPropertyType().equals(propertyType) == false) {
				if (propDesc.getWriteMethod() != null) {
					logger.warn(Message.getMessage("00007", propDesc.getWriteMethod().getName(), propDesc.getPropertyType()
							.getName(), readMethod.getName(), propertyType.getName()));
				} else {
					propDesc.setReadMethod(readMethod);
					propDesc.setPropertyType(propertyType);
				}
			} else {
				propDesc.setReadMethod(readMethod);
			}
		} else {
			propDesc = new PropertyDesc(propertyName, readMethod, null, this);
			addPropertyDesc(propertyName, propDesc, propertyDescTmpCache, propertyDescTmpList);
		}
	}

	private <PROPERTY> void addWriteMethod(Method writeMethod, String propertyName, Class<PROPERTY> propertyType,
			Map<String, PropertyDesc> propertyDescTmpCache, List<PropertyDesc> propertyDescTmpList) {
		PropertyDesc propDesc = propertyDescTmpCache.get(propertyName);
		if (propDesc != null) {
			if (propDesc.getPropertyType().equals(propertyType) == false) {
				if (propDesc.getReadMethod() != null) {
					logger.warn(Message.getMessage("00007", propDesc.getReadMethod().getName(), propDesc.getPropertyType()
							.getName(), writeMethod.getName(), propertyType.getName()));
				} else {
					propDesc.setWriteMethod(writeMethod);
					propDesc.setPropertyType(propertyType);
				}
			} else {
				propDesc.setWriteMethod(writeMethod);
			}
		} else {
			propDesc = new PropertyDesc(propertyName, null, writeMethod, this);
			addPropertyDesc(propertyName, propDesc, propertyDescTmpCache, propertyDescTmpList);
		}
	}

	public int getPropertyDescSize() {
		return propertyDescList.size();
	}

	public PropertyDesc getPropertyDesc(int index) {
		return propertyDescList.get(index);
	}

	public List<PropertyDesc> getReadablePropertyDescs() {
		return readablePropertyDescList;
	}

	public List<PropertyDesc> getWritablePropertyDescs() {
		return writablePropertyDescList;
	}

	public List<PropertyDesc> getPropertyDescs() {
		return propertyDescList;
	}

	public <T extends Annotation> PropertyDesc getPropertyDesc(Class<T> annotation) {
		List<PropertyDesc> propertyDescs = getPropertyDescs(annotation);
		int size = propertyDescs.size();
		if (size < 1) {
			return null;
		} else if (size == 1) {
			return propertyDescs.get(0);
		} else {
			throw new IllegalStateException(Message.getMessage("00023", annotation.getName()));
		}
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> List<PropertyDesc> getPropertyDescs(Class<A> annotation) {
		List<PropertyDesc> ret = annotationCache.get(annotation);
		if (ret != null) {
			return ret;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@SuppressWarnings("unchecked")
	public <JAVA_TYPE> List<PropertyDesc> getPropertyDescs(JavaType<JAVA_TYPE> javaType) {
		List<PropertyDesc> ret = javaTypeCache.get(javaType);
		if (ret != null) {
			return ret;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public Map<String, Object> toMapWithFields(OBJ obj) {
		Map<String, Object> ret = new HashMap<>();
		List<FieldDesc> fieldDescs = getFieldDescs();
		for (FieldDesc fd : fieldDescs) {
			String fieldName = fd.getFieldName();
			Object value;
			try {
				value = fd.getValue(obj, true);
			} catch (Throwable ignore) {
				value = null;
			}
			ret.put(fieldName, value);
		}
		return ret;
	}

	private List<FieldDesc> getFieldDescs() {
		return fieldDescList;
	}

	public Map<String, Object> toMap(Object obj) {
		Map<String, Object> ret = new HashMap<>();

		List<PropertyDesc> propertyDescs = getPropertyDescs();
		propertyDescs.stream().filter(pd -> pd.isReadable()).forEach(pd -> ret.put(pd.getPropertyName(), pd.getValue(obj)));
		return ret;
	}

	public boolean isRecord() {
		return this.targetClass.isRecord();
	}
}
