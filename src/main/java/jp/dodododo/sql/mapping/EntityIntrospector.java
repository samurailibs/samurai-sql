package jp.dodododo.sql.mapping;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Columns;
import jp.dodododo.sql.annotation.Rel;
import jp.dodododo.sql.annotation.Relations;
import jp.dodododo.sql.exception.PropertyNotFoundRuntimeException;
import jp.dodododo.sql.metadata.MetaDataService;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.metadata.ColumnMetaData;
import jp.dodododo.sql.metadata.TableMetaData;
import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.types.JavaType;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.util.ArgsUtil;
import jp.dodododo.sql.util.CaseInsensitiveMap;
import jp.dodododo.sql.util.CaseInsensitiveSet;
import jp.dodododo.sql.util.TypesUtil;
import jp.dodododo.sql.value.CandidateValue;
import jp.dodododo.sql.value.OGNLValueProxy;
import jp.dodododo.sql.value.ParameterValue;
import jp.dodododo.sql.value.ValueProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static jp.dodododo.sql.util.SqlUtil.TABLE_NAME;
import static jp.dodododo.sql.util.SqlUtil.getTableNames;

public class EntityIntrospector {
    protected static final Logger logger = LoggerFactory.getLogger(EntityIntrospector.class);

    protected MetaDataService metaDataService;

    public EntityIntrospector(MetaDataService metaDataService) {
        this.metaDataService = metaDataService;
    }

    public void gatherValue(Object[] entities, String tableName, String columnName, List<CandidateValue> values) {
        Map<Object, Object> processedObjects = new IdentityHashMap<>();
        for (Object entity : entities) {
            StringBuilder path = new StringBuilder();
            gatherValue(entity, tableName, columnName, values, path, processedObjects);
        }
    }

    public void gatherValue(Object entity, String tableName, String columnName, List<CandidateValue> values, StringBuilder path, Map<Object, Object> processedObjects) {

        if (entity == null) {
            return;
        }
        if (processedObjects.containsKey(entity)) {
            return;
        }
        processedObjects.put(entity, entity);

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
                    logger.trace(Message.getMessage("00034", value, pd, tableName, columnName, Message.getMessage("00035"), path + "#" + pd.getPropertyName()));
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
                gatherValue(relBean, tableName, columnName, values, new StringBuilder(path).append("#").append(pd.getPropertyName()), processedObjects);
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

    public Map<String, Object> toMap(Object obj, String tableName, TableMetaData tableMetaData) {
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

    protected void getColumns(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret) {
        if (obj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            List<String> columnNames = tableMetaData.getColumnNames();
            for (String columnName : columnNames) {
                Object value = map.get(columnName);
                if (map.containsKey(columnName) == true && map.get(columnName) != null) {
                    ColumnMetaData cmd = metaDataService.getColumnMetaData(tableMetaData, columnName);
                    if (notContains(ret, columnName)) {
                        ret.add(new ParameterValue(columnName, cmd.getDataType(), value, true));
                    }
                }
            }
            if (ret.isEmpty()) {
                List<?> list = ArgsUtil.getList(new Object[]{obj});
                if (list == null) {
                    return;
                }
                Set<String> nonNullColumnNames = new HashSet<>();
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

    public List<ParameterValue> getColumns(TableMetaData tableMetaData, Object[] objects) {
        List<ParameterValue> ret = new ArrayList<>();

        for (Object obj : objects) {
            getColumns(tableMetaData, obj, ret);
        }
        return ret;
    }

    private void addParameterValues(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret) {
        ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(obj);
        List<PropertyDesc> readablePropertyDescs = objectDesc.getReadablePropertyDescs();
        for (PropertyDesc propertyDesc : readablePropertyDescs) {
            ColumnMetaData cmd = metaDataService.getColumnMetaData(tableMetaData, propertyDesc.getPropertyName());
            if (cmd != null) {
                ValueProxy value = new ValueProxy(propertyDesc, obj);
                if (notContains(ret, cmd.getColumnName())) {
                    ret.add(new ParameterValue(cmd.getColumnName(), cmd.getDataType(), value, false));
                }
            }
        }
    }

    private void addParameterValues(TableMetaData tableMetaData, Object obj, List<ParameterValue> ret, Set<String> nonNullColumnNames) {
        ObjectDesc<?> objectDesc = ObjectDescFactory.getObjectDesc(obj);
        for (String columnName : nonNullColumnNames) {
            PropertyDesc propertyDesc = objectDesc.getPropertyDesc(columnName);
            ColumnMetaData cmd = metaDataService.getColumnMetaData(tableMetaData, propertyDesc.getPropertyName());
            if (cmd != null) {
                ValueProxy value = new ValueProxy(propertyDesc, obj);
                if (notContains(ret, cmd.getColumnName())) {
                    ret.add(new ParameterValue(cmd.getColumnName(), cmd.getDataType(), value, false));
                }
            }
        }
    }

    public Map<String, ParameterValue> createParameterValues(Map<String, Object> arg) {
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

    private boolean notContains(List<ParameterValue> parameterValues, String columnName) {
        for (ParameterValue parameterValue : parameterValues) {
            if (parameterValue.getName().equalsIgnoreCase(columnName)) {
                return false;
            }
        }
        return true;
    }

}
