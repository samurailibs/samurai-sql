package jp.dodododo.sql.bootstrap;

import jp.dodododo.sql.env.SqlEnvironment;
import jp.dodododo.sql.provider.ConnectionProvider;
import jp.dodododo.sql.provider.DialectProvider;

import javax.sql.DataSource;
import java.sql.Connection;

public class ClientBootstrap {

    protected ConnectionProvider connectionProvider;

    protected DialectProvider dialectProvider;

    protected SqlEnvironment sqlEnvironment;

    public ClientBootstrap(SqlEnvironment sqlEnvironment ) {
        this.sqlEnvironment = sqlEnvironment;
    }
    public void bootstrap(String jndiName) {
        init(new ConnectionProvider(jndiName));
    }

    public void bootstrap(Connection connection) {
        init(new ConnectionProvider(connection));
    }

    public void bootstrap(DataSource dataSource) {
        init(new ConnectionProvider(dataSource));
    }

    protected void init(ConnectionProvider provider) {
        this.connectionProvider = provider;
        this.dialectProvider = new DialectProvider(provider.dataSource());
        sqlEnvironment.init(provider.connection());
    }

    public ConnectionProvider connectionProvider() {
        return connectionProvider;
    }

    public DialectProvider dialectProvider() {
        return dialectProvider;
    }
}
