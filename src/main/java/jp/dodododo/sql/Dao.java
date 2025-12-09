package jp.dodododo.sql;

import jp.dodododo.sql.config.DaoConfig;
import jp.dodododo.sql.log.SqlLogRegistry;

/**
 *
 * @author Satoshi Kimura
 */
public interface Dao extends SelectDao, ExecuteUpdateDao, SamuraiSqlClient {
	SqlLogRegistry getSqlLogRegistry();

	void setQueryTimeout(int seconds);

	DaoConfig getConfig();
}
