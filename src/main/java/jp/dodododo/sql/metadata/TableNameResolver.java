package jp.dodododo.sql.metadata;

import jp.dodododo.sql.util.CacheUtil;
import jp.dodododo.sql.util.DBUtil;
import jp.dodododo.sql.util.SqlUtil;

import java.sql.Connection;
import java.util.Map;

public class TableNameResolver {
    protected static final Map<String, String> ALL_TABLE_NAMES_CACHE = CacheUtil.cacheCaseInsensitiveMap();
    protected static final Map<String, String> TABLE_NAME_CACHE = CacheUtil.cacheMap();

    public String getTableName(Object o, Connection connection) {
        String tableName = SqlUtil.getTableName(o);
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
}
