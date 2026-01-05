package jp.dodododo.sql.executor.crud;

import jp.dodododo.sql.value.ParameterValue;

import java.util.List;
import java.util.Map;

public class CrudSqlBuilder {

    protected String whereColumnPrefix;

    public  String createInsertSql(String tableName, List<String> updateColumnNames, Map<String, ParameterValue> values) {
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

    protected String getDummyValString(String columnName, Map<String, ParameterValue> values) {
        // TODO Return a reasonable string inferred from the column name.
        return "0";
    }

    public void setWhereColumnPrefix(String whereColumnPrefix) {
        this.whereColumnPrefix = whereColumnPrefix;
    }

    public String whereColumnPrefix() {
        return whereColumnPrefix;
    }
}
