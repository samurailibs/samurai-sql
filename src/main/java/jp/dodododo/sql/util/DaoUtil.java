package jp.dodododo.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.dodododo.sql.DaoConstants;
import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Columns;
import jp.dodododo.sql.annotation.Table;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.object.MapPropertyDesc;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.paging.LimitOffset;
import jp.dodododo.sql.sql.orderby.OrderByArg;
import jp.dodododo.sql.sql.orderby.SortType;

/**
 *
 * @author Satoshi Kimura
 */
public abstract class DaoUtil {
	public static final String TABLE_NAME = DaoConstants.TABLE_NAME;

	public static final String ORDER_BY = DaoConstants.ORDER_BY;

	public static final String VALUES = "vals";

	public static Map<String, Object> query(Object... map) {
		return map(map);
	}

	public static Map<String, Object> args(Object... map) {
		return map(map);
	}

	public static Map<String, Object> values(Object... map) {
		if (map != null && map.length == 1) {
			return map(VALUES, map[0]);
		}
		return map(map);
	}

	public static Map<String, Object> from(Object o) {
		return table(o);
	}

	public static Map<String, Object> into(Object o) {
		return table(o);
	}

	public static Map<String, Object> table(Object o) {
		return map(TABLE_NAME, getTableName(o));
	}

	public static Map<String, Object> by(Object... map) {
		return map(map);
	}

	public static Map<String, Object> where(Object... map) {
		return map(map);
	}

	public static Map<String, Object> map(Object... map) {
		Map<String, Object> args = CollectionOfString.map(map);
		if (args.containsKey(LimitOffset.KEYWORD) == true) {
			throw new IllegalArgumentException(Message.getMessage("00017", LimitOffset.KEYWORD));
		}
		Object val = args.remove(null);
		if (val == null) {
			return args;
		}
		if (val instanceof LimitOffset) {
			args.put(LimitOffset.KEYWORD, val);
		} else {
			args.put(null, val);
		}
		return args;
	}

	protected static final Map<Object, String> TABLE_NAME_CACHE = CacheUtil.cacheMap();
	protected static final Map<PropertyDesc, Map<String, Set<String>>> TABLE_NAMES_CACHE = CacheUtil.cacheMap();

	@SuppressWarnings("unchecked")
	public static String getTableName(Object o) {
		if (o instanceof CharSequence) {
			return o.toString();
		}
		if (o instanceof Map) {
			return getTableName((Map<String, Object>) o);
		}
		String ret = TABLE_NAME_CACHE.get(o);
		if (ret != null) {
			return ret;
		}
		if (o instanceof MapPropertyDesc) {
			return null;
		} else if (o instanceof PropertyDesc) {
			String tableName = getTableName(PropertyDesc.class.cast(o));
			TABLE_NAME_CACHE.put(o, tableName);
			return tableName;
		} else if (o instanceof Class) {
			String tableName = getTableName(Class.class.cast(o));
			TABLE_NAME_CACHE.put(o, tableName);
			return tableName;
		}
		Class<?> clazz = o.getClass();
		String tableName = getTableName(clazz);
		TABLE_NAME_CACHE.put(o, tableName);
		return tableName;
	}

	public static Set<String> getTableNames(PropertyDesc pd, String columnName) {
		Map<String, Set<String>> subCache = TABLE_NAMES_CACHE.get(pd);
		if(subCache == null) {
			subCache = new CaseInsensitiveMap<>();
			TABLE_NAMES_CACHE.put(pd, subCache);
		}
		Set<String> ret = subCache.get(columnName);
		if(ret != null) {
			return ret;
		}
		CaseInsensitiveSet colName = new CaseInsensitiveSet();
		colName.add(columnName);
		ret = new CaseInsensitiveSet();
		Columns columns = pd.getAnnotation(Columns.class);
		if (columns != null) {
			for (Column column : columns.value()) {
				if (colName.contains(column.value()) == true) {
					String table = column.table();
					if (EmptyUtil.isNotEmpty(table) == true) {
						ret.add(table);
					}
				}
			}
		}
		if (ret.isEmpty() == false) {
			subCache.put(columnName, ret);
			return ret;
		}
		Column column = pd.getAnnotation(Column.class);
		if (column != null && colName.contains(column.value()) == true) {
			String table = column.table();
			if (EmptyUtil.isNotEmpty(table) == true) {
				ret.add(table);
			}
		}
		if (ret.isEmpty() == false) {
			subCache.put(columnName, ret);
			return ret;
		}

		Class<?> clazz = pd.getObjectDesc().getTargetClass();
		do {
			ret.add(getTableName(clazz));
			clazz = clazz.getSuperclass();
		} while (clazz != null);

		subCache.put(columnName, ret);
		return ret;
	}

	public static String getTableName(PropertyDesc propertyDesc) {
		Map<Object, String> tableNameCache = TABLE_NAME_CACHE;
		String ret = tableNameCache.get(propertyDesc);
		if (ret != null) {
			return ret;
		}
		Column column = propertyDesc.getAnnotation(Column.class);
		if (column != null) {
			String table = column.table();
			if (EmptyUtil.isNotEmpty(table) == true) {
				tableNameCache.put(propertyDesc, table);
				return table;
			}
		}
		String table = getTableName(propertyDesc.getDeclaringClass());
		tableNameCache.put(propertyDesc, table);
		return table;
	}

	public static String getTableName(Class<?> entityClass) {
		String ret = TABLE_NAME_CACHE.get(entityClass);
		if (ret != null) {
			return ret;
		}
		Table table = entityClass.getAnnotation(Table.class);
		if (table != null) {
			ret = table.value();
			TABLE_NAME_CACHE.put(entityClass, ret);
			return ret;
		} else {
			ret = getTableNameFromClassName(ClassUtil.getShortName(entityClass));
			TABLE_NAME_CACHE.put(entityClass, ret);
			return ret;
		}
	}

	private static String getTableName(Map<String, Object> map) {
		if (map instanceof CaseInsensitiveMap == false) {
			map = new CaseInsensitiveMap<>(map);
		}
		Object val = map.get(TABLE_NAME);
		if (val != null) {
			return val.toString();
		} else {
			throw new IllegalArgumentException("'" + TABLE_NAME + "' was not found. [" + map + "]");
		}
	}

	private static String getTableNameFromClassName(String className) {
		if (0 < className.indexOf('$')) {
			return StringUtil.decamelize(className.substring(className.indexOf('$') + 1, className.length()));
		}
		return StringUtil.decamelize(className);
	}

	protected static final Map<PropertyDesc, String> COLUMN_NAME_CACHE = CacheUtil.cacheMap();

	public static String getColumnName(PropertyDesc propertyDesc) {
		if (propertyDesc instanceof MapPropertyDesc) {
			return propertyDesc.getPropertyName();
		}
		String ret = COLUMN_NAME_CACHE.get(propertyDesc);
		if (ret != null) {
			return ret;
		}
		Column column = propertyDesc.getAnnotation(Column.class);
		if (column != null) {
			ret = column.value();
		} else {
			ret = propertyDesc.getPropertyName();
		}
		COLUMN_NAME_CACHE.put(propertyDesc, ret);
		return ret;
	}

	public static List<Object> list(Object... args) {
		List<Object> ret = new ArrayList<>(args.length);
		for (Object o : args) {
			ret.add(o);
		}
		return ret;
	}

	public static Map<String, Object> orderBy(Object... args) {
		return map(ORDER_BY, orderByList(args));
	}

	public static List<OrderByArg> orderByList(Object... args) {
		List<OrderByArg> orderByList = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			Object arg1 = args[i];
			Object arg2 = null;
			if (i + 1 < args.length) {
				arg2 = args[i + 1];
			}

			String columnName = null;
			if (arg1 instanceof CharSequence) {
				columnName = arg1.toString();
			}
			if (columnName == null) {
				continue;
			}
			SortType sortType = null;
			if (arg2 instanceof SortType) {
				sortType = (SortType) arg2;
			}
			orderByList.add(new OrderByArg(columnName, sortType));
		}
		return orderByList;
	}
}
