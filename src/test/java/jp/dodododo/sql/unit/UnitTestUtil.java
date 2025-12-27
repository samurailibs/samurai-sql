package jp.dodododo.sql.unit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.impl.RdbDao;
import jp.dodododo.sql.impl.SamuraiSqlClientImpl;
import jp.dodododo.sql.object.PropertyDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTestUtil {

	public static Dao newTestClient() {
		Class<?>[] interfaces = new Class[] { Dao.class };
		InvocationHandler handler = new Handler(new RdbDao());
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Dao dao = (Dao) Proxy.newProxyInstance(loader, interfaces, handler);
		return dao;
	}

	public static Dao newTestClient(DataSource dataSource) {
		Class<?>[] interfaces = new Class[] { Dao.class };
		InvocationHandler handler = new Handler(new RdbDao(dataSource));
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Dao dao = (Dao) Proxy.newProxyInstance(loader, interfaces, handler);
		return dao;
	}

	public static SamuraiSqlClient newTestClient(Connection connection) {
		Class<?>[] interfaces = new Class[] { SamuraiSqlClient.class };
		InvocationHandler handler = new Handler(new SamuraiSqlClientImpl(connection));
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		SamuraiSqlClient client = (SamuraiSqlClient) Proxy.newProxyInstance(loader, interfaces, handler);
		return client;
	}

	public static class Handler implements InvocationHandler {

		private static final Logger logger = LoggerFactory.getLogger(Handler.class);

		protected SamuraiSqlClient client;

		public Handler() {
		}

		public Handler(SamuraiSqlClient client) {
			this.client = client;
		}

		@Override
		public Object invoke(Object target, Method method, Object[] args) throws Throwable {
			Method m = SamuraiSqlClient.class.getMethod(method.getName(), method.getParameterTypes());
			long start = System.currentTimeMillis();
			try {
				logger.debug("START : " + m.getName());
				return m.invoke(client, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			} finally {
				long end = System.currentTimeMillis();
				logger.debug("END : " + m.getName());
				logger.debug("TIME : " + m.getName() + " : " + (end - start));
				Field field = PropertyDesc.class.getDeclaredField("getValueCache");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				ThreadLocal<Map<PropertyDesc, Map<Object, Object>>> threadLocal = (ThreadLocal<Map<PropertyDesc, Map<Object, Object>>>) field
						.get(null);
				Map<PropertyDesc, Map<Object, Object>> cache = threadLocal.get();
				if (cache.size() != 0) {
					throw new RuntimeException("MemoryLeak!!");
				}
			}
		}

	}
}
