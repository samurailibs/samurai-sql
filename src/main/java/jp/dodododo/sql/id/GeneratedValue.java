package jp.dodododo.sql.id;

import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.log.SqlLogRegistry;
import jp.dodododo.sql.util.PreparedStatementUtil;
import jp.dodododo.sql.util.ResultSetUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static jp.dodododo.sql.util.ResultSetUtil.getObject;

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
