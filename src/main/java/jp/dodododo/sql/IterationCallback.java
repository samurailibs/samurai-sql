package jp.dodododo.sql;

import java.util.List;

public interface IterationCallback<ROW> {
	void iterate(ROW row);

	List<ROW> getResult();
}
