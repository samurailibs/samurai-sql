package jp.dodododo.sql.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import jp.dodododo.sql.dialect.Dialect;

public enum RandomUUID implements IdGenerator {
	RANDOM_UUID;

    @Override
	public Object generate(Connection connection, PreparedStatement ps, Dialect dialect, String sequenceName) {
		return UUID.randomUUID();
	}

    @Override
	public boolean generateBeforeInsert(Dialect dialect) {
		return true;
	}

}
