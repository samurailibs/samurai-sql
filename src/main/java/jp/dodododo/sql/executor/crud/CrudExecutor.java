package jp.dodododo.sql.executor.crud;

import jp.dodododo.sql.CRUD;
import jp.dodododo.sql.annotation.*;
import jp.dodododo.sql.columns.NoPersistentColumns;
import jp.dodododo.sql.columns.PersistentColumns;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.env.SqlEnvironment;
import jp.dodododo.sql.exception.MissingPrimaryKeyValueException;
import jp.dodododo.sql.exception.SQLRuntimeException;
import jp.dodododo.sql.executor.common.PreparedStatementFactory;
import jp.dodododo.sql.id.GeneratedValue;
import jp.dodododo.sql.id.IdAssignmentService;
import jp.dodododo.sql.id.IdGenerator;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.lock.Locking;
import jp.dodododo.sql.log.ExecuteType;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.mapping.EntityIntrospector;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.metadata.ColumnMetaData;
import jp.dodododo.sql.metadata.MetaDataService;
import jp.dodododo.sql.metadata.TableMetaData;
import jp.dodododo.sql.metadata.TableNameResolver;
import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.object.PropertyDescCacheScope;
import jp.dodododo.sql.provider.ConnectionProvider;
import jp.dodododo.sql.provider.DialectProvider;
import jp.dodododo.sql.sql.Sql;
import jp.dodododo.sql.sql.SqlContext;
import jp.dodododo.sql.sql.node.Node;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.util.*;
import jp.dodododo.sql.value.CandidateValue;
import jp.dodododo.sql.value.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import static jp.dodododo.sql.lock.OptimisticLocking.OPTIMISTIC_LOCKING;
import static jp.dodododo.sql.util.EmptyUtil.isEmpty;

public class CrudExecutor {
    protected static final Logger logger = LoggerFactory.getLogger(CrudExecutor.class);

    protected static final boolean PREPARE = true;

    protected static final Map<String, List<String>> UPDATE_COLUMN_NAMES_CACHE = CacheUtil.cacheMap();

    protected ConnectionProvider connectionProvider;
    protected DialectProvider dialectProvider;
    protected SqlEnvironment sqlEnvironment;
    protected TableNameResolver tableNameResolver;
    protected EntityIntrospector entityIntrospector;
    protected IdAssignmentService idAssignmentService;
    protected PreparedStatementFactory statementFactory;
    protected CrudSqlBuilder sqlBuilder = new CrudSqlBuilder();
    protected MetaDataService metaDataService;

    public CrudExecutor(MetaDataService metaDataService,
                        ConnectionProvider connectionProvider,
                        DialectProvider dialectProvider,
                        SqlEnvironment sqlEnvironment,
                        TableNameResolver tableNameResolver,
                        EntityIntrospector entityIntrospector,
                        PreparedStatementFactory statementFactory) {
        this.metaDataService = metaDataService;
        this.connectionProvider = connectionProvider;
        this.dialectProvider = dialectProvider;
        this.sqlEnvironment = sqlEnvironment;
        this.tableNameResolver = tableNameResolver;
        this.entityIntrospector = entityIntrospector;
        this.statementFactory = statementFactory;
        this.idAssignmentService = new IdAssignmentService(dialectProvider);
    }

    public int insert(String tableName, Object... entity) {
        ObjectDesc<Object> desc = ObjectDescFactory.getObjectDesc(entity[0]);
        TableMetaData tableMetaData = metaDataService.getTableMetaData(SqlUtil.getTableName(desc.getTargetClass()));
        List<PropertyDesc> propertyDescs = desc.getPropertyDescs();
        List<String> npc = new ArrayList<>();
        Dialect dialect = dialectProvider.dialect();
        for (PropertyDesc pd : propertyDescs) {
            Id id = pd.getAnnotation(Id.class);
            if (id == null) {
                continue;
            }

            for (IdDefSet idDefSet : id.value()) {
                Class<? extends IdGenerator> strategy = idDefSet.strategy();
                Class<? extends Dialect> db = idDefSet.db();
                if (db != null && !db.isInstance(dialect)) {
                    continue;
                }
                if (GeneratedValue.class.equals(strategy)) {
                    ColumnMetaData columnMetaData = metaDataService.getColumnMetaData(tableMetaData, pd.getPropertyName());
                    npc.add(columnMetaData.getColumnName());
                }
            }

        }
        return insert(tableName, entity[0], null, npc, OPTIMISTIC_LOCKING, entity);
    }

    public <ENTITY> int insert(ENTITY entity) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity);
    }

    protected Connection getConnection() {
        return connectionProvider.connection();
    }

    public <ENTITY> int delete(String tableName, ENTITY entity) {
        return delete(tableName, entity, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int delete(ENTITY entity) {
        return delete(tableNameResolver.getTableName(entity, getConnection()), entity);
    }

    public int update(String tableName, Object... entity) {
        return update(tableName, entity[0], null, null, OPTIMISTIC_LOCKING, entity);
    }

    public <ENTITY> int update(ENTITY entity) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity);
    }

    public <ENTITY> int[] delete(Collection<ENTITY> entities) {
        return delete(entities, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int[] insert(Collection<ENTITY> entities) {
        return insert(entities, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int[] update(Collection<ENTITY> entities) {
        return update(entities, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc) {
        return insert(tableName, entity, null, npc.getColumnList(), OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity, npc);
    }

    public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc) {
        return insert(tableName, entity, pc.getColumnList(), null, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int insert(ENTITY entity, PersistentColumns pc) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity, pc);
    }

    public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc) {
        return insert(entities, npc, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int insert(String tableName, ENTITY entity, Locking locking) {
        return insert(tableName, entity, null, null, locking);
    }

    public <ENTITY> int insert(ENTITY entity, Locking locking) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity, locking);
    }

    public <ENTITY> int insert(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return insert(tableName, entity, null, npc.getColumnList(), locking);
    }

    public <ENTITY> int insert(ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity, npc, locking);
    }

    public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc) {
        return insert(entities, pc, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int insert(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
        return insert(tableName, entity, pc.getColumnList(), null, locking);
    }

    public <ENTITY> int insert(ENTITY entity, PersistentColumns pc, Locking locking) {
        return insert(tableNameResolver.getTableName(entity, getConnection()), entity, pc, locking);
    }

    public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc) {
        return update(tableName, entity, npc, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity, npc);
    }

    public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc) {
        return update(tableName, entity, pc, OPTIMISTIC_LOCKING);
    }

    public <ENTITY> int update(ENTITY entity, PersistentColumns pc) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity, pc);
    }

    public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc) {
        return update(entities, npc, OPTIMISTIC_LOCKING);
    }

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

    public <ENTITY> int[] insert(Collection<ENTITY> entities, Locking locking) {
        return insertBatch(entities, null, null, locking);
    }

    public <ENTITY> int[] insert(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
        return insertBatch(entities, null, npc.getColumnList(), locking);
    }

    public <ENTITY> int[] insert(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
        return insertBatch(entities, pc.getColumnList(), null, locking);
    }

    public <ENTITY> int update(String tableName, ENTITY entity, Locking locking) {
        return update(tableName, entity, null, null, locking);
    }

    public <ENTITY> int update(ENTITY entity, Locking locking) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity, locking);
    }

    public <ENTITY> int update(String tableName, ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return update(tableName, entity, null, npc.getColumnList(), locking);
    }

    public <ENTITY> int update(ENTITY entity, NoPersistentColumns npc, Locking locking) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity, npc, locking);
    }

    public <ENTITY> int update(String tableName, ENTITY entity, PersistentColumns pc, Locking locking) {
        return update(tableName, entity, pc.getColumnList(), null, locking);
    }

    public <ENTITY> int update(ENTITY entity, PersistentColumns pc, Locking locking) {
        return update(tableNameResolver.getTableName(entity, getConnection()), entity, pc, locking);
    }

    public <ENTITY> int[] update(Collection<ENTITY> entities, Locking locking) {
        return updateBatch(entities, null, null, locking);
    }

    public <ENTITY> int[] update(Collection<ENTITY> entities, NoPersistentColumns npc, Locking locking) {
        return updateBatch(entities, null, npc.getColumnList(), locking);
    }

    public <ENTITY> int[] update(Collection<ENTITY> entities, PersistentColumns pc, Locking locking) {
        return updateBatch(entities, pc.getColumnList(), null, locking);
    }

    protected <ENTITY> int[] insertBatch(Collection<ENTITY> entities, List<String> pc, List<String> npc, Locking locking) {
        if (entities.isEmpty() == true) {
            return new int[]{};
        }
        Iterator<ENTITY> iterator = entities.iterator();
        if (iterator.hasNext() == false) {
            return new int[]{};
        }
        Connection connection = null;
        PreparedStatement ps = null;
        Dialect dialect = dialectProvider.dialect();
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            ENTITY entity = iterator.next();
            String tableName = tableNameResolver.getTableName(entity, connection);
            prepareInsert(tableName, entity, locking, connection);
            TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
            List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);

            Map<String, ParameterValue> values = getUpdateParameterValues(new Object[]{entity}, updateColumnNames, tableMetaData);
            String sql = sqlBuilder.createInsertSql(tableName, updateColumnNames, values);
            ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
            PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());

            for (; iterator.hasNext(); ) {
                ENTITY e = iterator.next();
                prepareInsert(tableName, e, locking, connection);

                Node node = sqlEnvironment.sqlNodeCache().getOrParse(sql);
                values = getUpdateParameterValues(new Object[]{e}, updateColumnNames, tableMetaData);
                CommandContext ctx = new CommandContext(dialect);
                ctx.addArgs(values);
                node.accept(ctx);
                List<Object> bindVariables = ctx.getBindVariables();
                List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
                statementFactory.bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
                PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());
            }
            int[] counts = PreparedStatementUtil.executeBatch(ps, sqlEnvironment.sqlLogRegistry());
            if (entities.size() == 1) {
                idAssignmentService.setIds(tableName, entity, connection, ps, !PREPARE);
            }
            return counts;
        } finally {
            try {
                StatementUtil.close(ps);
            } finally {
                ConnectionUtil.close(connection);
            }
        }
    }

    protected <ENTITY> void prepareInsert(String tableName, ENTITY entity, Locking locking, Connection connection) {
        idAssignmentService.setIds(tableName, entity, connection, null, PREPARE);
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
            boolean isMatchTable = idAssignmentService.isMatchTable(tableName, propertyDesc, timestamp);
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
            boolean isMatchTable = idAssignmentService.isMatchTable(tableName, propertyDesc, versionNo);
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

    protected <ENTITY> void prepareUpdate(String tableName, ENTITY entity, Locking locking) {
        if (locking == OPTIMISTIC_LOCKING) {
            setVersionNos(tableName, entity, false);
            setTimestamps(tableName, entity);
        }
    }

    protected <ENTITY> int insert(String tableName, ENTITY entity, List<String> pc, List<String> npc, Locking locking, Object... entities) {

        if (entity == null) {
            throw new IllegalArgumentException(Message.getMessage("00001"));
        }
        if (isEmpty(entities) == true) {
            entities = new Object[]{entity};
        }
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);
        Connection connection = null;
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            prepareInsert(tableMetaData.getTableName(), entity, locking, connection);
            Map<String, ParameterValue> values = getUpdateParameterValues(entities, updateColumnNames, tableMetaData);

            String sql = sqlBuilder.createInsertSql(tableMetaData.getTableName(), updateColumnNames, values);
            Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
            int ret = triple.first();
            idAssignmentService.setIds(tableMetaData.getTableName(), entity, triple.second(), triple.third(), !PREPARE);
            return ret;
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    protected Map<String, ParameterValue> getUpdateParameterValues(Object[] entities, List<String> updateColumnNames, TableMetaData tableMetaData) {
        Map<String, ParameterValue> parameterValues = new HashMap<>(updateColumnNames.size());
        String tableName = tableMetaData.getTableName();
        updateColumnNames.forEach(columnName -> {
            List<CandidateValue> values = new ArrayList<>();
            entityIntrospector.gatherValue(entities, tableName, columnName, values);
            CandidateValue value = CandidateValue.getValue(values, tableName, columnName);
            int dataType = metaDataService.getColumnMetaData(tableMetaData, columnName).getDataType();
            ParameterValue pv = createParameterValue(columnName, dataType, value);
            parameterValues.put(columnName, pv);
        });
        return parameterValues;
    }

    public int[] executeBatch(String sql, List<Object> list) {
        if (list.isEmpty() == true) {
            return new int[]{};
        }
        Iterator<Object> iterator = list.iterator();
        if (iterator.hasNext() == false) {
            return new int[]{};
        }
        Connection connection = null;
        PreparedStatement ps = null;
        Dialect dialect = dialectProvider.dialect();
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            Object object = iterator.next();

            Map<String, ParameterValue> values = new HashMap<>();
            addValues(values, object);
            sql = sqlEnvironment.sqlResourceLoader().getSql(sql, dialect);
            ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
            PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());

            while (iterator.hasNext()) {
                object = iterator.next();

                Node node = sqlEnvironment.sqlNodeCache().getOrParse(sql);
                values = new HashMap<>();
                addValues(values, object);
                CommandContext ctx = new CommandContext(dialect);
                ctx.addArgs(values);
                node.accept(ctx);
                List<Object> bindVariables = ctx.getBindVariables();
                List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
                statementFactory.bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
                PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());
            }
            return PreparedStatementUtil.executeBatch(ps, sqlEnvironment.sqlLogRegistry());
        } finally {
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
            return new int[]{};
        }
        Iterator<ENTITY> iterator = entities.iterator();
        if (iterator.hasNext() == false) {
            return new int[]{};
        }
        Connection connection = null;
        PreparedStatement ps = null;
        Dialect dialect = dialectProvider.dialect();
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            ENTITY entity = iterator.next();
            String tableName = tableNameResolver.getTableName(entity, getConnection());
            TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
            List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);

            Map<String, ParameterValue> values = new HashMap<>();
            List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.UPDATE);
            addWhereValues(entity, whereColumnNames, tableMetaData, values);
            prepareUpdate(tableName, entity, locking);
            values.putAll(getUpdateParameterValues(new Object[]{entity}, updateColumnNames, tableMetaData));
            String sql = sqlBuilder.createUpdateSql(tableName, updateColumnNames, values, whereColumnNames);
            ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
            PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());

            for (; iterator.hasNext(); ) {
                ENTITY e = iterator.next();

                Node node = sqlEnvironment.sqlNodeCache().getOrParse(sql);
                values = new HashMap<>();
                addWhereValues(e, whereColumnNames, tableMetaData, values);
                prepareUpdate(tableName, e, locking);
                values.putAll(getUpdateParameterValues(new Object[]{e}, updateColumnNames, tableMetaData));
                CommandContext ctx = new CommandContext(dialect);
                ctx.addArgs(values);
                node.accept(ctx);
                List<Object> bindVariables = ctx.getBindVariables();
                List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
                statementFactory.bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
                PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());
            }
            return PreparedStatementUtil.executeBatch(ps, sqlEnvironment.sqlLogRegistry());
        } finally {
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
            entities = new Object[]{entity};
        }
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
            List<String> updateColumnNames = getUpdateColumnNames(tableMetaData, pc, npc);
            Map<String, ParameterValue> values = new HashMap<>();
            List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.UPDATE);
            addWhereValues(entity, whereColumnNames, tableMetaData, values);
            for (Object e : entities) {
                addWhereValues(e, whereColumnNames, tableMetaData, values);
            }
            if (isAllNull(values)) {
                throw new MissingPrimaryKeyValueException("00044");
            }
            prepareUpdate(tableMetaData.getTableName(), entity, locking);
            values.putAll(getUpdateParameterValues(entities, updateColumnNames, tableMetaData));
            String sql = sqlBuilder.createUpdateSql(tableMetaData.getTableName(), updateColumnNames, values, whereColumnNames);
            Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
            try {
                return triple.first();
            } finally {
                close(triple.second(), triple.third());
            }
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
            ColumnMetaData columnMetaData = metaDataService.getColumnMetaData(tableMetaData, propertyName);
            whereColumnNames.add(columnMetaData.getColumnName());
        });
    }

    protected String getColumnName(PropertyDesc propertyDesc) {
        return SqlUtil.getColumnName(propertyDesc);
    }

    public <ENTITY> int delete(ENTITY entity, Locking locking) {
        return delete(tableNameResolver.getTableName(entity, getConnection()), entity, locking);
    }

    public <ENTITY> int delete(String tableName, ENTITY entity, Locking locking) {
        if (entity == null) {
            throw new IllegalArgumentException(Message.getMessage("00001"));
        }
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
            Map<String, ParameterValue> values = new HashMap<>();
            List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.DELETE);

            addWhereValues(entity, whereColumnNames, tableMetaData, values);
            if (isAllNull(values)) {
                throw new MissingPrimaryKeyValueException("00044");
            }
            String sql = sqlBuilder.createDeleteSql(tableMetaData.getTableName(), whereColumnNames, values);
            Triple<Integer, Connection, PreparedStatement> triple = executeUpdate(values, sql);
            try {
                return triple.first();
            } finally {
                close(triple.second(), triple.third());
            }
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

    public <ENTITY> int[] delete(Collection<ENTITY> entities, Locking locking) {
        return deleteBatch(entities, locking);
    }

    protected <ENTITY> int[] deleteBatch(Collection<ENTITY> entities, Locking locking) {
        if (entities.isEmpty() == true) {
            return new int[]{};
        }
        Iterator<ENTITY> iterator = entities.iterator();
        if (iterator.hasNext() == false) {
            return new int[]{};
        }
        Connection connection = null;
        PreparedStatement ps = null;
        Dialect dialect = dialectProvider.dialect();
        try (PropertyDescCacheScope propertyDescCacheScope = new PropertyDescCacheScope()){
            connection = getConnection();
            ENTITY entity = iterator.next();
            prepareDelete(entity, locking);
            String tableName = tableNameResolver.getTableName(entity, getConnection());
            TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);

            Map<String, ParameterValue> values = new HashMap<>();
            List<String> whereColumnNames = getWhereColumnNames(tableMetaData, entity, locking, CRUD.DELETE);
            addWhereValues(entity, whereColumnNames, tableMetaData, values);
            String sql = sqlBuilder.createDeleteSql(tableName, whereColumnNames, values);
            ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, null);
            PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());

            for (; iterator.hasNext(); ) {
                ENTITY e = iterator.next();
                prepareDelete(e, locking);

                Node node = sqlEnvironment.sqlNodeCache().getOrParse(sql);
                values = new HashMap<>();
                addWhereValues(e, whereColumnNames, tableMetaData, values);
                CommandContext ctx = new CommandContext(dialect);
                ctx.addArgs(values);
                node.accept(ctx);
                List<Object> bindVariables = ctx.getBindVariables();
                List<Integer> bindVariableTypes = ctx.getBindVariableTypes();
                statementFactory.bindArgs(ps, bindVariables, bindVariableTypes, ExecuteType.UPDATE, dialect);
                PreparedStatementUtil.addBatch(ps, sqlEnvironment.sqlLogRegistry());
            }
            return PreparedStatementUtil.executeBatch(ps, sqlEnvironment.sqlLogRegistry());
        } finally {
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

    public int executeInsert(String sql) {
        return executeInsert(sql, new HashMap<>());
    }

    public int executeInsert(String sql, Map<String, Object> arg) {
        Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
        try {
            return triple.first();
        } finally {
            close(triple.second(), triple.third());
        }
    }

    public int executeUpdate(String sql) {
        return executeUpdate(sql, new HashMap<>());
    }

    public int executeUpdate(String sql, Map<String, Object> arg) {
        Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
        try {
            return triple.first();
        } finally {
            close(triple.second(), triple.third());
        }
    }

    public int executeDelete(String sql) {
        return executeDelete(sql, new HashMap<>());
    }

    public int executeDelete(String sql, Map<String, Object> arg) {
        Triple<Integer, Connection, PreparedStatement> triple = _executeUpdate(sql, arg);
        try {
            return triple.first();
        } finally {
            close(triple.second(), triple.third());
        }
    }

    protected void close(Connection connection, PreparedStatement ps) {
        try {
            PreparedStatementUtil.close(ps);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    protected Triple<Integer, Connection, PreparedStatement> _executeUpdate(String sql, Map<String, Object> arg) {
        Map<String, ParameterValue> values = entityIntrospector.createParameterValues(arg);
        return executeUpdate(values, sql);
    }

    public Triple<Integer, Connection, PreparedStatement> executeUpdate(Map<String, ParameterValue> values, String sql) {
        return executeUpdate(values, sql, null, true);
    }

    public int execute(Sql sql, Object... entity) throws SQLRuntimeException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            Triple<Integer, Connection, PreparedStatement> result = _execute(sql, entity);
            connection = result.second();
            ps = result.third();
            return result.first();
        } finally {
            close(connection, ps);
        }
    }

    public Triple<Integer, Connection, PreparedStatement> _execute(Sql sql, Object... entity) throws SQLRuntimeException {
        List<?> list = ArgsUtil.getList(entity);
        SqlContext context = createSqlContext(entity, list, false);
        String sqlString = sql.getSql(context);
        Map<String, ParameterValue> values = toValues(entity, context.getParameters());
        return executeUpdate(values, sqlString, context, true);
    }

    protected Map<String, ParameterValue> toValues(Object[] entity, Map<String, Object> parameters) {
        Set<Map.Entry<String, Object>> entrySet = parameters.entrySet();
        Map<String, ParameterValue> ret = new CaseInsensitiveMap<>();
        String tableName = tableNameResolver.getTableName(entity[0], getConnection());
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        for (Map.Entry<String, Object> entry : entrySet) {
            String columnName = entry.getKey();
            ColumnMetaData columnMetaData = metaDataService.getColumnMetaData(tableMetaData, columnName);
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
        Dialect dialect = dialectProvider.dialect();
        try {
            connection = getConnection();
            if (sql.contains(" |\n|\r|\t") == false) {
                sql = sqlEnvironment.sqlResourceLoader().getSql(sql, dialect);
            }
            if (isDynamic == true) {
                ps = statementFactory.createPreparedStatement(connection, sql, values, ExecuteType.UPDATE, dialect, context);
            } else {
                sqlEnvironment.sqlLogger().logSql(sql, null, null, dialect, ExecuteType.UPDATE);
                ps = statementFactory.createPreparedStatement(connection, sql, dialect);
            }
            int count = PreparedStatementUtil.executeUpdate(ps, sqlEnvironment.sqlLogRegistry());
            return new Triple<>(count, connection, ps);
        } catch (Exception e) {
            close(connection, ps);
            throw e;
        } finally {
            TmpFileUtil.deleteThreadLocalTmpFiles();
        }
    }

    public SqlLogRegistry getSqlLogRegistry() {
        return sqlEnvironment.sqlLogRegistry();
    }

    protected <QUERY_OR_ENTITY> SqlContext createSqlContext(QUERY_OR_ENTITY queryOrEntity) {
        return createSqlContext(new Object[]{queryOrEntity}, null, true);
    }

    @SuppressWarnings("unchecked")
    protected <QUERY_OR_ENTITY> SqlContext createSqlContext(Object[] queryOrEntity, List<?> values, boolean isSelect) {
        String tableName = tableNameResolver.getTableName(queryOrEntity[0], getConnection());
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
        for (int i = 0; i < queryOrEntity.length; i++) {
            Object obj = queryOrEntity[i];
            if (obj instanceof CaseInsensitiveMap) {
                queryOrEntity[i] = obj;
            } else if (obj instanceof Map) {
                CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();
                map.putAll((Map<String, Object>) obj);
                queryOrEntity[i] = map;
            } else {
                queryOrEntity[i] = entityIntrospector.toMap(obj, tableName, tableMetaData);
            }
        }
        List<ParameterValue> pks = null;
        List<ParameterValue> columns = entityIntrospector.getColumns(tableMetaData, queryOrEntity);
        if (isSelect == false) {
            pks = getPks(tableName, columns);
        }
        Dialect dialect = dialectProvider.dialect();
        SqlContext context = new SqlContext(tableName, pks, columns, Map.class, dialect);
        context.setValues(values);
        for (Object obj : queryOrEntity) {
            context.addParameter((Map<String, Object>) obj);
        }
        return context;
    }

    protected List<ParameterValue> getPks(String tableName, List<ParameterValue> columns) {
        TableMetaData tableMetaData = metaDataService.getTableMetaData(tableName);
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

    protected <T> Class<T> getClass(T t) {
        return ClassUtil.getClass(t);
    }

    protected Object getLastInsertId() {
        Dialect dialect = dialectProvider.dialect();
        return Identity.IDENTITY.generate(getConnection(), null, dialect, null);
    }

    public <T> T getLastInsertId(Class<T> returnType) {
        Object id = getLastInsertId();
        return TypeConverter.convert(id, returnType);
    }

    public SqlConfig getConfig() {
        return sqlEnvironment.sqlConfig();
    }

    protected Map<String, Object> toPkValues(Map<String, ParameterValue> values) {
        Map<String, Object> ret = new CaseInsensitiveMap<>();
        values.keySet().forEach(key -> ret.put(key.replaceFirst(sqlBuilder.whereColumnPrefix(), ""), values.get(key).getValue()));
        return ret;
    }

    protected <ENTITY> void addWhereValues(ENTITY entity, List<String> whereColumnNames, TableMetaData tableMetaData,
                                           Map<String, ParameterValue> values) {
        String tableName = tableMetaData.getTableName();
        for (String columnName : whereColumnNames) {
            String name = sqlBuilder.whereColumnPrefix + columnName;
            if (values.containsKey(name) == true && values.get(name).getValue() != null) {
                continue;
            }
            List<CandidateValue> vals = new ArrayList<>();
            entityIntrospector.gatherValue(new Object[]{entity}, tableName, columnName, vals);
            CandidateValue value = CandidateValue.getValue(vals, tableName, columnName);
            int dataType = tableMetaData.getColumnMetaData(columnName).getDataType();
            value.pullRealValue();
            ParameterValue pv = createParameterValue(name, dataType, value);
            values.put(name, pv);
        }
    }

    public record Triple<A, B, C>(A first, B second, C third) {
    }
}
