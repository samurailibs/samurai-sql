package jp.dodododo.sql.log;

import java.time.LocalDateTime;
import java.util.List;

public class SqlLog {

	private String sql;

	private String completeSql;

	private List<Object> bindArgs;

	private List<Integer> bindArgTypes;

	private ExecuteType executeType;

	private Thread executeThread;

	private LocalDateTime time;

	/**
	 * インスタンスを構築します。
	 *
	 * @param sql
	 *            未加工のSQL
	 * @param completeSql
	 *            完全なSQL
	 * @param bindArgs
	 *            SQLにバインドされる値の配列
	 * @param bindArgTypes
	 *            SQLにバインドされる値の型の配列
	 * @param executeType
	 *            SQLの実行タイプ
	 */
	public SqlLog(String sql, String completeSql, List<Object> bindArgs, List<Integer> bindArgTypes, ExecuteType executeType) {
		this.sql = sql;
		this.completeSql = completeSql;
		this.bindArgs = bindArgs;
		this.bindArgTypes = bindArgTypes;
		this.executeType = executeType;
		this.executeThread = Thread.currentThread();
		this.time = LocalDateTime.now();
	}

	public List<Object> getBindArgs() {
		return bindArgs;
	}

	public List<Integer> getBindArgTypes() {
		return bindArgTypes;
	}

	public String getCompleteSql() {
		return completeSql;
	}

	public String getRawSql() {
		return sql;
	}

	public ExecuteType getSqlType() {
		return executeType;
	}

	public Thread getExecuteThread() {
		return executeThread;
	}

	public String getThreadName() {
		return executeThread.getName();
	}

	public LocalDateTime getTime() {
		return time;
	}

	@Override
	public String toString() {
		return sql;
	}
}
