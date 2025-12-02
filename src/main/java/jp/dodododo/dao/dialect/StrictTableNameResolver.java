package jp.dodododo.dao.dialect;

import jp.dodododo.dao.util.CaseInsensitiveMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StrictTableNameResolver implements TableNameResolver {
    public static TableNameResolver INSTANCE = new StrictTableNameResolver();
    @Override
    public String resolve(Connection connection, String tableName) throws SQLException {
        return resolveActualTableName(connection).get(tableName);
    }

    /**
     * information_schema から実際のテーブル名を取得する
     */
    private CaseInsensitiveMap<String> resolveActualTableName(Connection connection) throws SQLException {
        String schema = connection.getSchema();
        CaseInsensitiveMap<String> tableNames = new CaseInsensitiveMap<>();
        String sql =
                "SELECT table_name " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = ? ";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tableName = rs.getString("table_name");
                    tableNames.put(tableName, tableName);   // ← これが正式名称（dept / Dept / DEPT 等）
                }
            }
        }
        return tableNames;
    }

}
