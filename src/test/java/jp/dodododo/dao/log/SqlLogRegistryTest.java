package jp.dodododo.dao.log;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import jp.dodododo.dao.object.ObjectDesc;
import jp.dodododo.dao.object.PropertyDesc;
import org.junit.jupiter.api.Test;

public class SqlLogRegistryTest {

	@Test
	public void dump() throws Exception {
		SqlLogRegistry registry = SqlLogRegistry.getInstance();
		for (int i = 0; i < 50; i++) {
			registry.add(newSqlLog());
		}

		File dumpFile = registry.dump();

		if (dumpFile != null) {
			dumpFile.delete();
		}
	}

	private SqlLog newSqlLog() throws InterruptedException {
		String name = "threadName" + (new Random().nextInt() % 10);
		SqlLog log = new SqlLog("select * from " + name, "select * from " + name, null, null, null);
		ObjectDesc<SqlLog> objectDesc = new ObjectDesc(SqlLog.class, false);
		PropertyDesc propertyDesc = objectDesc.getPropertyDesc("executeThread");
		propertyDesc.setValue(log, new Thread(name), true);
		TimeUnit.MILLISECONDS.sleep(123);
		return log;
	}
}
