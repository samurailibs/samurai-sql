package jp.dodododo.sql.env;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.log.ExecuteType;
import jp.dodododo.sql.log.SqlLog;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.types.SQLType;
import jp.dodododo.sql.util.StringUtil;
import jp.dodododo.sql.util.TypesUtil;
import jp.dodododo.sql.value.CandidateValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jp.dodododo.sql.types.SQLTypes.*;
import static jp.dodododo.sql.types.SQLTypes.BOOLEAN;

public class SqlLogger {
    protected static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    protected SqlLogRegistry sqlLogRegistry;

    protected SqlConfig config;

    public SqlLogger(SqlLogRegistry sqlLogRegistry, SqlConfig config) {
        this.sqlLogRegistry= sqlLogRegistry;
        this.config = config;
    }

    public void logSql(String sql, List<Object> args, List<Integer> types, Dialect dialect, ExecuteType sqlType) {
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
}
