package jp.dodododo.sql.handler.impl;

import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.IterationCallback;

public class HashMapResultSetHandler extends jp.dodododo.sql.handler.impl.MapResultSetHandler {

	protected HashMapResultSetHandler() {
	}

	public HashMapResultSetHandler(IterationCallback<Map<String, Object>> callback) {
		super(callback);
	}

	public HashMapResultSetHandler(IterationCallback<Map<String, Object>> callback, Map<String, Object> arg) {
		super(callback, arg);
	}

	@Override
	protected Map<String, Object> newInstance() {
		Map<String, Object> ret = new HashMap<>();
		ret.putAll(arg);
		return ret;
	}
}
