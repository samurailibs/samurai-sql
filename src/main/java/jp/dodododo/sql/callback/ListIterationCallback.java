package jp.dodododo.sql.callback;

import java.util.ArrayList;
import java.util.List;

import jp.dodododo.sql.IterationCallback;

public class ListIterationCallback<ROW> implements IterationCallback<ROW> {
	protected List<ROW> list = new ArrayList<>(1024);

    @Override
	public void iterate(ROW object) {
		list.add(object);
	}

    @Override
	public List<ROW> getResult() {
		return list;
	}
}
