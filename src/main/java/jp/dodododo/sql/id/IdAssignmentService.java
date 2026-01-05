package jp.dodododo.sql.id;

import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.annotation.Timestamp;
import jp.dodododo.sql.annotation.VersionNo;
import jp.dodododo.sql.dialect.Default;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.Standard;
import jp.dodododo.sql.lazyloading.LazyLoadingUtil;
import jp.dodododo.sql.object.ObjectDesc;
import jp.dodododo.sql.object.ObjectDescFactory;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.provider.DialectProvider;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdAssignmentService {

    protected static final Map<PropertyDesc, Map<Class<? extends Dialect>, IdDefSet>> ID_DEFS_CACHE = CacheUtil.cacheMap();

    protected DialectProvider dialectProvider;

    public IdAssignmentService(DialectProvider dialectProvider) {
        this.dialectProvider = dialectProvider;
    }

    public <ENTITY> boolean setIds(String tableName, ENTITY entity, Connection connection, PreparedStatement ps, boolean isPrepare) {
        if (entity == null) {
            return false;
        }
        Map<Integer, Object> processedObjects = new HashMap<>();
        return setIds(tableName, entity, connection, ps, isPrepare, processedObjects);
    }

    public <ENTITY> boolean setIds(String tableName, ENTITY entity, Connection connection, PreparedStatement ps, boolean isPrepare, Map<Integer, Object> processedObjects) {
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
                boolean didSet = setId(tableName, entity, propertyDesc, connection, ps, isPrepare);
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

    protected <ENTITY> boolean setId(String tableName, ENTITY entity, PropertyDesc propertyDesc, Connection connection, PreparedStatement ps, boolean isPrepare) {
        Map<Class<? extends Dialect>, IdDefSet> idDefs = getIdDefs(propertyDesc);
        Dialect dialect = dialectProvider.dialect();
        IdDefSet idDefSet = getIdDefSet(idDefs, dialect.getClass());
        if (idDefSet == null) {
            idDefSet = idDefs.get(Default.class);
        }
        if (idDefSet != null) {
            Class<? extends IdGenerator> strategy = idDefSet.strategy();
            IdGenerator generator;
            if (Enum.class.isAssignableFrom(strategy)) {
                @SuppressWarnings({"unchecked", "rawtypes"})
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

    public boolean isMatchTable(String tableName, PropertyDesc propertyDesc, Id id) {
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
        String table = SqlUtil.getTableName(declaringClass);
        return StringUtil.equalsIgnoreCase(tableName, table);
    }

    public boolean isMatchTable(String tableName, PropertyDesc propertyDesc, VersionNo versionNo) {
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
        String table = SqlUtil.getTableName(declaringClass);
        return StringUtil.equalsIgnoreCase(tableName, table);
    }

    public boolean isMatchTable(String tableName, PropertyDesc propertyDesc, Timestamp timestamp) {
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
        String table = SqlUtil.getTableName(declaringClass);
        return StringUtil.equalsIgnoreCase(tableName, table);
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
}
