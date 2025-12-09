package jp.dodododo.sql.util;

import java.sql.SQLException;
import java.sql.Wrapper;

import jp.dodododo.sql.error.SQLError;

public class WrapperUtil {

	public static <T> T unwrap(Object target, Class<T> iface) {
		try {
			if (target instanceof Wrapper) {
				Wrapper wrapper = (Wrapper) target;
				if (wrapper.isWrapperFor(iface) == true) {
					try {
						return wrapper.unwrap(iface);
					} catch (SQLException e) {
						throw new SQLError(e);
					}
				}
			}
		} catch (Throwable ignore) { // NoSuchMethodError
		}
		if (iface.isAssignableFrom(target.getClass()) == true) {
			@SuppressWarnings("unchecked")
			T ret = (T) target;
			return ret;
		}
		return null;
	}
}
