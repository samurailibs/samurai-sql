package jp.dodododo.dao.id;

import java.sql.Connection;
import java.sql.PreparedStatement;

import jp.dodododo.dao.dialect.Dialect;

public interface IdGenerator {
	boolean generateBeforeInsert(Dialect dialect);

	Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName);

}
