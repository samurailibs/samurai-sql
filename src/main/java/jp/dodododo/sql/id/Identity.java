package jp.dodododo.sql.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.impl.RdbDao;
import jp.dodododo.sql.impl.SamuraiSqlClientImpl;

public enum Identity implements IdGenerator {
	IDENTITY;

    @Override
	public Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName) {
		String sql = dialect.identitySelectSql();
		SamuraiSqlClient client = new SamuraiSqlClientImpl(connection);
		Optional<Map<String, Object>> result = client.selectOneMap(sql);
		Collection<Object> values = result.get().values();
		return values.iterator().next();
	}

    @Override
	public boolean generateBeforeInsert(Dialect dialect) {
		return dialect.isPrepareIdentitySelectSql();
	}

}
