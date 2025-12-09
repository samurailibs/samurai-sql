package jp.dodododo.dao;

import jp.dodododo.dao.config.DaoConfig;
import jp.dodododo.dao.log.SqlLogRegistry;

/**
 *
 * @author Satoshi Kimura
 */
public interface Dao extends SelectDao, ExecuteUpdateDao, SamuraiSqlClient {
	SqlLogRegistry getSqlLogRegistry();

	void setQueryTimeout(int seconds);

	DaoConfig getConfig();
}
