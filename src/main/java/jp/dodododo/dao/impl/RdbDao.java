package jp.dodododo.dao.impl;

import static jp.dodododo.dao.lock.OptimisticLocking.*;
import static jp.dodododo.dao.sql.GenericSql.*;
import static jp.dodododo.dao.types.SQLTypes.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static jp.dodododo.dao.util.EmptyUtil.*;

import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.sql.DataSource;

import jp.dodododo.dao.CRUD;
import jp.dodododo.dao.Dao;
import jp.dodododo.dao.ExtendedExecuteUpdateDao;
import jp.dodododo.dao.IterationCallback;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Columns;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Internal;
import jp.dodododo.dao.annotation.Rel;
import jp.dodododo.dao.annotation.Relations;
import jp.dodododo.dao.annotation.Timestamp;
import jp.dodododo.dao.annotation.VersionNo;
import jp.dodododo.dao.columns.NoPersistentColumns;
import jp.dodododo.dao.columns.PersistentColumns;
import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.context.CommandContext;
import jp.dodododo.dao.dialect.Default;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.DialectManager;
import jp.dodododo.dao.dialect.Standard;
import jp.dodododo.dao.empty_impl.DataSourceImpl;
import jp.dodododo.dao.exception.DaoRuntimeException;
import jp.dodododo.dao.exception.InvalidSQLException;
import jp.dodododo.dao.exception.PropertyNotFoundRuntimeException;
import jp.dodododo.dao.exception.SQLRuntimeException;
import jp.dodododo.dao.function.ConsumerWrapper;
import jp.dodododo.dao.handler.ResultSetHandler;
import jp.dodododo.dao.handler.factory.ResultSetHandlerFactory;
import jp.dodododo.dao.id.GeneratedValue;
import jp.dodododo.dao.id.IdGenerator;
import jp.dodododo.dao.id.Identity;
import jp.dodododo.dao.lazyloading.LazyLoadingUtil;
import jp.dodododo.dao.lock.Locking;
import jp.dodododo.dao.log.ExecuteType;
import jp.dodododo.dao.log.SqlLog;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.message.Message;
import jp.dodododo.dao.metadata.ColumnMetaData;
import jp.dodododo.dao.metadata.TableMetaData;
import jp.dodododo.dao.object.ObjectDesc;
import jp.dodododo.dao.object.ObjectDescFactory;
import jp.dodododo.dao.object.PropertyDesc;
import jp.dodododo.dao.paging.LimitOffset;
import jp.dodododo.dao.paging.PagingResultSet;
import jp.dodododo.dao.script.Each;
import jp.dodododo.dao.sql.Sql;
import jp.dodododo.dao.sql.SqlContext;
import jp.dodododo.dao.sql.node.Node;
import jp.dodododo.dao.sql.parse.SqlParser;
import jp.dodododo.dao.types.JavaType;
import jp.dodododo.dao.types.JavaTypes;
import jp.dodododo.dao.types.SQLType;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.util.CacheUtil;
import jp.dodododo.dao.util.CaseInsensitiveMap;
import jp.dodododo.dao.util.CaseInsensitiveSet;
import jp.dodododo.dao.util.ClassUtil;
import jp.dodododo.dao.util.CloseableUtil;
import jp.dodododo.dao.util.ConnectionUtil;
import jp.dodododo.dao.util.DBUtil;
import jp.dodododo.dao.util.DaoUtil;
import jp.dodododo.dao.util.DataSourceUtil;
import jp.dodododo.dao.util.EnumUtil;
import jp.dodododo.dao.util.InputStreamReaderUtil;
import jp.dodododo.dao.util.JndiUtil;
import jp.dodododo.dao.util.OgnlUtil;
import jp.dodododo.dao.util.PreparedStatementUtil;
import jp.dodododo.dao.util.ReaderUtil;
import jp.dodododo.dao.util.ResultSetUtil;
import jp.dodododo.dao.util.StatementUtil;
import jp.dodododo.dao.util.StringUtil;
import jp.dodododo.dao.util.TmpFileUtil;
import jp.dodododo.dao.util.ToStringer;
import jp.dodododo.dao.util.TypesUtil;
import jp.dodododo.dao.value.CandidateValue;
import jp.dodododo.dao.value.OGNLValueProxy;
import jp.dodododo.dao.value.ParameterValue;
import jp.dodododo.dao.value.ValueProxy;
import jp.dodododo.dao.wrapper.ConnectionWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RdbDao implements Dao, ExtendedExecuteUpdateDao {

	protected static final Log logger = LogFactory.getLog(RdbDao.class);

	protected static final String DEFAULT_WHERE_COLUMN_PREFIX = "where___";

	protected static final Map<String, String> SQL_CACHE = CacheUtil.cacheMap();

	protected static final boolean PREPARE = true;

	protected static final Map<String, Node> NODE_CACHE = CacheUtil.cacheMap();

	protected static final Map<String, List<String>> UPDATE_COLUMN_NAMES_CACHE = CacheUtil.cacheMap();

	protected DataSource dataSource;

	protected Dialect dialect;

	protected SqlLogRegistry sqlLogRegistry;

	protected String whereColumnPrefix;

	protected DaoConfig config = new DaoConfig();

	protected int queryTimeout;

	{
		sqlLogRegistry = SqlLogRegistry.getInstance();
		sqlLogRegistry.setConfig(config);
		whereColumnPrefix = DEFAULT_WHERE_COLUMN_PREFIX;
		queryTimeout = config.getQueryTimeout();
	}

	/**
	 * @see #setDataSource(DataSource)
	 */
	public RdbDao() {
	}

	public RdbDao(Object arg) {
		if (arg instanceof String) {
			setDataSource(lookupDataSource((String) arg));
		} else if (arg instanceof Connection) {
			setConnection((Connection) arg);
		} else if (arg instanceof DataSource) {
			setDataSource((DataSource) arg);
		} else {
			throw new RuntimeException("arg is " + arg.getClass().getName());
		}
	}

	public RdbDao(String jndiName) {
		this(lookupDataSource(jndiName));
	}

	public RdbDao(Connection connection) {
		setConnection(connection);
	}

	public void setConnection(Connection connection) {
		init(connection);
		initDialect();
	}

	protected synchronized void init(final Connection connection) {
		final Connection connectionWrapper = new ConnectionWrapper(connection) {
			@Override
			public void close() {
				// empty
			}
		};
		this.dataSource = new DataSourceImpl() {
			@Override
			public Connection getConnection() {
				return connectionWrapper;
			}
		};
	}

	public RdbDao(DataSource dataSource) {
		setDataSource(dataSource);
	}

	protected void initDialect() {
		Connection connection = null;
		try {
			connection = getConnection();
			dialect = DialectManager.getDialect(connection);
		} finally {
			ConnectionUtil.close(connection);
		}
	}

    @Override
	public int insert(String tableName, Object... entity) {
		ObjectDesc<Object> desc =ObjectDescFactory.getObjectDesc(entity[0]);
		TableMetaData tableMetaData = getTableMetaData(DaoUtil.getTableName(desc.getTargetClass()));
		List<PropertyDesc> propertyDescs = desc.getPropertyDescs();
		List<String> npc= new ArrayList<>();
		Dialect dialect = DialectManager.getDialect(dataSource);
		for (PropertyDesc pd :propertyDescs ) {
			Id id = pd.getAnnotation(Id.class);
			if (id == null) {
				continue;
			}

			for (IdDefSet idDefSet : id.value()) {
				Class<? extends IdGenerator> strategy = idDefSet.strategy();
				Class<? extends Dialect> db = idDefSet.db();
				if (db == null) {
				} else if (db.isInstance(dialect) == false) {
					continue;
				}
				if (GeneratedValue.class.equals(strategy)) {
					ColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(pd.getPropertyName());
					npc.add(columnMetaData.getColumnName());
				}
			}

		}
		return insert(tableName, entity[0], null, npc, OPTIMISTIC_LOCKING, entity);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity) {
		return insert(getTableName(entity, getConnection()), entity);
	}

	protected TableMetaData getTableMetaData(String tableName) {
		Connection connection = null;
		try {
			connection = getConnection();
			return TableMetaData.getTableMetaData(connection, tableName);
		} finally {
			ConnectionUtil.close(connection);
		}
	}

	protected Connection getConnection() {
		return DataSourceUtil.getConnection(dataSource);
	}

	protected static final Map<String, String> ALL_TABLE_NAMES_CACHE = CacheUtil.cacheCaseInsensitiveMap();
	protected static final Map<String, String> TABLE_NAME_CACHE = CacheUtil.cacheMap();

	protected String getTableName(Object o) {
		Connection connection = null;
		try {
			connection = getConnection();
			return getTableName(o, connection);
		} finally {
			ConnectionUtil.close(connection);
		}
	}

	protected String getTableName(Object o, Connection connection) {
		String tableName = DaoUtil.getTableName(o);
		String ret = TABLE_NAME_CACHE.get(tableName);
		if (ret != null) {
			return ret;
		}

		if (ALL_TABLE_NAMES_CACHE.isEmpty()) {
			Map<String, String> tableNames = DBUtil.getTableNames(connection);
			ALL_TABLE_NAMES_CACHE.putAll(tableNames);
		}
		ret = ALL_TABLE_NAMES_CACHE.get(tableName);
		if (ret != null) {
			TABLE_NAME_CACHE.put(tableName, ret);
			return ret;
		}
		TABLE_NAME_CACHE.put(tableName, tableName);
		return tableName;
	}

    @Override
	public <ENTITY> int delete(String tableName, ENTITY entity) {
		return delete(tableName, entity, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int delete(ENTITY entity) {
		return delete(getTableName(entity, getConnection()), entity);
	}

    @Override
	public int update(String tableName, Object... entity) {
		return update(tableName, entity[0], null, null, OPTIMISTIC_LOCKING, entity);
	}

    @Override
	public <ENTITY> int update(ENTITY entity) {
		return update(getTableName(entity, getConnection()), entity);
	}

    @Override
	public <ENTITY> int[] delete(Collection<ENTITY> entities) {
		return delete(entities, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities) {
		return insert(entities, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities) {
		return update(entities, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ROW> Optional<ROW> selectOne(Class<ROW> returnType, Object... args) {
		List<ROW> list = select(returnType, args);
		return getOne(list, args);
	}

    @Override
	public <ROW> List<ROW> select(Class<ROW> returnType, Object... args) {
		return select(SIMPLE_WHERE, query(args), returnType);
	}

	protected <ROW> List<ROW> select(String sql, Map<String, Object> arg, IterationCallback<ROW> callback, ResultSetHandler<?> handler) {
		return select(sql, arg, callback, handler, true);
	}

	@Internal
	public <ROW> List<ROW> select(String sql, Map<String, Object> arg, IterationCallback<ROW> callback, ResultSetHandler<?> handler, boolean isDynamic) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			if (sql.contains(" |\n|\r|\t") == false) {
				sql = getSql(sql, dialect);
			}
			LimitOffset limitOffset = null;
			if (arg != null && arg.containsKey(LimitOffset.KEYWORD)) {
				limitOffset = (LimitOffset) arg.get(LimitOffset.KEYWORD);
				try {
					sql = dialect.limitOffsetSql(sql, limitOffset);
					limitOffset = null;
				} catch (UnsupportedOperationException ignore) {
				}
			}
			Map<String, ParameterValue> values = createParameterValues(arg);
			if (isDynamic) {
				ps = createPreparedStatement(connection, sql, values, ExecuteType.QUERY, dialect, null);
			} else {
				logSql(sql, null, null, dialect, ExecuteType.QUERY);
				ps = createPreparedStatement(connection, sql, dialect);
			}
			rs = PreparedStatementUtil.executeQuery(ps, sqlLogRegistry);
			if (limitOffset != null) {
				rs = new PagingResultSet(rs, limitOffset, sql);
			}
			rs = dialect.resultSet(rs);
			handler.handle(rs);
			return callback.getResult();
		} catch (SQLException e) {
			throw new SQLRuntimeException(sqlLogRegistry.getLast().getCompleteSql(), e);
		} finally {
			try {
				ResultSetUtil.close(rs);
			} finally {
				try {
					StatementUtil.close(ps);
				} finally {
					ConnectionUtil.close(connection);
				}
			}
		}
	}

	protected String getSql(String sql, Dialect dialect) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return getSql(sql, dialect, cl);
	}

	protected String getSql(String sql, Dialect dialect, ClassLoader loader) {
		String key = sql + dialect.getSuffix() + "[" + loader.getClass() + "@" + loader.hashCode() + "]";
		if (SQL_CACHE.containsKey(key)) {
			return SQL_CACHE.get(key);
		}
		String ret = readSqlFile(sql, dialect, loader);
		SQL_CACHE.put(key, ret);
		return ret;
	}

	protected String readSqlFile(final String sql, Dialect dialect, ClassLoader loader) {
		InputStream is = null;
		Reader reader = null;
		try {
			is = loader.getResourceAsStream(getSqlFilePath(sql, dialect, loader));
			if (is != null) {
				reader = InputStreamReaderUtil.create(is, config.getEncoding());
				return ReaderUtil.readText(reader);
			}
			return sql;
		} catch (RuntimeException e) {
			// functionの可能性があるので、そのまま返す
			return sql;
		} finally {
			CloseableUtil.close(reader, true);
			CloseableUtil.close(is, true);
		}
	}

	protected String getSqlFilePath(String sqlPath, Dialect dialect, ClassLoader loader) {
		String base = getBase(sqlPath);
		String dialectPath = base + dialect.getSuffix() + ".sql";
		String standardPath = base + ".sql";
		if (loader.getResource(dialectPath) != null) {
			return dialectPath;
		}
		String tryDialectPath = "/" + dialectPath;
		if (loader.getResource(tryDialectPath) != null) {
			return tryDialectPath;
		}
		if (loader.getResource(standardPath) != null) {
			return standardPath;
		}
		String tryStandardPath = "/" + standardPath;
		if (loader.getResource(tryStandardPath) != null) {
			return tryStandardPath;
		}
		return sqlPath;
	}

	protected String getBase(String sqlPath) {
		if (sqlPath.endsWith(".sql")) {
			return sqlPath.substring(0, sqlPath.length() - ".sql".length());
		}
		return sqlPath;
	}

	protected <ROW> ResultSetHandler<?> createResultSetHandler(Class<ROW> entityClass, IterationCallback<ROW> callback, Dialect dialect,
			Map<String, Object> arg) {

		ResultSetHandlerFactory<ROW> resultSetHandlerFactory = ResultSetHandlerFactory.getResultSetHandlerFactory(entityClass);
		JavaType<?> javaType = TypesUtil.getJavaType(entityClass);
		return resultSetHandlerFactory.create(entityClass, javaType, callback, dialect, arg, getConnection());
	}

    @Override
	public <ROW> Optional<ROW> selectOne(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
		List<ROW> rows = select(sql, arg, entityClass);
		return getOne(rows, arg);
	}

    @Override
	public <ROW> Optional<ROW> selectOne(String sql, Class<ROW> entityClass) {
		return selectOne(sql, null, entityClass);
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		initDialect();
	}

    @Override
	public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc) {
		return insert(tableName, entity, null, npc.getColumnList(), OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc) {
		return insert(getTableName(entity, getConnection()), entity, npc);
	}

    @Override
	public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc) {
		return insert(tableName, entity, pc.getColumnList(), null, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity, PersistentColumns pc) {
		return insert(getTableName(entity, getConnection()), entity, pc);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc) {
		return insert(entities, npc, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int insert(String tableName, ENTITY entity, Locking locking) {
		return insert(tableName, entity, null, null, locking);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity, Locking locking) {
		return insert(getTableName(entity, getConnection()), entity, locking);
	}

    @Override
	public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
		return insert(tableName, entity, null, npc.getColumnList(), locking);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc, Locking locking) {
		return insert(getTableName(entity, getConnection()), entity, npc, locking);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc) {
		return insert(entities, pc, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
		return insert(tableName, entity, pc.getColumnList(), null, locking);
	}

    @Override
	public <ENTITY> int insert(ENTITY entity, PersistentColumns pc, Locking locking) {
		return insert(getTableName(entity, getConnection()), entity, pc, locking);
	}

    @Override
	public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc) {
		return update(tableName, entity, npc, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc) {
		return update(getTableName(entity, getConnection()), entity, npc);
	}

    @Override
	public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc) {
		return update(tableName, entity, pc, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int update(ENTITY entity, PersistentColumns pc) {
		return update(getTableName(entity, getConnection()), entity, pc);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc) {
		return update(entities, npc, OPTIMISTIC_LOCKING);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc) {
		return update(entities, pc, OPTIMISTIC_LOCKING);
	}

	protected List<String> getUpdateColumnNames(TableMetaData tableMetaData, List<String> persistentColumns, List<String> noPersistentColumns) {
		if (persistentColumns != null && persistentColumns.isEmpty() == false) {
			return persistentColumns;
		}
		String cacheKey = tableMetaData.getTableName() + "$$$" + persistentColumns + "$$$" + noPersistentColumns;
		List<String> columnNames = UPDATE_COLUMN_NAMES_CACHE.get(cacheKey);
		if (columnNames != null) {
			return columnNames;
		}

		List<String> allColumnNames = tableMetaData.getColumnNames();
		columnNames = new ArrayList<>(allColumnNames.size());
		Set<String> npc = toCaseInsensitiveSet(noPersistentColumns);
		for (String columnName : allColumnNames) {
			if (npc.contains(columnName) == false) {
				columnNames.add(columnName);
			}
		}
		UPDATE_COLUMN_NAMES_CACHE.put(cacheKey, columnNames);
		return columnNames;
	}

	protected Set<String> toCaseInsensitiveSet(List<String> list) {
		if (list == null) {
			return CaseInsensitiveSet.EMPTY;
		}
		return new CaseInsensitiveSet(list);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities, Locking locking) {
		return insertBatch(entities, null, null, locking);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
		return insertBatch(entities, null, npc.getColumnList(), locking);
	}

    @Override
	public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
		return insertBatch(entities, pc.getColumnList(), null, locking);
	}

    @Override
	public <ENTITY> int update(String tableName, ENTITY entity, Locking locking) {
		return update(tableName, entity, null, null, locking);
	}

    @Override
	public <ENTITY> int update(ENTITY entity, Locking locking) {
		return update(getTableName(entity, getConnection()), entity, locking);
	}

    @Override
	public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
		return update(tableName, entity, null, npc.getColumnList(), locking);
	}

    @Override
	public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc, Locking locking) {
		return update(getTableName(entity, getConnection()), entity, npc, locking);
	}

    @Override
	public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
		return update(tableName, entity, pc.getColumnList(), null, locking);
	}

    @Override
	public <ENTITY> int update(ENTITY entity, PersistentColumns pc, Locking locking) {
		return update(getTableName(entity, getConnection()), entity, pc, locking);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities, Locking locking) {
		return updateBatch(entities, null, null, locking);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
		return updateBatch(entities, null, npc.getColumnList(), locking);
	}

    @Override
	public <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
		return updateBatch(entities, pc.getColumnList(), null, locking);
	}

	protected <ENTITY> int[] insertBatch(Collection<ENTITY> entities, List<String> pc, List<String> npc, Locking locking) {
		if (entities.isEmpty() == true) {
			return new int[] {};
		}
		Iterator<ENTITY> iterator = entities.iterator();
		if (iterator.hasNext() == false) {
			return new int[] {};
		}
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			ENTITY entity = iterator.next();
			String tableName = getTableName(entity, getConnection());
			prepareInsert(tableName, entity, locking, connection);
			TableMetaData tableMetaData = getTableMetaData(tableName);
			List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);

			Map<String, ParameterValue> values = getUpdateParameterValues(new Object[] { entity }, updateColumnNames, tableMetaData);
			String sql = createInsertSql(tableName, updateColumnNames, values);
			ps = createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
			PreparedStatementUtil.addBatch(ps, sqlLogRegistry);

			for (; iterator.hasNext();) {
				ENTITY e = iterator.next();
				prepareInsert(tableName, e, locking, connection);

				Node node = parse(sql);
				values = getUpdateParameterValues(new Object[] { e }, updateColumnNames, tableMetaData);
				CommandContext ctx = new CommandContext(dialect);
				ctx.addArgs(values);
				node.accept(ctx);
				List<Object> bindVariables = ctx.getBindVariables();
				List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
				bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
				PreparedStatementUtil.addBatch(ps, sqlLogRegistry);
			}
			int[] counts = PreparedStatementUtil.executeBatch(ps, sqlLogRegistry);
			if (entities.size() == 1) {
				setIds(tableName, entity, connection, ps, !PREPARE);
			}
			return counts;
		} finally {
			PropertyDesc.cacheModeOff();
			try {
				StatementUtil.close(ps);
			} finally {
				ConnectionUtil.close(connection);
			}
		}
	}

	protected <ENTITY> void prepareInsert(String tableName, ENTITY entity, Locking locking, Connection connection) {
		setIds(tableName, entity, connection, null, PREPARE);
		if (locking == OPTIMISTIC_LOCKING) {
			setVersionNos(tableName, entity, true);
			setTimestamps(tableName, entity);
		}
	}

	protected <ENTITY> void setTimestamps(String tableName, ENTITY entity) {
		ObjectDesc<ENTITY> objectDesc = ObjectDescFactory.getObjectDesc(entity);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(Timestamp.class);
		for (PropertyDesc propertyDesc : propertyDescs) {
			Timestamp timestamp = propertyDesc.getAnnotation(Timestamp.class);
			boolean isMatchTable = isMatchTable(tableName, propertyDesc, timestamp);
			if (isMatchTable == false) {
				continue;
			}
			String expression = timestamp.value();
			Object value = OgnlUtil.getValue(expression, new HashMap<>());
			propertyDesc.setValue(entity, value);
		}
	}

	protected <ENTITY> void setVersionNos(String tableName, ENTITY entity, boolean isInsert) {

		ObjectDesc<ENTITY> objectDesc = ObjectDescFactory.getObjectDesc(entity);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(VersionNo.class);
		for (PropertyDesc propertyDesc : propertyDescs) {
			VersionNo versionNo = propertyDesc.getAnnotation(VersionNo.class);
			boolean isMatchTable = isMatchTable(tableName, propertyDesc, versionNo);
			if (isMatchTable == false) {
				continue;
			}
			if (isInsert == true) {
				propertyDesc.setValue(entity, versionNo.value());
			} else {
				long oldVersion = Long.parseLong(propertyDesc.getValue(entity).toString());
				long newVersion = oldVersion + 1;
				propertyDesc.setValue(entity, newVersion);
			}
		}
	}

	protected <ENTITY> boolean setIds(String tableName, ENTITY entity, Connection connection, PreparedStatement ps, boolean isPrepare) {
		if (entity == null) {
			return false;
		}
		Map<Integer, Object> processedObjects = new HashMap<>();
		return setIds(tableName, entity, connection, ps, isPrepare, processedObjects);
	}

	protected <ENTITY> boolean setIds(String tableName, ENTITY entity, Connection connection, PreparedStatement ps, boolean isPrepare, Map<Integer, Object> processedObjects) {
		if (entity == null) {
			return false;
		}
		int identityHashCode = System.identityHashCode(entity);
		if (processedObjects.containsKey(identityHashCode)) {
			return false;
		} else {
			processedObjects.put(identityHashCode, entity);
		}
		ObjectDesc<ENTITY> objectDesc = ObjectDescFactory.getObjectDesc(entity);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(Id.class);
		for (PropertyDesc propertyDesc : propertyDescs) {
			boolean isMatchTable = isMatchTable(tableName, propertyDesc, propertyDesc.getAnnotation(Id.class));
			if (isMatchTable == true) {
				boolean didSet = setId(tableName, entity, propertyDesc, connection,ps, isPrepare);
				if (didSet == true) {
					return true;
				}
			}
		}
		propertyDescs = objectDesc.getPropertyDescs(JavaTypes.OBJECT);
		for (PropertyDesc pd : propertyDescs) {
			if (pd.isReadable() == false) {
				continue;
			}
			if (LazyLoadingUtil.isProxy(entity) == true) {
				continue;
			}
			Object value;
			try {
				value = pd.getValue(entity);
			} catch (Exception e) {
				value = null;
			}
			boolean didSet = setIds(tableName, value, connection, ps, isPrepare, processedObjects);
			if (didSet == true) {
				return true;
			}
		}
		return false;
	}

	protected boolean isMatchTable(String tableName, PropertyDesc propertyDesc, Id id) {
		String[] generateTables = id.targetTables();
		if (0 < generateTables.length) {
			for (String table : generateTables) {
				if (StringUtil.equalsIgnoreCase(tableName, table) == true) {
					return true;
				}
			}
			return false;
		}
		Class<?> declaringClass = propertyDesc.getDeclaringClass(Id.class);
		if (declaringClass == null) {
			return false;
		}
		String table = DaoUtil.getTableName(declaringClass);
		return StringUtil.equalsIgnoreCase(tableName, table);
	}

	protected boolean isMatchTable(String tableName, PropertyDesc propertyDesc, VersionNo versionNo) {
		String[] generateTables = versionNo.targetTables();
		if (0 < generateTables.length) {
			for (String table : generateTables) {
				if (StringUtil.equalsIgnoreCase(tableName, table) == true) {
					return true;
				}
			}
			return false;
		}
		Class<?> declaringClass = propertyDesc.getDeclaringClass(VersionNo.class);
		if (declaringClass == null) {
			return false;
		}
		String table = DaoUtil.getTableName(declaringClass);
		return StringUtil.equalsIgnoreCase(tableName, table);
	}

	protected boolean isMatchTable(String tableName, PropertyDesc propertyDesc, Timestamp timestamp) {
		String[] generateTables = timestamp.targetTables();
		if (0 < generateTables.length) {
			for (String table : generateTables) {
				if (StringUtil.equalsIgnoreCase(tableName, table) == true) {
					return true;
				}
			}
			return false;
		}
		Class<?> declaringClass = propertyDesc.getDeclaringClass(Timestamp.class);
		if (declaringClass == null) {
			return false;
		}
		String table = DaoUtil.getTableName(declaringClass);
		return StringUtil.equalsIgnoreCase(tableName, table);
	}

	protected <ENTITY> boolean setId(String tableName, ENTITY entity, PropertyDesc propertyDesc, Connection connection, PreparedStatement ps, boolean isPrepare) {
		Map<Class<? extends Dialect>, IdDefSet> idDefs = getIdDefs(propertyDesc);
		IdDefSet idDefSet = getIdDefSet(idDefs, dialect.getClass());
		if (idDefSet == null) {
			idDefSet = idDefs.get(Default.class);
		}
		if (idDefSet != null) {
			Class<? extends IdGenerator> strategy = idDefSet.strategy();
			IdGenerator generator;
			if (Enum.class.isAssignableFrom(strategy)) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Class<? extends Enum> enumClass = (Class<? extends Enum>) strategy;
				generator = EnumUtil.firstValue(enumClass, IdGenerator.class);
			} else {
				generator = ClassUtil.newInstance(strategy);
			}

			if (generator.generateBeforeInsert(dialect) == isPrepare) {
				Object idValue = generator.generate(connection, ps, dialect, idDefSet.name());
				dialect.setId(entity, propertyDesc, idValue);
				return true;
			}
		}
		return false;
	}

	protected IdDefSet getIdDefSet(Map<Class<? extends Dialect>, IdDefSet> idDefs, Class<?> dialectClass) {
		if (Standard.class.equals(dialectClass)) {
			return null;
		}
		IdDefSet idDefSet = idDefs.get(dialectClass);
		if (idDefSet == null) {
			return getIdDefSet(idDefs, dialectClass.getSuperclass());
		}
		return idDefSet;
	}

	protected static final Map<PropertyDesc, Map<Class<? extends Dialect>, IdDefSet>> ID_DEFS_CACHE = CacheUtil.cacheMap();

	protected Map<Class<? extends Dialect>, IdDefSet> getIdDefs(PropertyDesc propertyDesc) {
		Map<Class<? extends Dialect>, IdDefSet> ret = ID_DEFS_CACHE.get(propertyDesc);
		if (ret != null) {
			return ret;
		}
		ret = new HashMap<>();
		Id idAnnotation = propertyDesc.getAnnotation(Id.class);
		if (idAnnotation == null) {
			ID_DEFS_CACHE.put(propertyDesc, ret);
			return ret;
		}
		IdDefSet[] idDefSets = idAnnotation.value();
		for (IdDefSet idDefSet : idDefSets) {
			Class<? extends Dialect> dialectDef = idDefSet.db();
			ret.put(dialectDef, idDefSet);
		}
		ID_DEFS_CACHE.put(propertyDesc, ret);
		return ret;
	}

	protected <ENTITY> void prepareUpdate(String tableName, ENTITY entity, Locking locking) {
		if (locking == OPTIMISTIC_LOCKING) {
			setVersionNos(tableName, entity, false);
			setTimestamps(tableName, entity);
		}
	}

	protected Node parse(String sql) {
		Node node = NODE_CACHE.get(sql);
		if (node != null) {
			return node;
		}
		node = new SqlParser(sql).parse();
		NODE_CACHE.put(sql, node);
		return node;
	}

	protected <ENTITY> int insert(String tableName, ENTITY entity, List<String> pc, List<String> npc, Locking locking, Object... entities) {

		if (entity == null) {
			throw new IllegalArgumentException(Message.getMessage("00001"));
		}
		if (isEmpty(entities) == true) {
			entities = new Object[] { entity };
		}
		TableMetaData tableMetaData = getTableMetaData(tableName);
		List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);
		Connection connection = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			prepareInsert(tableMetaData.getTableName(), entity, locking, connection);
			Map<String, ParameterValue> values = getUpdateParameterValues(entities, updateColumnNames, tableMetaData);

			String sql = createInsertSql(tableMetaData.getTableName(), updateColumnNames, values);
			Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
			int ret = triple.first;
			setIds(tableMetaData.getTableName(), entity, triple.second, triple.third, !PREPARE);
			return ret;
		} finally {
			PropertyDesc.cacheModeOff();
			ConnectionUtil.close(connection);
		}
	}

	protected PreparedStatement createPreparedStatement(Connection connection, String sql, Map<String, ParameterValue> values,
			ExecuteType executeType, Dialect dialect, SqlContext sqlContext) {
		CommandContext ctx = createCommandContext(sqlContext, dialect, values);
		Node node = parse(sql);
		return createPreparedStatement(connection, executeType, dialect, ctx, node);
	}

	protected CommandContext createCommandContext(SqlContext sqlContext, Dialect dialect, Map<String, ParameterValue> values) {
		CommandContext ctx = new CommandContext(dialect);
		ctx.addArgs(values);
		if (sqlContext != null) {
			ctx.addValues(sqlContext.getVals());
		}
		return ctx;
	}

	protected PreparedStatement createPreparedStatement(Connection connection, ExecuteType executeType, Dialect dialect, CommandContext ctx, Node node) {
		node.accept(ctx);
		List<Object> bindVariables = ctx.getBindVariables();
		List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
		String executableSql = ctx.getSql();
		PreparedStatement ps = createPreparedStatement(connection, executableSql, dialect);
		bindArgs(ps, bindVariables, bindVariableTypes, executableSql, executeType, dialect);
		return ps;
	}

	protected PreparedStatement createPreparedStatement(Connection connection, String sql, Dialect dialect) {
		PreparedStatement ps;
		try {
			ps = ConnectionUtil.prepareStatement(connection, sql);
		} catch (SQLRuntimeException e) {
			throw new InvalidSQLException(sql, e);
		}
		PreparedStatementUtil.setQueryTimeout(ps, queryTimeout);
		ps = dialect.preparedStatement(ps);
		return ps;
	}

	protected void bindArgs(PreparedStatement ps, List<Object> bindVariables, List<Integer> bindVariableTypes, ExecuteType executeType,
			Dialect dialect) {
		String sql = this.sqlLogRegistry.getLast().getRawSql();
		bindArgs(ps, bindVariables, bindVariableTypes, sql, executeType, dialect);
	}

	protected void bindArgs(PreparedStatement ps, List<Object> bindVariables, List<Integer> bindVariableTypes, String sql, ExecuteType executeType,
			Dialect dialect) {
		logSql(sql, bindVariables, bindVariableTypes, dialect, executeType);
		if (bindVariables == null || bindVariables.isEmpty()) {
			return;
		}
		for (int i = 0; i < bindVariables.size(); ++i) {
			Object value = null;
			try {
				value = bindVariables.get(i);
				if (value == null) {
					ps.setNull(i + 1, bindVariableTypes.get(i));
				} else if (value instanceof InputStream) {
					InputStream is = (InputStream) value;
					dialect.setBinaryStream(ps, i + 1, is);
				} else {
					int bindVariableType = bindVariableTypes.get(i);
					ps.setObject(i + 1, TypesUtil.getSQLType(bindVariableType).convert(value, getFormats()), bindVariableType);
				}
			} catch (SQLException e) {
				throw new SQLRuntimeException("value=" + value, e);
			}
		}
	}

	protected String createInsertSql(String tableName, List<String> updateColumnNames, Map<String, ParameterValue> values) {
		StringBuilder sql = new StringBuilder(1024);
		sql.append("INSERT INTO ").append(tableName).append(" ( ");
		updateColumnNames.forEach(updateColumnName -> sql.append(updateColumnName).append(", "));
		if (updateColumnNames.isEmpty() == false) {
			sql.setLength(sql.length() - 2);
		}
		sql.append(" ) VALUES ( ");
		updateColumnNames.forEach(
				updateColumnName -> sql.append("/*").append(updateColumnName).append("*/")
						.append(getDummyValString(updateColumnName, values)).append(" , "));
		if (updateColumnNames.isEmpty() == false) {
			sql.setLength(sql.length() - 2);
		}
		sql.append(")");
		return sql.toString();
	}

	protected String getDummyValString(String columnName, Map<String, ParameterValue> values) {
		// TODO Return a reasonable string inferred from the column name.
		return "0";
	}

	protected Map<String, ParameterValue> getUpdateParameterValues(Object[] entities, List<String> updateColumnNames, TableMetaData tableMetaData) {
		Map<String, ParameterValue> parameterValues = new HashMap<>(updateColumnNames.size());
		String tableName = tableMetaData.getTableName();
		updateColumnNames.forEach(columnName -> {
			List<CandidateValue> values = new ArrayList<>();
			gatherValue(entities, tableName, columnName, values);
			CandidateValue value = CandidateValue.getValue(values, tableName, columnName);
			int dataType = tableMetaData.getColumnMetaData(columnName).getDataType();
			ParameterValue pv = createParameterValue(columnName, dataType, value);
			parameterValues.put(columnName, pv);
		});
		return parameterValues;
	}

	protected void gatherValue(Object[] entities, String tableName, String columnName, List<CandidateValue> values) {
		Map<Integer, Object> processedObjects = new HashMap<>();
		for (Object entity : entities) {
			StringBuilder path = new StringBuilder();
			gatherValue(entity, tableName, columnName, values, path, processedObjects);
		}
	}

	protected void gatherValue(Object entity, String tableName, String columnName, List<CandidateValue> values, StringBuilder path, Map<Integer, Object> processedObjects) {

		if (entity == null) {
			return;
		}
		int identityHashCode = System.identityHashCode(entity);
		if (processedObjects.containsKey(identityHashCode)) {
			return;
		}
		processedObjects.put(identityHashCode, entity);

		if (path.length() == 0) {
			path.append(entity.getClass().getName());
		} else {
			path.append("(").append(entity.getClass().getName()).append(")");
		}

		CaseInsensitiveSet tableNameSet = new CaseInsensitiveSet(tableName);
		CaseInsensitiveSet columnNameSet = new CaseInsensitiveSet(columnName);

		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(entity);

		gatherValueFromRelsAnnotation(entity, objectDesc, tableNameSet, columnNameSet, values, path);
		gatherValueFromColumnsAnnotation(entity, objectDesc, tableNameSet, columnNameSet, values, path);
		gatherValueFromColumnAnnotation(entity, objectDesc, tableNameSet, columnNameSet, values, path);

		try {
			PropertyDesc pd = objectDesc.getPropertyDesc(columnName);
			if (pd.isReadable() == true) {
				ValueProxy value = new ValueProxy(pd, entity);
				if (logger.isTraceEnabled()) {
					logger.trace(Message.getMessage("00034", value, pd, tableName, columnName, Message.getMessage("00035"),path + "#" + pd.getPropertyName()));
				}
				values.add(new CandidateValue(value, getTableNames(pd, columnName).contains(tableName),
						CandidateValue.PRIORITY_LEVEL_CONVENTION));
			}
		} catch (PropertyNotFoundRuntimeException e) {
			List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(JavaTypes.OBJECT);
			for (PropertyDesc pd : propertyDescs) {
				if (pd.isReadable() == false) {
					continue;
				}
				Object relBean = null;
				try {
					relBean = pd.getValue(entity);
				} catch (RuntimeException re) {
					logger.warn(Message.getMessage("00052", entity.getClass().getName(), pd.getPropertyName()), re);
				}
				gatherValue(relBean, tableName, columnName, values, new StringBuilder(path).append("#").append(pd.getPropertyName()) ,processedObjects);
			}
		}
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs();
		for (PropertyDesc pd : propertyDescs) {
			JavaType<?> javaType = TypesUtil.getJavaType(pd.getPropertyType());
			if (javaType.equals(JavaTypes.OBJECT) || pd.isEnumType()) {
				if (pd.isReadable() == false) {
					continue;
				}
				Object bean = null;
				try {
					bean = pd.getValue(entity);
				} catch (RuntimeException e) {
					logger.warn(Message.getMessage("00052", entity.getClass().getName(), pd.getPropertyName()), e);
				}
				gatherValue(bean, tableName, columnName, values, new StringBuilder(path).append("#").append(pd.getPropertyName()), processedObjects);
			}
		}
	}

	protected void gatherValueFromRelsAnnotation(Object entity, ObjectDesc<?> objectDesc, CaseInsensitiveSet tableNameSet,
			CaseInsensitiveSet columnNameSet, List<CandidateValue> values, StringBuilder path) {

		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(Relations.class);
		for (PropertyDesc pd : propertyDescs) {
			Relations relations = pd.getAnnotation(Relations.class);
			Rel[] rels = relations.value();
			for (Rel rel : rels) {
				if (tableNameSet.contains(rel.table()) == false) {
					continue;
				}
				if (columnNameSet.contains(rel.column()) == false) {
					continue;
				}
				Object bean = null;
				try {
					bean = pd.getValue(entity, true);
				} catch (RuntimeException re) {
					logger.warn(Message.getMessage("00052", entity.getClass().getName(), pd.getPropertyName()), re);
				}
				if (bean == null) {
					continue;
				}
				ValueProxy value = new OGNLValueProxy(bean, "data." + rel.property());
				if (logger.isTraceEnabled()) {
					logger.trace(Message.getMessage("00034", value, pd, tableNameSet.getFirstElement(), columnNameSet.getFirstElement(),
							Message.getMessage("00038"), path + "#" + pd.getPropertyName()));
				}
				values.add(new CandidateValue(value, true, CandidateValue.PRIORITY_LEVEL_RELATIONS_ANNOTATION));
			}
		}
	}

	protected void gatherValueFromColumnAnnotation(Object entity, ObjectDesc<?> objectDesc, CaseInsensitiveSet tableNameSet,
			CaseInsensitiveSet columnNameSet, List<CandidateValue> values, StringBuilder path) {

		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(Column.class);
		for (PropertyDesc pd : propertyDescs) {
			if (pd.isReadable() == false) {
				continue;
			}
			Column column = pd.getAnnotation(Column.class);
			if (columnNameSet.contains(column.value()) == true) {
				ValueProxy value = new ValueProxy(pd, entity);
				if (logger.isTraceEnabled()) {
					logger.trace(Message.getMessage("00034", value, pd, tableNameSet.getFirstElement(), columnNameSet.getFirstElement(),
							Message.getMessage("00036"), path + "#" + pd.getPropertyName()));
				}
				values.add(new CandidateValue(value, getTableNames(pd, columnNameSet.getFirstElement()).contains(tableNameSet.getFirstElement()),
						CandidateValue.PRIORITY_LEVEL_COLUMN_ANNOTATION));
			}
		}
	}

	protected void gatherValueFromColumnsAnnotation(Object entity, ObjectDesc<?> objectDesc, CaseInsensitiveSet tableNameSet,
			CaseInsensitiveSet columnNameSet, List<CandidateValue> values, StringBuilder path) {

		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(Columns.class);
		for (PropertyDesc pd : propertyDescs) {
			if (pd.isReadable() == false) {
				continue;
			}
			Column[] columns = pd.getAnnotation(Columns.class).value();
			for (Column column : columns) {
				if (tableNameSet.contains(column.table()) && columnNameSet.contains(column.value())) {
					ValueProxy value = new ValueProxy(pd, entity);
					if (logger.isTraceEnabled()) {
						logger.trace(Message.getMessage("00034", value, pd, tableNameSet.getFirstElement(), columnNameSet.getFirstElement(),
								Message.getMessage("00037"), path + "#" + pd.getPropertyName()));
					}
					values.add(new CandidateValue(value, true, CandidateValue.PRIORITY_LEVEL_COLUMNS_ANNOTATION));
				}
			}
		}
		for (PropertyDesc pd : propertyDescs) {
			if (pd.isReadable() == false) {
				continue;
			}
			Column[] columns = pd.getAnnotation(Columns.class).value();
			for (Column column : columns) {
				if (columnNameSet.contains(column.value())) {
					ValueProxy value = new ValueProxy(pd, entity);
					if (logger.isTraceEnabled()) {
						logger.trace(Message.getMessage("00034", value, pd, tableNameSet.getFirstElement(), columnNameSet.getFirstElement(),
								Message.getMessage("00037"), path + "#" + pd.getPropertyName()));
					}
					values.add(new CandidateValue(value, getTableNames(pd, columnNameSet.getFirstElement()).contains(tableNameSet.getFirstElement()),
							CandidateValue.PRIORITY_LEVEL_COLUMNS_ANNOTATION));
				}
			}
		}
	}

	/**
	 * Unsupported next list.
	 * <ul>
	 * <li>@Id
	 * <li>@Timestamp
	 * <li>@VersionNo
	 * <li>Locking
	 * </ul>
	 */
    @Override
	public int[] executeBatch(String sql, List<Object> list) {
		if (list.isEmpty() == true) {
			return new int[] {};
		}
		Iterator<Object> iterator = list.iterator();
		if (iterator.hasNext() == false) {
			return new int[] {};
		}
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			Object object = iterator.next();

			Map<String, ParameterValue> values = new HashMap<>();
			addValues(values, object);
			sql = getSql(sql, dialect);
			ps = createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
			PreparedStatementUtil.addBatch(ps, sqlLogRegistry);

			while (iterator.hasNext()) {
				object = iterator.next();

				Node node = parse(sql);
				values = new HashMap<>();
				addValues(values, object);
				CommandContext ctx = new CommandContext(dialect);
				ctx.addArgs(values);
				node.accept(ctx);
				List<Object> bindVariables = ctx.getBindVariables();
				List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
				bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
				PreparedStatementUtil.addBatch(ps, sqlLogRegistry);
			}
			return PreparedStatementUtil.executeBatch(ps, sqlLogRegistry);
		} finally {
			PropertyDesc.cacheModeOff();
			try {
				StatementUtil.close(ps);
			} finally {
				ConnectionUtil.close(connection);
			}
		}
	}

	private void addValues(Map<String, ParameterValue> values, Object object) {
		ObjectDesc<Object> objectDesc = ObjectDescFactory.getObjectDesc(object);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs();
		for (PropertyDesc propertyDesc : propertyDescs) {
			if (propertyDesc.isReadable()) {
				String propertyName = propertyDesc.getPropertyName();
				Object value = null;
				try {
					value = propertyDesc.getValue(object);
				} catch (RuntimeException re) {
					logger.warn(Message.getMessage("00052", object.getClass().getName(), propertyDesc.getPropertyName()), re);
				}
				if (value == null) {
					continue;
				}
				int dataType = TypesUtil.getSQLType(value).getType();
				values.put(propertyName, new ParameterValue(propertyName, dataType, value, false));
			}
		}
	}

	protected <ENTITY> int[] updateBatch(Collection<ENTITY> entities, List<String> pc, List<String> npc, Locking locking) {
		if (entities.isEmpty() == true) {
			return new int[] {};
		}
		Iterator<ENTITY> iterator = entities.iterator();
		if (iterator.hasNext() == false) {
			return new int[] {};
		}
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			ENTITY entity = iterator.next();
			String tableName = getTableName(entity, getConnection());
			TableMetaData tableMetaData = getTableMetaData(tableName);
			List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);

			Map<String, ParameterValue> values = new HashMap<>();
			List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.UPDATE);
			addWhereValues(entity, whereColumnNames, tableMetaData, values);
			prepareUpdate(tableName, entity, locking);
			values.putAll(getUpdateParameterValues(new Object[] { entity }, updateColumnNames, tableMetaData));
			String sql = createUpdateSql(tableName, updateColumnNames, values, whereColumnNames);
			ps = createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
			PreparedStatementUtil.addBatch(ps, sqlLogRegistry);

			for (; iterator.hasNext();) {
				ENTITY e = iterator.next();

				Node node = parse(sql);
				values = new HashMap<>();
				addWhereValues(e, whereColumnNames, tableMetaData, values);
				prepareUpdate(tableName, e, locking);
				values.putAll(getUpdateParameterValues(new Object[] { e }, updateColumnNames, tableMetaData));
				CommandContext ctx = new CommandContext(dialect);
				ctx.addArgs(values);
				node.accept(ctx);
				List<Object> bindVariables = ctx.getBindVariables();
				List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
				bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
				PreparedStatementUtil.addBatch(ps, sqlLogRegistry);
			}
			return PreparedStatementUtil.executeBatch(ps, sqlLogRegistry);
		} finally {
			PropertyDesc.cacheModeOff();
			try {
				StatementUtil.close(ps);
			} finally {
				ConnectionUtil.close(connection);
			}
		}
	}

	protected <ENTITY> int update(String tableName, ENTITY entity, List<String> pc, List<String> npc, Locking locking, Object... entities) {
		if (entity == null) {
			throw new IllegalArgumentException(Message.getMessage("00001"));
		}
		if (isEmpty(entities) == true) {
			entities = new Object[] { entity };
		}
		try {
			PropertyDesc.cacheModeOn();
			TableMetaData tableMetaData = getTableMetaData(tableName);
			List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);
			Map<String, ParameterValue> values = new HashMap<>();
			List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.UPDATE);
			addWhereValues(entity, whereColumnNames, tableMetaData, values);
			for (Object e : entities) {
				addWhereValues(e, whereColumnNames, tableMetaData, values);
			}
			if (isAllNull(values)) {
				throw new DaoRuntimeException("00044");
			}
			prepareUpdate(tableMetaData.getTableName(), entity, locking);
			values.putAll(getUpdateParameterValues(entities, updateColumnNames, tableMetaData));
			String sql = createUpdateSql(tableMetaData.getTableName(), updateColumnNames, values, whereColumnNames);
			Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
			try {
				return triple.first;
			} finally {
				close(triple.second, triple.third);
			}
		} finally {
			PropertyDesc.cacheModeOff();
		}
	}

	protected <ENTITY> void addWhereValues(ENTITY entity, List<String> whereColumnNames, TableMetaData tableMetaData,
			Map<String, ParameterValue> values) {
		String tableName = tableMetaData.getTableName();
		for (String columnName : whereColumnNames) {
			String name = whereColumnPrefix + columnName;
			if (values.containsKey(name) == true && values.get(name).getValue() != null) {
				continue;
			}
			List<CandidateValue> vals = new ArrayList<>();
			gatherValue(new Object[]{entity}, tableName, columnName, vals);
			CandidateValue value = CandidateValue.getValue(vals, tableName, columnName);
			int dataType = tableMetaData.getColumnMetaData(columnName).getDataType();
			value.pullRealValue();
			ParameterValue pv = createParameterValue(name, dataType, value);
			values.put(name, pv);
		}
	}

	protected ParameterValue createParameterValue(String columnName, int dataType, CandidateValue value) {
		return new ParameterValue(columnName, dataType, value.value, value.matchTableName);
	}

	protected <ENTITY> List<String> getWhereColumnNames(TableMetaData tableMetaData, ENTITY entity, Locking locking, CRUD crud) {
		List<String> columnNames = tableMetaData.getPkColumnNames();
		if (columnNames.isEmpty() == true) {
			if (crud.equals(CRUD.DELETE) == false) {
				throw new IllegalStateException(Message.getMessage("00002"));
			} else {
				columnNames = tableMetaData.getColumnNames();
			}
		}
		List<String> whereColumnNames = new ArrayList<>(columnNames);

		if (locking == OPTIMISTIC_LOCKING) {
			addVersionNoColumnNames(entity, tableMetaData, whereColumnNames);
			addTimestampColumnNames(entity, tableMetaData, whereColumnNames);
		}

		return whereColumnNames;
	}

	protected <ENTITY> void addVersionNoColumnNames(ENTITY entity, TableMetaData tableMetaData, List<String> whereColumnNames) {
		addWhereColumnNames(entity, tableMetaData, VersionNo.class, whereColumnNames);
	}

	protected <ENTITY> void addTimestampColumnNames(ENTITY entity, TableMetaData tableMetaData, List<String> whereColumnNames) {
		addWhereColumnNames(entity, tableMetaData, Timestamp.class, whereColumnNames);
	}

	protected <ENTITY> void addWhereColumnNames(ENTITY entity, TableMetaData tableMetaData, Class<? extends Annotation> annotationClass,
			List<String> whereColumnNames) {

		ObjectDesc<ENTITY> objectDesc = ObjectDescFactory.getObjectDesc(entity);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs(annotationClass);
		propertyDescs.forEach(propertyDesc -> {
			String propertyName = getColumnName(propertyDesc);
			ColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(propertyName);
			whereColumnNames.add(columnMetaData.getColumnName());
		});
	}

	protected String getColumnName(PropertyDesc propertyDesc) {
		return DaoUtil.getColumnName(propertyDesc);
	}

	protected String createUpdateSql(String tableName, List<String> updateColumnNames, Map<String, ParameterValue> values,
			List<String> whereColumnNames) {
		StringBuilder sql = new StringBuilder(1024);
		sql.append("UPDATE ");
		sql.append(tableName);
		sql.append(" SET ");
		updateColumnNames.forEach(updateColumnName -> {
			sql.append(updateColumnName);
			sql.append(" = ");
			sql.append("/*" + updateColumnName + "*/" + getDummyValString(updateColumnName, values) + " ");
			sql.append(", ");
		});
		if (updateColumnNames.isEmpty() == false) {
			sql.setLength(sql.length() - 2);
		}
		sql.append("WHERE ");
		whereColumnNames.forEach(columnName -> {
			sql.append(columnName);
			sql.append(" = ");
			sql.append("/*").append(whereColumnPrefix).append(columnName).append("*/");
			sql.append(getDummyValString(columnName, values));
			sql.append(" ");
			sql.append("AND ");
		});
		if (whereColumnNames.isEmpty() == false) {
			sql.setLength(sql.length() - 4);
		}
		return sql.toString();
	}

    @Override
	public <ENTITY> int delete(ENTITY entity, Locking locking) {
		return delete(getTableName(entity, getConnection()), entity, locking);
	}

    @Override
	public <ENTITY> int delete(String tableName, ENTITY entity, Locking locking) {
		if (entity == null) {
			throw new IllegalArgumentException(Message.getMessage("00001"));
		}
		try {
			PropertyDesc.cacheModeOn();
			TableMetaData tableMetaData = getTableMetaData(tableName);
			Map<String, ParameterValue> values = new HashMap<>();
			List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.DELETE);

			addWhereValues(entity, whereColumnNames, tableMetaData, values);
			if (isAllNull(values)) {
				throw new DaoRuntimeException("00044");
			}
			String sql = createDeleteSql(tableMetaData.getTableName(), whereColumnNames, values);
			Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
			try {
				return triple.first;
			}finally {
				close(triple.second,triple.third);
			}
		} finally {
			PropertyDesc.cacheModeOff();
		}
	}

	private boolean isAllNull(Map<String, ParameterValue> values) {
		for (ParameterValue value : values.values()) {
			if (value.getValue() != null) {
				return false;
			}
		}
		return true;
	}

	protected String createDeleteSql(String tableName, List<String> whereColumnNames, Map<String, ParameterValue> values) {
		final String AND = "AND ";
		StringBuilder sql = new StringBuilder(1024);
		sql.append("DELETE FROM ").append(tableName).append(" WHERE ");
		whereColumnNames.forEach(columnName -> {
			sql.append(columnName).append(" = ");
			sql.append("/*").append(whereColumnPrefix).append(columnName).append("*/");
			sql.append(getDummyValString(columnName, values)).append(" ");
			sql.append(AND);
		});
		sql.setLength(sql.length() - AND.length());
		return sql.toString();
	}

    @Override
	public <ENTITY> int[] delete(Collection<ENTITY> entities, Locking locking) {
		return deleteBatch(entities, locking);
	}

	protected <ENTITY> int[] deleteBatch(Collection<ENTITY> entities, Locking locking) {
		if (entities.isEmpty() == true) {
			return new int[] {};
		}
		Iterator<ENTITY> iterator = entities.iterator();
		if (iterator.hasNext() == false) {
			return new int[] {};
		}
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			ENTITY entity = iterator.next();
			prepareDelete(entity, locking);
			String tableName = getTableName(entity, getConnection());
			TableMetaData tableMetaData = getTableMetaData(tableName);

			Map<String, ParameterValue> values = new HashMap<>();
			List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.DELETE);
			addWhereValues(entity, whereColumnNames, tableMetaData, values);
			String sql = createDeleteSql(tableName, whereColumnNames, values);
			ps = createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
			PreparedStatementUtil.addBatch(ps, sqlLogRegistry);

			for (; iterator.hasNext();) {
				ENTITY e = iterator.next();
				prepareDelete(e, locking);

				Node node = parse(sql);
				values = new HashMap<>();
				addWhereValues(e, whereColumnNames, tableMetaData, values);
				CommandContext ctx = new CommandContext(dialect);
				ctx.addArgs(values);
				node.accept(ctx);
				List<Object> bindVariables = ctx.getBindVariables();
				List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
				bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
				PreparedStatementUtil.addBatch(ps, sqlLogRegistry);
			}
			return PreparedStatementUtil.executeBatch(ps, sqlLogRegistry);
		} finally {
			PropertyDesc.cacheModeOff();
			try {
				StatementUtil.close(ps);
			} finally {
				ConnectionUtil.close(connection);
			}
		}
	}

	protected <ENTITY> void prepareDelete(ENTITY entity, Locking locking) {
		// empty
	}

    @Override
	public int executeInsert(String sql) {
		return executeInsert(sql, new HashMap<>());
	}

    @Override
	public int executeInsert(String sql, Map<String, Object> arg) {
		Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
		try {
			return triple.first;
		} finally {
			close(triple.second, triple.third);
		}
	}

    @Override
	public int executeUpdate(String sql) {
		return executeUpdate(sql, new HashMap<>());
	}

    @Override
	public int executeUpdate(String sql, Map<String, Object> arg) {
		Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
		try {
			return triple.first;
		} finally {
			close(triple.second, triple.third);
		}
	}

    @Override
	public int executeDelete(String sql) {
		return executeDelete(sql, new HashMap<>());
	}

    @Override
	public int executeDelete(String sql, Map<String, Object> arg) {
		Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
		try {
			return triple.first;
		} finally {
			close(triple.second, triple.third);
		}
	}

	protected void close(Connection connection , PreparedStatement ps) {
		try {
			PreparedStatementUtil.close(ps);
		} finally {
			ConnectionUtil.close(connection);
		}
	}

	protected Triple<Integer, Connection, PreparedStatement> _executeUpdate(String sql, Map<String, Object> arg) {
		Map<String, ParameterValue> values = createParameterValues(arg);
		return executeUpdate(values, sql);
	}

	protected Triple<Integer, Connection, PreparedStatement> executeUpdate(Map<String, ParameterValue> values, String sql) {
		return executeUpdate(values, sql, null, true);
	}

    @Override
	public int execute(Sql sql, Object... entity) throws SQLRuntimeException {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			Triple<Integer, Connection, PreparedStatement> result = _execute(sql, entity);
			connection = result.second();
			ps = result.third();
			return result.first;
		} finally {
			close(connection, ps);
		}
	}

	public Triple<Integer, Connection, PreparedStatement> _execute(Sql sql, Object... entity) throws SQLRuntimeException {
		List<?> list = getList(entity);
		SqlContext context = createSqlContext(entity, list, false);
		String sqlString = sql.getSql(context);
		Map<String, ParameterValue> values = toValues(entity, context.getParameters());
		return executeUpdate(values, sqlString, context, true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<?> getList(Object[] entity) {
		for (Object object : entity) {
			if (object instanceof List) {
				return (List<?>) object;
			}
			if (object instanceof Map) {
				Set<Entry> entrySet = ((Map) object).entrySet();
				for (Entry entry : entrySet) {
					Object value = entry.getValue();
					if (value instanceof List) {
						return (List<?>) value;
					}
				}
			}
		}
		return null;
	}

	protected Map<String, ParameterValue> toValues(Object[] entity, Map<String, Object> parameters) {
		Set<Entry<String, Object>> entrySet = parameters.entrySet();
		Map<String, ParameterValue> ret = new CaseInsensitiveMap<>();
		String tableName = getTableName(entity[0], getConnection());
		TableMetaData tableMetaData = getTableMetaData(tableName);
		for (Entry<String, Object> entry : entrySet) {
			String columnName = entry.getKey();
			ColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(columnName);
			if (columnMetaData == null) {
				continue;
			}
			int dataType = columnMetaData.getDataType();
			ret.put(columnName, new ParameterValue(columnName, dataType, entry.getValue(), true));
		}
		return ret;
	}

	@Internal
	public Triple<Integer, Connection, PreparedStatement> executeUpdate(Map<String, ParameterValue> values, String sql, SqlContext context, boolean isDynamic) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			if (sql.contains(" |\n|\r|\t") == false) {
				sql = getSql(sql, dialect);
			}
			if (isDynamic == true) {
				ps = createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, context);
			} else {
				logSql(sql, null, null, dialect, ExecuteType.UPDATE);
				ps = createPreparedStatement(connection, sql, dialect);
			}
			int count = PreparedStatementUtil.executeUpdate(ps, sqlLogRegistry);
			return new Triple<>(count, connection, ps);
		} catch (Exception e) {
			close(connection, ps);
			throw e;
		} finally {
			TmpFileUtil.deleteThreadLocalTmpFiles();
		}
	}

	protected Map<String, ParameterValue> createParameterValues(Map<String, Object> arg) {
		Map<String, ParameterValue> parameterValues = new HashMap<>();
		if (arg == null) {
			return parameterValues;
		}
		arg.entrySet().forEach(entry -> {
			String name = entry.getKey();
			Object value = entry.getValue();
			int dataType = TypesUtil.getSQLType(value).getType();
			ParameterValue pv = new ParameterValue(name, dataType, value, true);
			parameterValues.put(name, pv);
		});
		return parameterValues;
	}

	protected void logSql(String sql, List<Object> args, List<Integer> types, Dialect dialect, ExecuteType sqlType) {
		String completeSql = getCompleteSql(sql, args, types, dialect);

		SqlLog sqlLog = new SqlLog(sql, completeSql, args, types, sqlType);
		this.sqlLogRegistry.add(sqlLog);

		if (logger.isDebugEnabled() == true) {
			logger.debug("\n" + completeSql);
		}
	}

	protected String getCompleteSql(String sql, List<Object> args, List<Integer> types, Dialect dialect) {
		if (args == null || args.isEmpty() == true) {
			return StringUtil.trimLine(sql);
		}
		StringBuilder buf = new StringBuilder(512);
		boolean isSelect = false;
		if (sql.toUpperCase().trim().startsWith("SELECT") == true) {
			isSelect = true;
		}
		int pos = 0;
		int pos2 = 0;
		int index = 0;
		while (true) {
			pos = sql.indexOf('?', pos2);
			if (pos > 0) {
				buf.append(sql.substring(pos2, pos));
				int type = types.get(index);
				buf.append(getBindVariableText(args.get(index), type, dialect, isSelect));
				index++;
				pos2 = pos + 1;
			} else {
				buf.append(sql.substring(pos2));
				break;
			}
		}
		String completeSql = buf.toString();
		return StringUtil.trimLine(completeSql);
	}

	protected String getBindVariableText(Object value, Integer type, Dialect dialect, boolean isSelect) {
		if (value == null) {
			return "NULL";
		}
		if (value instanceof CandidateValue) {
			return getBindVariableText(((CandidateValue) value).value.getValue(), type, dialect, isSelect);
		}
		SQLType sqlType = (type != null && isSelect == false) ? TypesUtil.getSQLType(type.intValue()) : TypesUtil.getSQLType(value);

		if (value instanceof Enum) {
			value = sqlType.convert(value, getFormats());
		}

		if (sqlType == STRING) {
			return "'" + value + "'";
		} else if (sqlType == NUMBER) {
			return value.toString();
		} else if (sqlType == SQL_DATE) {
			return dialect.toDateString(JavaTypes.DATE.convert(value, getFormats()));
		} else if (sqlType == DATE || sqlType == TIMESTAMP) {
			return dialect.toTimestampString(JavaTypes.TIMESTAMP.convert(value, getFormats()));
		} else if (sqlType == BOOLEAN) {
			return value.toString();
		} else {
			return "'" + value.toString() + "'";
		}
	}

	protected String[] getFormats() {
		return config.getFormats();
	}

    @Override
	public Optional<Map<String, Object>> selectOneMap(String sql, Map<String, Object> arg) {
		List<Map<String, Object>> rows = selectMap(sql, arg);
		return getOne(rows, arg);
	}

    @Override
	public Optional<Map<String, Object>> selectOneMap(String sql) {
		return selectOneMap(sql, null);
	}

    @Override
	public Optional<BigDecimal> selectOneNumber(String sql, Map<String, Object> arg) {
		IterationCallback<BigDecimal> callback = new DBListIterationCallback<>();
		ResultSetHandler<?> handler = createResultSetHandler(BigDecimal.class, callback, this.dialect, arg);

		List<BigDecimal> rows = select(sql, arg, callback, handler);
		return getOne(rows, arg);
	}

	protected <T> Optional<T> getOne(List<T> rows, Object arg) {
		if (rows.size() == 0) {
			return Optional.empty();
		} else if (rows.size() == 1) {
			return Optional.of(rows.get(0));
		} else {
			throw new IllegalStateException(Message.getMessage("00003", ToStringer.toString(arg)));
		}
	}

    @Override
	public Optional<BigDecimal> selectOneNumber(String sql) {
		return selectOneNumber(sql, null);
	}

    @Override
	public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass) {
		return select(sql, arg, entityClass, new DBListIterationCallback<ROW>());
	}

    @Override
	public <ROW> List<ROW> select(Sql sql, Map<String, Object> arg, Class<ROW> entityClass) throws SQLRuntimeException {
		IterationCallback<ROW> callback = new DBListIterationCallback<ROW>();
		SqlContext context = createSqlContext(arg);
		String sqlString = sql.getSql(context);
		return select(sqlString, context.getParameters(), entityClass, callback);
	}

    @Override
	@SuppressWarnings("unchecked")
	public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, Each callback) {
		return select(sql, arg, entityClass, (IterationCallback<ROW>) callback);
	}

    @Override
	public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> entityClass, IterationCallback<ROW> callback) {
		ResultSetHandler<?> handler = createResultSetHandler(entityClass, callback, dialect, arg);
		return select(sql, arg, callback, handler);
	}

    @Override
	public <ROW> List<ROW> select(String sql, Class<ROW> entityClass) {
		return select(sql, null, entityClass);
	}

    @Override
	@SuppressWarnings("unchecked")
	public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, Each callback) {
		return select(sql, entityClass, (IterationCallback<ROW>) callback);
	}

    @Override
	public <ROW> List<ROW> select(String sql, Class<ROW> entityClass, IterationCallback<ROW> callback) {
		return select(sql, null, entityClass, callback);
	}

    @Override
	public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg) {
		return selectMap(sql, arg, new DBListIterationCallback<>());
	}

    @Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, IterationCallback<Map<String, Object>> callback) {

		IterationCallback<Map> callback2 = (IterationCallback<Map>) ((IterationCallback<?>) callback);
		ResultSetHandler<?> handler = createResultSetHandler(Map.class, callback2, dialect, arg);
		List<Map<String, Object>> ret = select(sql, arg, callback, handler);
		return ret;
	}

    @Override
	@SuppressWarnings({  "unchecked" })
	public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Each callback) {
		return selectMap(sql, arg, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
	}

    @Override
	public List<Map<String, Object>> selectMap(String sql) {
		return selectMap(sql, new DBListIterationCallback<>());
	}

    @Override
	public List<Map<String, Object>> selectMap(String sql, IterationCallback<Map<String, Object>> callback) {
		return selectMap(sql, null, callback);
	}

    @Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMap(String sql, Each callback) {
		return selectMap(sql, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
	}

    @Override
	public SqlLogRegistry getSqlLogRegistry() {
		return sqlLogRegistry;
	}

    @Override
	public <ROW> List<ROW> select(Sql sql, Class<ROW> entityClass) {
		return select(sql, args(TABLE_NAME, getTableName(entityClass)), entityClass);
	}

    @Override
	public <ROW> List<ROW> select(Sql sql, ROW query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return select(sqlString, context.getParameters(), getClass(query));
	}

	protected <QUERY_OR_ENTITY> SqlContext createSqlContext(QUERY_OR_ENTITY queryOrEntity) {
		return createSqlContext(new Object[] { queryOrEntity }, null, true);
	}

	@SuppressWarnings("unchecked")
	protected <QUERY_OR_ENTITY> SqlContext createSqlContext(Object[] queryOrEntity, List<?> values, boolean isSelect) {
		String tableName = getTableName(queryOrEntity[0], getConnection());
		TableMetaData tableMetaData = getTableMetaData(tableName);
		for (int i = 0; i < queryOrEntity.length; i++) {
			Object obj = queryOrEntity[i];
			if (obj instanceof CaseInsensitiveMap) {
				queryOrEntity[i] = obj;
			} else if (obj instanceof Map) {
				CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();
				map.putAll((Map<String, Object>) obj);
				queryOrEntity[i] = map;
			} else {
				queryOrEntity[i] = toMap(obj, tableName, tableMetaData);
			}
		}
		List<ParameterValue> pks = null;
		List<ParameterValue> columns = getColumns(tableMetaData, queryOrEntity);
		if (isSelect == false) {
			pks = getPks(tableName, columns);
		}
		SqlContext context = new SqlContext(tableName, pks, columns, Map.class, dialect);
		context.setValues(values);
		for (Object obj : queryOrEntity) {
			context.addParameter((Map<String, Object>) obj);
		}
		return context;
	}

	protected Map<String, Object> toMap(Object obj, String tableName, TableMetaData tableMetaData) {
		Map<String, Object> ret = new CaseInsensitiveMap<>();

		List<String> columnNames = tableMetaData.getColumnNames();
		columnNames.forEach(columnName -> {
			List<CandidateValue> values = new ArrayList<>();
			gatherValue(new Object[]{obj}, tableName, columnName, values);
			CandidateValue value = CandidateValue.getValue(values, tableName, columnName);
			ret.put(columnName, value.value.getValue());
		});
		ret.put(TABLE_NAME, tableName);

		return ret;
	}

	protected List<ParameterValue> getPks(String tableName, List<ParameterValue> columns) {
		TableMetaData tableMetaData = getTableMetaData(tableName);
		List<String> pkColumnNames = tableMetaData.getPkColumnNames();
		List<ParameterValue> pks = new ArrayList<>(pkColumnNames.size());
		for (String pkColumnName : pkColumnNames) {
			for (ParameterValue column : columns) {
				if (StringUtil.equalsIgnoreCase(pkColumnName, column.getName())) {
					pks.add(column);
				}
			}
		}
		return pks;
	}

	protected List<ParameterValue> getColumns(TableMetaData tableMetaData, Object[] objects) {
		List<ParameterValue> ret = new ArrayList<>();

		for (Object obj : objects) {
			getColumns(tableMetaData, obj, ret);
		}
		return ret;
	}

	protected void getColumns(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret) {
		if (obj instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) obj;
			List<String> columnNames = tableMetaData.getColumnNames();
			for (String columnName : columnNames) {
				Object value = map.get(columnName);
				if (map.containsKey(columnName) == true && map.get(columnName) != null) {
					ColumnMetaData cmd = tableMetaData.getColumnMetaData(columnName);
					if(notContains(ret, columnName)) {
						ret.add(new ParameterValue(columnName, cmd.getDataType(), value, true));
					}
				}
			}
			if (ret.isEmpty()) {
				List<?> list = getList(new Object[] { obj });
				if (list == null) {
					return;
				}
				Set<String> nonNullColumnNames = new HashSet<String>();
				for (Object e : list) {
					Map<String, Object> map2 = toMap(e, tableMetaData.getTableName(), tableMetaData);
					for (String colName : tableMetaData.getColumnNames()) {
						if (nonNullColumnNames.contains(colName)) {
							continue;
						}
						Object value = map2.get(colName);
						if (value != null) {
							nonNullColumnNames.add(colName);
						}
					}
				}
				addParameterValues(tableMetaData, list.get(0), ret, nonNullColumnNames);
			}
		} else {
			addParameterValues(tableMetaData, obj, ret);
		}
	}

	private void addParameterValues(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret, Set<String> nonNullColumnNames) {
		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(obj);
		for (String columnName : nonNullColumnNames) {
			PropertyDesc propertyDesc = objectDesc.getPropertyDesc(columnName);
			ColumnMetaData cmd = tableMetaData.getColumnMetaData(propertyDesc.getPropertyName());
			if (cmd != null) {
				ValueProxy value = new ValueProxy(propertyDesc, obj);
				if(notContains(ret, cmd.getColumnName())) {
					ret.add(new ParameterValue(cmd.getColumnName(), cmd.getDataType(), value, false));
				}
			}
		}
	}

	private boolean notContains(List<ParameterValue> parameterValues, String columnName) {
		for (ParameterValue parameterValue : parameterValues) {
			if(parameterValue.getName().equalsIgnoreCase(columnName)) {
				return false;
			}
		}
		return true;
	}

	private void addParameterValues(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret) {
		ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(obj);
		List<PropertyDesc> readablePropertyDescs = objectDesc.getReadablePropertyDescs();
		for (PropertyDesc propertyDesc : readablePropertyDescs) {
			ColumnMetaData cmd = tableMetaData.getColumnMetaData(propertyDesc.getPropertyName());
			if (cmd != null) {
				ValueProxy value = new ValueProxy(propertyDesc, obj);
				if(notContains(ret, cmd.getColumnName())) {
					ret.add(new ParameterValue(cmd.getColumnName(), cmd.getDataType(), value, false));
				}
			}
		}
	}

    @Override
	@SuppressWarnings("unchecked")
	public <ROW> List<ROW> select(Sql sql, ROW query, Each callback) {
		return select(sql, query, (IterationCallback<ROW>) callback);
	}

    @Override
	public <ROW> List<ROW> select(Sql sql, ROW query, IterationCallback<ROW> callback) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return select(sqlString, context.getParameters(), getClass(query), callback);
	}

    @Override
	public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectMap(sqlString, context.getParameters());
	}

    @Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMap(Object... query) {
		IterationCallback<Map<String, Object>> callback = new DBListIterationCallback<>();
		for (Object o : query) {
			if (o instanceof IterationCallback) {
				callback = (IterationCallback<Map<String, Object>>) o;
			}
		}
		return selectMap(SIMPLE_WHERE, query(query), callback);
	}

    @Override
	public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, IterationCallback<Map<String, Object>> callback) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectMap(sqlString, context.getParameters(), callback);
	}

    @Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Each callback) {
		return selectMap(sql, query, (IterationCallback<Map<String, Object>>) ((IterationCallback<?>) callback));
	}

    @Override
	public <ROW> Optional<ROW> selectOne(Sql sql, ROW query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectOne(sqlString, context.getParameters(), getClass(query));
	}

    @Override
	public Optional<Map<String, Object>> selectOneMap(Sql sql, Map<String, Object> query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectOneMap(sqlString, context.getParameters());
	}

    @Override
	public Optional<Map<String, Object>> selectOneMap(Object... query) {
		return selectOneMap(SIMPLE_WHERE, query(query));
	}

    @Override
	public Optional<BigDecimal> selectOneNumber(Sql sql, Map<String, Object> query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectOneNumber(sqlString, context.getParameters());
	}

    @Override
	public <QUERY> Optional<BigDecimal> selectOneNumber(Sql sql, QUERY query) {
		SqlContext context = createSqlContext(query);
		String sqlString = sql.getSql(context);
		return selectOneNumber(sqlString, context.getParameters());
	}

	protected <T> Class<T> getClass(T t) {
		return ClassUtil.getClass(t);
	}

    @Override
	public boolean existsRecord(String sql) {
		return existsRecord(sql, null);
	}

    @Override
	public boolean existsRecord(String sql, Map<String, Object> query) {
		List<Map<String, Object>> result = selectMap(sql, query);
		return result.isEmpty() == false;
	}

    @Override
	public boolean existsRecord(Sql sql) throws SQLRuntimeException {
		@SuppressWarnings("rawtypes")
		List<Map> result = this.select(sql, Map.class);
		return result.isEmpty() == false;
	}

    @Override
	public <QUERY> boolean existsRecord(Sql sql, QUERY query) {
		List<QUERY> result = this.select(sql, query);
		return result.isEmpty() == false;
	}

    @Override
	public boolean existsRecord(Object... query) {
		return existsRecord(SIMPLE_WHERE, query(query));
	}

	protected Object getLastInsertId() {
		return Identity.IDENTITY.generate(getConnection(), null, dialect, null);
	}

    @Override
	public <T> T getLastInsertId(Class<T> returnType) {
		Object id = getLastInsertId();
		return TypeConverter.convert(id, returnType);
	}

    @Override
	public DaoConfig getConfig() {
		return config;
	}

    @Override
	public <ENTITY> boolean exists(ENTITY entity) {
		return exists(getTableName(entity, getConnection()), entity);
	}

    @Override
	public boolean exists(String tableName, Object... entities) {
		if (entities == null) {
			throw new IllegalArgumentException(Message.getMessage("00001"));
		}
		if (entities[0] == null) {
			throw new IllegalArgumentException(Message.getMessage("00001"));
		}
		TableMetaData tableMetaData = getTableMetaData(tableName);
		Connection connection = null;
		try {
			PropertyDesc.cacheModeOn();
			connection = getConnection();
			Map<String, ParameterValue> values = new HashMap<>();
			List<String> whereColumnNames = getWhereColumnNames(tableMetaData, null, null, CRUD.UPDATE);
			for (Object entity : entities) {
				addWhereValues(entity, whereColumnNames, tableMetaData, values);
			}
			Map<String, Object> pks = toPkValues(values);
			pks.put(TABLE_NAME, tableName);

			BigDecimal count = selectOneNumber(SIMPLE_COUNT_WHERE, pks).orElse(BigDecimal.ZERO);
			if (count.longValue() == 1) {
				return true;
			}
			return false;
		} finally {
			PropertyDesc.cacheModeOff();
			ConnectionUtil.close(connection);
		}
	}

	protected Map<String, Object> toPkValues(Map<String, ParameterValue> values) {
		Map<String, Object> ret = new CaseInsensitiveMap<>();
		values.keySet().forEach(key -> ret.put(key.replaceFirst(whereColumnPrefix, ""), values.get(key).getValue()));
		return ret;
	}

	protected static DataSource lookupDataSource(String jndiName) {
		return JndiUtil.lookup(DataSource.class, jndiName);
	}

    @Override
	public void setQueryTimeout(int seconds) {
		if (0 < seconds) {
			queryTimeout = seconds;
		}
	}

    @Override
	public <ROW> List<ROW> select(String sql, Class<ROW> as, Consumer<ROW> callback) {
		return select(sql, as, new ConsumerWrapper<>(callback));
	}

    @Override
	public <ROW> List<ROW> select(String sql, Map<String, Object> arg, Class<ROW> as, Consumer<ROW> callback) {
		return select(sql, arg, as, new ConsumerWrapper<>(callback));
	}

    @Override
	public List<Map<String, Object>> selectMap(String sql, Map<String, Object> arg, Consumer<Map<String, Object>> callback) {
		return selectMap(sql, arg, new ConsumerWrapper<>(callback));
	}

    @Override
	public List<Map<String, Object>> selectMap(String sql, Consumer<Map<String, Object>> callback) {
		return selectMap(sql, new ConsumerWrapper<>(callback));
	}

    @Override
	public <ROW> List<ROW> select(Sql sql, ROW query, Consumer<ROW> callback) {
		return select(sql, query, new ConsumerWrapper<>(callback));
	}

    @Override
	public List<Map<String, Object>> selectMap(Sql sql, Map<String, Object> query, Consumer<Map<String, Object>> callback) {
		return selectMap(sql, query, new ConsumerWrapper<>(callback));
	}

	public record Triple<A, B, C>(A first, B second, C third) {
	}
}
