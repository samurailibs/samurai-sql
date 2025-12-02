package jp.dodododo.dao.id;

import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.log.SqlLogRegistry;
import jp.dodododo.dao.util.PreparedStatementUtil;
import jp.dodododo.dao.util.ResultSetUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static jp.dodododo.dao.util.ResultSetUtil.getObject;

public enum GeneratedValue implements IdGenerator {
	GENERATED_VALUE;

    @Override
	public Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName) {
		ResultSet generatedKeys = PreparedStatementUtil.getGeneratedKeys(ps, SqlLogRegistry.getInstance());
		if(ResultSetUtil.next(generatedKeys)){
			return getObject(generatedKeys, 1);
		}
		return null;
	}

    @Override
	public boolean generateBeforeInsert(Dialect dialect) {
		return false;
	}

}
