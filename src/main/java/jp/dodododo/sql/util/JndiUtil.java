package jp.dodododo.sql.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jp.dodododo.sql.error.NamingError;

public class JndiUtil {

	@SuppressWarnings("unchecked")
	public static <T> T lookup(Class<T> type, String jndiName) {
		try {
			Context ctx = new InitialContext();
			return (T) ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new NamingError(e);
		}
	}

}
