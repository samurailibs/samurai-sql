package jp.dodododo.sql.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlowQuery {
	private static final Logger logger = LoggerFactory.getLogger(SlowQuery.class);

	public static void warn(double time, String slowQuerySql) {
		String caller = "";
		try {
			throw new Exception();
		} catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement element : stackTrace) {
				String className = element.getClassName();
				String methodName = element.getMethodName();
				int lineNumber = element.getLineNumber();
				if (className.startsWith("jp.dodododo.sql") == false) {
					caller = className + "#" + methodName + "()" + " line:" + lineNumber;
					break;
				}
			}
		}
		logger.warn("QueryTime: " + time + "secs. SQL: " + slowQuerySql + ". Caller: " + caller);
	}
}
