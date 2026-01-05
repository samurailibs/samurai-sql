package jp.dodododo.sql.metadata;

import jp.dodododo.sql.provider.ConnectionProvider;
import jp.dodododo.sql.util.ConnectionUtil;

import java.sql.Connection;

public class MetaDataService {

    protected ConnectionProvider connectionProvider;

    public MetaDataService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public TableMetaData getTableMetaData(String tableName) {
        Connection connection = null;
        try {
            connection = connectionProvider.connection();
            return TableMetaData.getTableMetaData(connection, tableName);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public ColumnMetaData getColumnMetaData(TableMetaData tableMetaData, String propertyName) {
        return tableMetaData.getColumnMetaData(propertyName);
    }

}
