package jp.dodododo.dao.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.impl.RdbDao;

public enum Identity implements IdGenerator {
	IDENTITY;

    @Override
	public Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName) {
		String sql = dialect.identitySelectSql();
		Dao dao = new RdbDao(connection);
		Optional<Map<String, Object>> result = dao.selectOneMap(sql);
		Collection<Object> values = result.get().values();
		return values.iterator().next();
	}

    @Override
	public boolean generateBeforeInsert(Dialect dialect) {
		return dialect.isPrepareIdentitySelectSql();
	}

}
