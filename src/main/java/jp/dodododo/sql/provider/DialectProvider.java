package jp.dodododo.sql.provider;

import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.DialectManager;

import javax.sql.DataSource;

public class DialectProvider {

    protected Dialect dialect;

    public DialectProvider(DataSource dataSource) {
        this.dialect = DialectManager.getDialect(dataSource);
    }

    public Dialect dialect() {
        return dialect;
    }
}
