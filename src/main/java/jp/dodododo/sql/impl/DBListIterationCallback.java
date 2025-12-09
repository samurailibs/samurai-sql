package jp.dodododo.sql.impl;

import java.util.List;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.util.DbArrayList;

public class DBListIterationCallback<ROW> implements IterationCallback<ROW> {
	protected List<ROW> list = new DbArrayList<>();

    @Override
	public void iterate(ROW object) {
		list.add(object);
	}

    @Override
	public List<ROW> getResult() {
		return list;
	}
}
