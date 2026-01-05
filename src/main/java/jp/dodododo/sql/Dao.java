package jp.dodododo.sql;

import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.log.SqlLogRegistry;

/**
 *
 * @author Satoshi Kimura
 */
@Deprecated
public interface Dao extends SelectDao, ExecuteUpdateDao, SamuraiSqlClient {
	SqlLogRegistry getSqlLogRegistry();
}
