package jp.dodododo.sql.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import jp.dodododo.sql.config.SqlConfig;
import jp.dodododo.sql.util.EmptyUtil;
import jp.dodododo.sql.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLogRegistry {

	private static final Logger logger = LoggerFactory.getLogger(SlowQuery.class);

	protected SqlConfig config;

	private static final SqlLogRegistry INSTANCE = new SqlLogRegistry();

	private ThreadLocal<LinkedList<SqlLog>> threadList = ThreadLocal.withInitial(() -> new LinkedList<>());

	private Map<Thread, LinkedList<SqlLog>> allLogs = new HashMap<>();

	public static SqlLogRegistry getInstance() {
		return INSTANCE;
	}

	/**
	 * デフォルトの上限サイズを使用してインスタンスを構築します。
	 */
	public SqlLogRegistry() {
		this(SqlConfig.getDefaultConfig());
	}

	/**
	 * DaoConfigを指定してインスタンスを構築します。
	 *
	 * @param config config
	 */
	public SqlLogRegistry(SqlConfig config) {
		setConfig(config);
	}

	public void setConfig(SqlConfig config) {
		this.config = config;
	}

	public SqlConfig getConfig() {
		return config;
	}

	public int getLimitSize() {
		return config.getLogMaxSize();
	}

	public int getSize() {
		return getSqlLogList().size();
	}

	public boolean isEmpty() {
		return EmptyUtil.isEmpty(getSqlLogList());
	}

	public SqlLog get(int index) {
		return getSqlLogList().get(index);
	}

	public SqlLog getLast() {
		return isEmpty() ? null : getSqlLogList().getLast();
	}

	public void add(SqlLog sqlLog) {
		LinkedList<SqlLog> list = getSqlLogList();
		list.add(sqlLog);
		allLogs.put(sqlLog.getExecuteThread(), list);
		if (getLimitSize() < list.size()) {
			list.removeFirst();
		}
	}

	public void clear() {
		getSqlLogList().clear();
		ThreadLocalUtil.remove(threadList);
	}

	private LinkedList<SqlLog> getSqlLogList() {
		LinkedList<SqlLog> ret = threadList.get();
		if (ret == null) {
			ret = new LinkedList<>();
			threadList.set(ret);
		}
		return ret;
	}

	public List<String> getSqls(ExecuteType executeType) {
		LinkedList<SqlLog> sqlLogList = getSqlLogList();
		List<String> ret = new ArrayList<>(sqlLogList.size());
		for (SqlLog sqlLog : sqlLogList) {
			if (ExecuteType.ALL.equals(executeType) || executeType.equals(sqlLog.getSqlType())) {
				ret.add(sqlLog.getCompleteSql());
			}
		}
		return ret;
	}

	public List<String> getAllSql() {
		LinkedList<SqlLog> sqlLogList = getSqlLogList();
		List<String> ret = new ArrayList<>(sqlLogList.size());
		for (SqlLog sqlLog : sqlLogList) {
			ret.add(sqlLog.getCompleteSql());
		}
		return ret;
	}

	public List<SqlLog> getAllThreadSqls() {
		List<SqlLog> ret = new ArrayList<>(512);
		for (Map.Entry<Thread, LinkedList<SqlLog>> entry : allLogs.entrySet()) {
			ret.addAll(entry.getValue());
		}
		return ret;
	}

	public File dump() {
		File dumpFile;
		try {
			dumpFile = File.createTempFile("samurai-dao-sql-", ".dump", null);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		try (OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(dumpFile), 16384), "UTF8")) {
			List<SqlLog> allSqlList = getAllThreadSqls();
			allSqlList.stream().sorted((log1, log2) -> {
				int threadName = log1.getThreadName().compareTo(log2.getThreadName());
				if (threadName != 0) {
					return threadName;
				}
				int time = log1.getTime().compareTo(log2.getTime());
				return time;
			}).forEach(log -> {
				try {
					writer.write("[");
					writer.write(log.getThreadName());
					writer.write("]");
					writer.write("[");
					writer.write(log.getTime().toString());
					writer.write("]");
					writer.write("[");
					writer.write(log.getCompleteSql());
					writer.write("]");
					writer.write(System.lineSeparator());
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			});
			writer.flush();
			logger.info("Dump all SQL.[" + dumpFile.getAbsolutePath() + "]");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return dumpFile;
	}
}
