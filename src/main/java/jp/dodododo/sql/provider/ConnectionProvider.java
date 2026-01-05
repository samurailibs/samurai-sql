package jp.dodododo.sql.provider;

import jp.dodododo.sql.empty_impl.DataSourceImpl;
import jp.dodododo.sql.util.DataSourceUtil;
import jp.dodododo.sql.util.JndiUtil;
import jp.dodododo.sql.wrapper.ConnectionWrapper;

import javax.sql.DataSource;
import java.sql.Connection;

public class ConnectionProvider {
    protected DataSource dataSource;

    public  ConnectionProvider(String jndiName) {
        this.dataSource= JndiUtil.lookup(DataSource.class, jndiName);
    }

    public ConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ConnectionProvider(Connection connection){
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

    public Connection connection() {
        return DataSourceUtil.getConnection(dataSource);
    }

    public DataSource dataSource() {
        return dataSource;
    }
}
