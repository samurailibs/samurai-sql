package jp.dodododo.sql.env;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.util.CacheUtil;
import jp.dodododo.sql.util.CloseableUtil;
import jp.dodododo.sql.util.InputStreamReaderUtil;
import jp.dodododo.sql.util.ReaderUtil;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

public class SqlResourceLoader {
    protected static final Map<String, String> SQL_CACHE = CacheUtil.cacheMap();

    protected SqlConfig config;

    public SqlResourceLoader(SqlConfig config) {
        this.config = config;
    }

    public String getSql(String sql, Dialect dialect) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return getSql(sql, dialect, cl);
    }

    public String getSql(String sql, Dialect dialect, ClassLoader loader) {
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

}
