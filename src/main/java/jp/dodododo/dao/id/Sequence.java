package jp.dodododo.dao.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.impl.RdbDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Sequence implements IdGenerator {
	SEQUENCE;

	private static final Logger logger = LoggerFactory.getLogger(Sequence.class);

	@Override
	public Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName) {
		String sql = dialect.sequenceNextValSql(sequenceName);
		Dao dao = new RdbDao(connection);
		Optional<Map<String, Object>> result = dao.selectOneMap(sql);
		Collection<Object> values = result.get().values();
		logger.debug("values : {}", values);
		return values.iterator().next();
	}

    @Override
	public boolean generateBeforeInsert(Dialect dialect) {
		return true;
	}

}
