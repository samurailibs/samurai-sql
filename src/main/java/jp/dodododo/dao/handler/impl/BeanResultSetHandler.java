package jp.dodododo.dao.handler.impl;

import static jp.dodododo.dao.util.EmptyUtil.*;
import static jp.dodododo.janerics.GenericsTypeUtil.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.dodododo.dao.ConnectionHolder;
import jp.dodododo.dao.IterationCallback;
import jp.dodododo.dao.annotation.Arg;
import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Compress;
import jp.dodododo.dao.annotation.Zone;
import jp.dodododo.dao.columns.ResultSetColumn;
import jp.dodododo.dao.commons.Null;
import jp.dodododo.dao.exception.InstantiationRuntimeException;
import jp.dodododo.dao.exception.NoParameterizedException;
import jp.dodododo.dao.impl.EmptyIterationCallback;
import jp.dodododo.dao.lazyloading.LazyLoadingUtil;
import jp.dodododo.dao.lazyloading.ProxyFactory;
import jp.dodododo.dao.message.Message;
import jp.dodododo.dao.object.ObjectDesc;
import jp.dodododo.dao.object.ObjectDescFactory;
import jp.dodododo.dao.object.PropertyDesc;
import jp.dodododo.dao.row.Row;
import jp.dodododo.dao.types.JavaType;
import jp.dodododo.dao.types.JavaTypes;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.util.AnnotationUtil;
import jp.dodododo.dao.util.CacheUtil;
import jp.dodododo.dao.util.CaseInsensitiveSet;
import jp.dodododo.dao.util.ConstructorUtil;
import jp.dodododo.dao.util.DaoUtil;
import jp.dodododo.dao.util.EmptyUtil;
import jp.dodododo.dao.util.OgnlUtil;
import jp.dodododo.dao.util.StringUtil;
import jp.dodododo.dao.util.TypesUtil;
import jp.dodododo.dao.util.ZoneUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Satoshi Kimura
 */
public class BeanResultSetHandler<T> extends AbstractResultSetHandler<T> {

	private static final Log logger = LogFactory.getLog(BeanResultSetHandler.class);

	private static final Map<Class<?>, List<PropertyDesc>> FOR_REL_PROPERTY_DESC_CACHE = CacheUtil.cacheMap();

	private static final Map<Class<?>, Constructor<?>> USABLE_CONSTRUCTORS = CacheUtil.cacheMap();

	private Class<T> beanClass;

	private String createMethod;

	private PropertyDesc pd;

	private boolean nullable;

	private Map<String, Object> arg;

	private Connection connection;

	public BeanResultSetHandler(IterationCallback<T> callback, Map<String, Object> arg, Class<T> beanClass, Connection  connection) {
		this(callback, beanClass, getCreateMethod(beanClass), false, arg, connection);
	}

	protected BeanResultSetHandler(IterationCallback<T> callback, Class<T> beanClass, String createMethod, boolean nullable, Map<String, Object> arg, Connection  connection) {
		super(callback);
		if (isEmpty(createMethod)) {
			this.createMethod = getCreateMethod(beanClass);
		} else {
			this.createMethod = createMethod;
		}
		if (isEmpty(createMethod) == true) {
			validateBeanClass(beanClass);
		}
		this.beanClass = beanClass;
		this.nullable = nullable;
		this.arg = arg;
		this.connection= connection;
	}

	protected void validateBeanClass(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
			throw new InstantiationRuntimeException(clazz);
		}
	}

	private static <T> String getCreateMethod(Class<T> beanClass) {
		Bean bean = beanClass.getAnnotation(Bean.class);
		if (bean == null) {
			return null;
		}
		return bean.createMethod();
	}

	protected BeanResultSetHandler(IterationCallback<T> callback, Class<T> beanClass, Map<Class<?>, Map<StringBuilderWrapper, Object>> rowCaches,
			Map<String, Object> arg, Connection  connection) {
		this(callback, arg, beanClass, connection);
		this.rowCaches = rowCaches;
	}

	@Override
	protected T createRow(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {
		T row = newInstance(rs, resultSetColumnList);

		if (row == null) {
			return row;
		}

		setValues(rs, resultSetColumnList, row);

		ObjectDesc<T> objectDesc = ObjectDescFactory.getObjectDesc(beanClass);

		StringBuilderWrapper key = getKey(row);
		Map<StringBuilderWrapper, Object> rowCache = getRowCache(row.getClass());
		boolean isReturnNull = rowCache.containsKey(key);

		// For Relation
		List<PropertyDesc> propertyDescs = FOR_REL_PROPERTY_DESC_CACHE.get(beanClass);
		boolean useCache = true;
		if (propertyDescs == null) {
			useCache = false;
			propertyDescs = new ArrayList<>(objectDesc.getReadablePropertyDescs());
		}
		for (int i = 0; i < propertyDescs.size(); i++) {
			PropertyDesc propertyDesc = propertyDescs.get(i);
			if (useCache == false && isForRelProperty(propertyDesc) == false) {
				propertyDescs.remove(i);
				i--;
				continue;
			}
			setRelationValues(rs, resultSetColumnList, propertyDesc, row);
		}
		if (useCache == false) {
			FOR_REL_PROPERTY_DESC_CACHE.put(beanClass, propertyDescs);
		}

		if (isReturnNull == true) {
			return null;
		}
		return row;
	}

	private boolean isForRelProperty(PropertyDesc propertyDesc) {
		if (propertyDesc.isWritable() == false) {
			return false;
		}
		if (propertyDesc.isEnumType() == true) {
			return false;
		}
		Class<?> propertyType = propertyDesc.getPropertyType();
		Class<?> javaType = TypesUtil.getJavaType(propertyType).getType();
		if (Object.class.equals(javaType) == true) {
			return true;
		}
		if (Collection.class.equals(javaType) == true) {
			return true;
		}
		if (javaType.isArray() == true) {
			return true;
		}
		return false;
	}

	private void setRelationValues(ResultSet rs, List<ResultSetColumn> resultSetColumnList, PropertyDesc propertyDesc, T target) throws SQLException {

		if (LazyLoadingUtil.isProxy(target) == true) {
			return;
		}

		Class<?> propertyType = propertyDesc.getPropertyType();
		boolean isOneDimensionalArray = propertyDesc.isOneDimensionalArrayType();
		if (propertyDesc.isCollectionType() == true || isOneDimensionalArray == true) { // OneToMany,ManyToMany
			Type elementType = getElementType(propertyDesc);
			if (elementType == null) {
				throw new NoParameterizedException(target.getClass(), propertyDesc.getPropertyName());
			}
			Class<?> nestedBeanType = getRawClass(elementType);
			if (nestedBeanType == null) {
				throw new NoParameterizedException(target.getClass(), propertyDesc.getPropertyName());
			}
			Object nestedValue = getNestedValue(rs, resultSetColumnList, propertyDesc, isOneDimensionalArray, nestedBeanType);
			StringBuilderWrapper key = getKey(target);
			Map<StringBuilderWrapper, Object> rowCache = getRowCache(target.getClass());
			@SuppressWarnings("unchecked")
			T tmp = (T) rowCache.get(key);
			if (tmp != null) {
				target = tmp;
			} else {
				rowCache.clear();
				rowCache.put(key, target);
			}
			Object value = propertyDesc.getValue(target);
			if (value == null) {
				value = newEmptyValue(propertyDesc);
				propertyDesc.setValue(target, value);
			}
			if (nestedValue != null) {
				if (propertyDesc.isCollectionType() == true) {
					@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
					boolean ignore = ((Collection) value).add(nestedValue);
				} else if (isOneDimensionalArray == true) {
					int length = Array.getLength(value);
					Object newArray = Array.newInstance(propertyType.getComponentType(), length + 1);
					System.arraycopy(value, 0, newArray, 0, length);
					Array.set(newArray, length, nestedValue);
					propertyDesc.setValue(target, newArray);
				}
			}
		} else { // Other
			Class<?> beanClass = getBeanClass(propertyDesc);
			Class<?> relBeanType = beanClass != null ? beanClass : propertyType;
			try {
				logger.trace(Message.getMessage("00046", propertyDesc.getDeclaringClass().getName(), propertyDesc.getPropertyName()));
				Object nestedBean = getNestedValue(rs, resultSetColumnList, propertyDesc, isOneDimensionalArray, relBeanType);
				propertyDesc.setValue(target, nestedBean);
				logger.trace(Message.getMessage("00047", propertyDesc.getDeclaringClass().getName(), propertyDesc.getPropertyName()));
			} catch (SQLException e) {
				logger.warn(Message.getMessage("00048", propertyDesc.getDeclaringClass().getName(), propertyDesc.getPropertyName()), e);
			} catch (RuntimeException e) {
				logger.warn(Message.getMessage("00048", propertyDesc.getDeclaringClass().getName(), propertyDesc.getPropertyName()), e);
			}
		}
	}

	private Class<?> getBeanClass(PropertyDesc pd) {
		Bean bean = pd.getAnnotation(Bean.class);
		if (bean == null) {
			return null;
		}
		if (Null.class.equals(bean.defaultBeanClass()) == false) {
			return bean.defaultBeanClass();
		}
		if (Null.class.equals(bean.value()) == false) {
			return bean.value();
		}
		return null;
	}

	private Object getNestedValue(ResultSet rs, List<ResultSetColumn> resultSetColumnList, PropertyDesc propertyDesc, boolean isOneLengthArray,
			Class<?> nestedBeanType) throws SQLException {
		IterationCallback<?> emptyCallback = EmptyIterationCallback.getInstance();
		Object nestedValue = null;
		if (propertyDesc.isCollectionType() == true // Collection<Bean>
				|| (isOneLengthArray == true && Object.class.equals(TypesUtil.getJavaType(nestedBeanType).getType())) // Bean[]
				|| isOneLengthArray == false // Bean
		) {
			Bean bean = propertyDesc.getAnnotation(Bean.class);
			String createMethod = bean == null ? null : bean.createMethod();
			boolean nullable = bean == null ? false : bean.nullable();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			BeanResultSetHandler<?> handler = new BeanResultSetHandler(emptyCallback, nestedBeanType, createMethod, nullable, this.arg, this.connection);
			handler.pd = propertyDesc;
			handler.rowCaches = this.rowCaches;
			Object nestedBean = handler.createRow(rs, resultSetColumnList);
			nestedValue = nestedBean;
		} else if (isOneLengthArray == true) { // String[], Number[], ...
			JavaType<?> javaType = TypesUtil.getJavaType(propertyDesc.getPropertyType().getComponentType());
			nestedValue = javaType.getValue(rs, propertyDesc.getPropertyName());
		}
		return nestedValue;
	}

	private Object newEmptyValue(PropertyDesc propertyDesc) {
		if (propertyDesc.isCollectionType() == true) {
			return new ArrayList<>();
		}
		Class<?> propertyType = propertyDesc.getPropertyType();
		if (propertyDesc.isOneDimensionalArrayType() == true) {
			return Array.newInstance(propertyType.getComponentType(), 0);
		}
		throw new IllegalArgumentException(propertyDesc.getPropertyName());
	}

	private Type getElementType(PropertyDesc propertyDesc) {
		if (propertyDesc.isCollectionType() == true) {
			return getElementTypeOfCollection(propertyDesc.getGenericPropertyType());
		}
		Class<?> propertyType = propertyDesc.getPropertyType();
		if (propertyType.isArray() == true) {
			return propertyType.getComponentType();
		}
		throw new IllegalArgumentException(propertyDesc.getPropertyName());
	}

	private Map<StringBuilderWrapper, Object> getRowCache(Class<?> clazz) {
		if (rowCaches.containsKey(clazz) == true) {
			return rowCaches.get(clazz);
		} else {
			Map<StringBuilderWrapper, Object> ret = new HashMap<>();
			rowCaches.put(clazz, ret);
			return ret;
		}
	}

	private void setValues(ResultSet rs, List<ResultSetColumn> resultSetColumnList, T row) throws SQLException {
		if (LazyLoadingUtil.isProxy(row) == true) {
			return;
		}

		ObjectDesc<T> objectDesc = ObjectDescFactory.getObjectDesc(beanClass);

		List<PropertyDesc> propertyDescs = objectDesc.getWritablePropertyDescs();
		for (PropertyDesc pd : propertyDescs) {
			setValue(row, pd, resultSetColumnList, rs);
		}
	}

	private void setValue(T row, PropertyDesc pd, List<ResultSetColumn> resultSetColumnList, ResultSet rs) throws SQLException {
		Class<?> propertyType = pd.getPropertyType();
		JavaType<?> javaType = TypesUtil.getJavaType(propertyType);
		if (JavaTypes.ARRAY.equals(javaType) == true || JavaTypes.COLLECTION.equals(javaType) == true || JavaTypes.MAP.equals(javaType) == true

		) {
			return;
		}
		Column column = pd.getAnnotation(Column.class);
		Zone zone = pd.getAnnotation(Zone.class);
		if (column == null) {
			Object value = getValue(pd, resultSetColumnList, rs, row, zone);
			setValue(row, pd, value);
			return;
		}
		String[] alias = column.alias();
		if (isNotEmpty(alias)) {
			for (String colName : alias) {
				Object value = getValue(pd, colName, resultSetColumnList, rs, row, zone);
				if (setValue(row, pd, value)) {
					return;
				}
			}
		}
		Object value = getValue(pd, column.value(), resultSetColumnList, rs, row, zone);
		setValue(row, pd, value);
	}

	private boolean setValue(T row, PropertyDesc pd, Object value) {
		if (value == null) {
			return false;
		}
		if (pd.isWritable() == false) {
			return false;
		}
		if (pd.isCollectionType() || pd.isMapType() || pd.getPropertyType().isArray()) {
			return false;
		}
		Compress compress = pd.getAnnotation(Compress.class);
		if (compress != null && compress.autoUncompress() == true) {
			value = compress.compressType().uncompress(value);
		}
		pd.setValue(row, value);
		addRowDataCaches(row, pd, value);
		return true;
	}

	private void addRowDataCaches(T target, PropertyDesc propertyDesc, Object value) {
		Class<?> propertyType = propertyDesc.getPropertyType();
		addRowDataCaches(target, propertyType, propertyDesc.getPropertyName(), value);
	}

	private void addRowDataCaches(Object target, Class<?> propertyType, String propertyName, Object value) {
		Integer key = System.identityHashCode(target);
		JavaType<?> javaType = TypesUtil.getJavaType(propertyType);
		if (JavaTypes.INPUT_STREAM == javaType) {
			return;
		}
		if (JavaTypes.ARRAY == javaType) {
			return;
		}
		if (JavaTypes.COLLECTION == javaType) {
			return;
		}
		if (JavaTypes.MAP == javaType) {
			return;
		}
		if (JavaTypes.BYTE_ARRAY == javaType) {
			return;
		}
		if (JavaTypes.OBJECT == javaType) {
			return;
		}
		if (LazyLoadingUtil.isProxy(value) == true) {
			return;
		}
		StringBuilder data = rowDataCaches.get(key);
		if (data == null) {
			data = new StringBuilder(128);
			rowDataCaches.put(key, data);
		}
		data.append(", ");
		data.append("[");
		data.append(propertyName);
		data.append("=");
		data.append(value);
		data.append("]");
	}

	private Object getValue(PropertyDesc pd, List<ResultSetColumn> resultSetColumnList, ResultSet rs, T row, Zone zone) throws SQLException {
		String colName = pd.getPropertyName();
		return getValue(pd, colName, resultSetColumnList, rs, row, zone);
	}

	private Object getValue(PropertyDesc pd, String colName, List<ResultSetColumn> resultSetColumnList, ResultSet rs, T row, Zone zone) throws SQLException {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		if(zone!= null) {
			if (EmptyUtil.isNotEmpty(zone.id())) {
				zoneId = ZoneUtil.zoneId(zone.id());
			}
			if (EmptyUtil.isNotEmpty(zone.idPropertyName())) {
				Object value = ObjectDescFactory.getObjectDesc(row).getPropertyDesc(zone.idPropertyName()).getValue(row);
				zoneId = ZoneUtil.zoneId(value);
			}
			if (EmptyUtil.isNotEmpty(zone.idColumnName())) {
				String value = rs.getString(zone.idColumnName());
				zoneId = ZoneUtil.zoneId(value);
			}
			if (EmptyUtil.isNotEmpty(zone.offset())) {
				zoneOffset = ZoneUtil.zoneOffset(zone.offset());
			}
			if (EmptyUtil.isNotEmpty(zone.offsetPropertyName())) {
				Object value = ObjectDescFactory.getObjectDesc(row).getPropertyDesc(zone.offsetPropertyName()).getValue(row);
				zoneOffset = ZoneUtil.zoneOffset(value);
			}
			if (EmptyUtil.isNotEmpty(zone.offsetColumnName())) {
				String value = rs.getString(zone.offsetColumnName());
				zoneOffset = ZoneUtil.zoneOffset(value);
			}
		}

		for (ResultSetColumn resultSetColumn : resultSetColumnList) {
			if (StringUtil.equalsIgnoreCase(colName, resultSetColumn.getName())) {
				JavaType<?> javaType = TypesUtil.getJavaType(pd.getPropertyType());
				try {
					if(zoneOffset != null) {
						return javaType.getValue(rs, resultSetColumn.getName(), zoneOffset);
					} else if (zoneId != null) {
						return javaType.getValue(rs, resultSetColumn.getName(), zoneId);
					} else {
						return javaType.getValue(rs, resultSetColumn.getName());
					}
				} catch (UnsupportedOperationException ignore) {
				}
			}
		}
		return null;
	}

	private Map<Class<?>, Map<StringBuilderWrapper, Object>> rowCaches = new HashMap<>();
	private Map<Integer, StringBuilder> rowDataCaches = new HashMap<>();

	protected T newInstance(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {

		if (isEmpty(createMethod)) {
			createMethod = getCreateMethod(beanClass);
		}
		if (isNotEmpty(createMethod)) {
			Map<String, Object> root = new HashMap<>();
			Map<String, Object> resultSetMap = new MapResultSetHandler(null).createRow(rs, resultSetColumnList);
			Row row = new  Row(resultSetMap);
			root.put("resultSetMap", resultSetMap);
			root.put("rsMap", resultSetMap);
			root.put("row", row);
			root.put("resultSet", rs);
			root.put("rs", rs);
			root.put("columnList", resultSetColumnList);
			root.put("columns", resultSetColumnList);
			Object val = OgnlUtil.getValue(createMethod, root);
			if (val instanceof Constructor) {
				@SuppressWarnings("unchecked")
				Constructor<T> constructor = (Constructor<T>) val;
				return newInstance(beanClass, constructor, rs, resultSetColumnList, nullable, this.arg, this.connection);
			}
			if (val instanceof Class) {
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) val;
				return newInstance(clazz, rs, resultSetColumnList, nullable, arg, this.connection);
			}
			@SuppressWarnings("unchecked")
			T ret = (T) val;
			return ret;
		}

		if (JavaTypes.OBJECT.equals(TypesUtil.getJavaType(beanClass)) == false && pd != null) {
			String columnName = DaoUtil.getColumnName(pd);
			if (contains(resultSetColumnList, columnName) == false) {
				return null;
			}
			@SuppressWarnings("unchecked")
			T ret = (T) TypesUtil.getJavaType(beanClass).getValue(rs, columnName);
			return ret;
		}

		return newInstance(beanClass, rs, resultSetColumnList, nullable, arg, this.connection);
	}

	protected <BEAN> BEAN newInstance(Class<BEAN> beanClass, ResultSet rs, List<ResultSetColumn> resultSetColumnList, boolean nullable,
			Map<String, Object> arg, Connection connection) throws SQLException {
		ObjectDesc<BEAN> objectDesc = ObjectDescFactory.getObjectDesc(beanClass);
		List<Constructor<BEAN>> constructors = objectDesc.getClassDesc().getConstructors(Modifier.PUBLIC);
		Constructor<BEAN> usableConstructor = getUsableConstructor(beanClass, constructors, resultSetColumnList, beanClass);
		return newInstance(beanClass, usableConstructor, rs, resultSetColumnList, nullable, arg,connection);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <BEAN> BEAN newInstance(Class<BEAN> beanClass, Constructor<BEAN> constructor, ResultSet rs,
			List<ResultSetColumn> resultSetColumnList, boolean nullable, Map<String, Object> args, Connection connection) throws SQLException {
		List<Column> columnAnnotations = AnnotationUtil.getParameterAnnotations(constructor, Column.class);
		List<Compress> compressAnnotations = AnnotationUtil.getParameterAnnotations(constructor, Compress.class);
		List<Arg> argAnnotations = AnnotationUtil.getParameterAnnotations(constructor, Arg.class);
		Map<Integer, String> paramNames = new HashMap<>();
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
		Object[] initArgs = new Object[parameterTypes.length];
		Object[] dbInitArgs = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			JavaType<?> javaType = TypesUtil.getJavaType(parameterType);
			Class<?> wrapperType = javaType.getWrapperType();
			Class<?> parameterWrapperType = javaType.getWrapperType()==null? parameterType:wrapperType;
			JavaType<?> wrapperJavaType = TypesUtil.getJavaType(parameterWrapperType);
			Column column = columnAnnotations.get(i);
			Compress compress = compressAnnotations.get(i);
			Arg arg = argAnnotations.get(i);
			String argName = null;
			boolean processed = false;
			if (arg != null && column != null) {
				Annotation[] annotations = constructor.getParameterAnnotations()[i];
				for (Annotation annotation : annotations) {
					if (annotation instanceof Column) {
						processed = processColumnAnnotation(i, column, compress, rs, resultSetColumnList, javaType, wrapperJavaType, initArgs,
								dbInitArgs, paramNames);
					} else if (annotation instanceof Arg) {
						String name = ((Arg)annotation).value();
						processed = processArgAnnotation(i, name, args, parameterType, parameterWrapperType, initArgs, dbInitArgs, paramNames);
					}
					if(processed) {
						break;
					}
				}
			}
			if (arg != null) {
				argName = arg.value();
			}
			if (processed == true) {
			} else if (column != null) {
				processColumnAnnotation(i, column, compress, rs, resultSetColumnList, javaType, wrapperJavaType, initArgs, dbInitArgs, paramNames);
			} else if (dbInitArgs[i] == null && arg != null && args.containsKey(argName)) {
				processArgAnnotation(i, argName, args, parameterType, parameterWrapperType, initArgs, dbInitArgs, paramNames);
			} else {
				IterationCallback<?> callback = EmptyIterationCallback.getInstance();

				Class<?> beanType = getBeanType(parameterType, parameterAnnotations[i]);
				String createMethod = getCreateMethod(parameterType, parameterAnnotations[i]);
				boolean nullableParameter = getNullable(parameterType, parameterAnnotations[i]);
				BeanResultSetHandler<?> beanResultSetHandler = new BeanResultSetHandler(callback, beanType, createMethod, nullableParameter, args, connection);
				try {
					logger.trace(Message.getMessage("00049",  constructor, i, parameterType));
					Object bean = beanResultSetHandler.createRow(rs, resultSetColumnList);
					logger.trace(Message.getMessage("00050",  constructor, i, parameterType));
					initArgs[i] = bean;
					dbInitArgs[i] = bean;
				} catch (SQLException e) {
					logger.warn(Message.getMessage("00051",  constructor, i, parameterType.getSimpleName()), e);
					initArgs[i] = null;
					dbInitArgs[i] = null;
				} catch (RuntimeException e) {
					logger.warn(Message.getMessage("00051",  constructor, i, parameterType.getSimpleName()), e);
					initArgs[i] = null;
					dbInitArgs[i] = null;
				}
				paramNames.put(i, null);
			}
		}

		if (nullable == true && isNotEmpty(dbInitArgs) && isAllNull(dbInitArgs) == true) {
			return null;
		}

		BEAN bean;
		if (LazyLoadingUtil.isProxy(beanClass) == true) {
			ProxyFactory proxyFactory = ProxyFactory.newInstance(beanClass);
			bean = proxyFactory.create(beanClass, constructor, initArgs);
		} else {
			bean = ConstructorUtil.newInstance(constructor, initArgs);
		}
		for (int i = 0; i < initArgs.length; i++) {
			addRowDataCaches(bean, constructor.getParameterTypes()[i], paramNames.get(i), initArgs[i]);
		}
		if (bean instanceof ConnectionHolder) {
			ConnectionHolder holder = (ConnectionHolder) bean;
			holder.setConnection(connection);
		}
		return bean;
	}

	/**
	 *
	 * @return processed
	 */
	private boolean processArgAnnotation(int index, String argName, Map<String, Object> args, Class<?> parameterType, Class<?> parameterWrapperType,
			Object[] initArgs, Object[] dbInitArgs, Map<Integer, String> paramNames) {
		boolean processed = true;
		Object object = args.get(argName);
		if (object == null) {
			initArgs[index] = TypeConverter.convert(object, parameterType);
			dbInitArgs[index] = TypeConverter.convert(object, parameterWrapperType);
			processed = false;
		} else if (parameterType.isAssignableFrom(object.getClass())) {
			initArgs[index] = object;
			dbInitArgs[index] = object;
		} else {
			Object tmpValue = TypeConverter.convert(object, parameterType);
			if (tmpValue.getClass().isAssignableFrom(parameterType)) {
				initArgs[index] = tmpValue;
				dbInitArgs[index] = TypeConverter.convert(object, parameterWrapperType);
			}
		}
		paramNames.put(index, argName);
		return processed;
	}

	/**
	 *
	 * @return processed
	 */
	protected boolean processColumnAnnotation(int index, Column column, Compress compress, ResultSet rs, List<ResultSetColumn> resultSetColumnList,
			JavaType<?> javaType, JavaType<?> wrapperJavaType, Object[] initArgs, Object[] dbInitArgs, Map<Integer, String> paramNames)
			throws SQLException {
		String columnName = null;
		List<String> columnNames = getColumnNames(column);
		for (String name : columnNames) {
			String resultSetColumnName = getResultSetColumnName(resultSetColumnList, name);
			if (resultSetColumnName == null) {
				initArgs[index] = javaType.convert(null);
				paramNames.put(index, columnName);
				dbInitArgs[index] = null;
				continue;
			}
			columnName = resultSetColumnName;
			break;
		}
		if (columnName == null) {
			// continue;
			return false;
		}
		initArgs[index] = javaType.getValue(rs, columnName);
		dbInitArgs[index] = wrapperJavaType.getValue(rs, columnName);
		if (compress != null && compress.autoUncompress() == true) {
			initArgs[index] = compress.compressType().uncompress(initArgs[index]);
		}
		paramNames.put(index, columnName);
		return true;
	}

	protected static boolean contains(List<ResultSetColumn> resultSetColumnList, String columnName) {
		return resultSetColumnList.stream().anyMatch(resultSetColumn -> StringUtil.equalsIgnoreCase(resultSetColumn.getName(), columnName));
	}

	private static String getResultSetColumnName(List<ResultSetColumn> resultSetColumnList, String name) {
		Set<String> tmpSet = new CaseInsensitiveSet();
		tmpSet.add(name);
		for (ResultSetColumn resultSetColumn : resultSetColumnList) {
			String columnName = resultSetColumn.getName();
			if (tmpSet.contains(columnName)) {
				return columnName;
			}
		}
		return null;
	}

	private static boolean isAllNull(Object[] objects) {

		for (Object o : objects) {
			if (o != null) {
				return false;
			}
		}
		return true;
	}

	private static boolean getNullable(Class<?> parameterType, Annotation[] annotations) {

		if (annotations == null) {
			return false;
		}

		for (Annotation annotation : annotations) {
			if (annotation instanceof Bean == false) {
				continue;
			}
			Bean beanAnnotation = (Bean) annotation;
			return beanAnnotation.nullable();
		}

		return false;
	}

	private static String getCreateMethod(Class<?> parameterType, Annotation[] annotations) {

		if (annotations == null) {
			return getCreateMethod(parameterType);
		}

		for (Annotation annotation : annotations) {
			if (annotation instanceof Bean == false) {
				continue;
			}
			Bean beanAnnotation = (Bean) annotation;
			String createMethod = beanAnnotation.createMethod();
			Class<?> beanType = beanAnnotation.value();
			Class<?> defaultBeanClass = beanAnnotation.defaultBeanClass();
			validateCreateMethod(createMethod, beanType, defaultBeanClass);
			String createMethod2 = getCreateMethod(parameterType);
			if (isNotEmpty(createMethod2) && isEmpty(createMethod)) {
				return createMethod2;
			}
			if(isEmpty(createMethod)) {
				return null;
			}
			return createMethod;
		}

		return getCreateMethod(parameterType);
	}

	private static void validateCreateMethod(String createMethod, Class<?> beanType, Class<?> defaultBeanClass) {
		if (isEmpty(createMethod) == false && isNullValue(beanType) == false || isNullValue(defaultBeanClass) == false) {
			throw new IllegalStateException("Can not write both createMethod and defalutbeanClass.");
		}
	}

	private static Class<?> getBeanType(Class<?> basicType, Annotation[] annotations) {

		if (annotations == null) {
			return basicType;
		}

		for (Annotation annotation : annotations) {
			if (annotation instanceof Bean == false) {
				continue;
			}
			Bean beanAnnotation = (Bean) annotation;
			Class<?> defaultBeanClass = beanAnnotation.defaultBeanClass();
			if (isNullValue(defaultBeanClass) == false) {
				return defaultBeanClass;
			}
			Class<?> value = beanAnnotation.value();
			if (isNullValue(value) == false) {
				return value;
			}
		}

		return basicType;
	}

	private static boolean isNullValue(Class<?> clazz) {
		return Null.class.equals(clazz);
	}

	private static List<String> getColumnNames(Column column) {

		String[] alias = column.alias();
		List<String> ret = new ArrayList<>(alias.length);
		for (String s : alias) {
			ret.add(s);
		}
		ret.add(column.value());
		return ret;
	}

	protected static <T> Constructor<T> getUsableConstructor(Class<T> beanClass, List<Constructor<T>> constructors, List<ResultSetColumn> resultSetColumnList,
			Class<T> targetClass) {

		Constructor<?> c = USABLE_CONSTRUCTORS.get(targetClass);
		if (c != null) {
			@SuppressWarnings("unchecked")
			Constructor<T> ret = (Constructor<T>) c;
			return ret;
		}

		Collections.sort(constructors, new ConstructorComparator());

		for (Constructor<T> constructor : constructors) {
			boolean usable = true;
			List<Column> columnAnnotations = AnnotationUtil.getParameterAnnotations(constructor, Column.class);
			List<Arg> argAnnotations = AnnotationUtil.getParameterAnnotations(constructor, Arg.class);
			for (int i = 0; i < columnAnnotations.size(); i++) {
				Column column = columnAnnotations.get(i);
				Arg arg = argAnnotations.get(i);
				if (column != null || arg != null || isBeanClass(constructor.getParameterTypes()[i]) == true) {
				} else {
					usable = false;
					logger.trace("[" + constructor + "], argIndex[" + i + "] has not @Column or type is not bean.");
					continue;
				}
			}
			if (usable == true) {
				logger.trace("used constructor [" + constructor + "]");
				USABLE_CONSTRUCTORS.put(targetClass, constructor);
				return constructor;
			}
		}

		throw new RuntimeException("ConstructorNotFoundException at " + targetClass);
	}

	private static boolean isBeanClass(Class<?> clazz) {
		if (clazz.isArray() == true) {
			return false;
		}
		JavaType<?> javaType = TypesUtil.getJavaType(clazz);
		return JavaTypes.OBJECT == javaType;
	}

	private StringBuilderWrapper getKey(Object row) {
		if (row == null) {
			return new StringBuilderWrapper(new StringBuilder());
		}
		int hashCode = System.identityHashCode(row);
		StringBuilder builder = this.rowDataCaches.get(hashCode);
		if (builder == null) {
			return new StringBuilderWrapper(new StringBuilder());
		}
		return new StringBuilderWrapper(builder);
	}

	private static final class StringBuilderWrapper {
		private final StringBuilder builder;

		public StringBuilderWrapper(StringBuilder builder) {
			this.builder = builder;
		}

		@Override
		public boolean equals(Object obj) {
			try {
				StringBuilderWrapper wrapper = (StringBuilderWrapper) obj;
				int length = builder.length();
				if (length != wrapper.builder.length()) {
					return false;
				}
				for (int i = 0; i < length; i++) {
					if (builder.charAt(i) != wrapper.builder.charAt(i)) {
						return false;
					}
				}

				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int ret = 0;
			int length = builder.length();
			if (0 < length) {
				ret = length + builder.charAt(0);
			}
			if (32 < length) {
				ret = length + builder.charAt(32);
			}
			if (128 < length) {
				ret = length + builder.charAt(128);
			}
			return ret;
		}
	}

	public static class ConstructorComparator implements Comparator<Constructor<?>> {

		@Override
		public int compare(Constructor<?> o1, Constructor<?> o2) {
			Class<?>[] parameterTypes1 = o1.getParameterTypes();
			Class<?>[] parameterTypes2 = o2.getParameterTypes();
			return parameterTypes2.length - parameterTypes1.length;
		}
	}
}
