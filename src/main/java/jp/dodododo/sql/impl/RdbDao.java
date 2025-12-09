package jp.dodododo.sql.impl;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.ExtendedExecuteUpdateDao;

import javax.sql.DataSource;
import java.sql.Connection;

public class RdbDao extends SamuraiSqlClientImpl implements Dao, ExtendedExecuteUpdateDao {

    public RdbDao() {
        super();
    }

    public RdbDao(Object obj) {
        super(obj);
    }

    public RdbDao(String jndiName) {
        super(jndiName);
    }

    public RdbDao(Connection connection) {
        super(connection);
    }

    public RdbDao(DataSource dataSource) {
        super(dataSource);
    }
}
