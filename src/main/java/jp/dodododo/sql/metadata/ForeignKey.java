package jp.dodododo.sql.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class ForeignKey implements Iterable<ForeignKeyColumn> {

	protected List<ForeignKeyColumn> columns = new ArrayList<>();

	public void add(String table, String column, String referenceTable, String referenceColumn) {
		columns.add(new ForeignKeyColumn(table, column, referenceTable, referenceColumn));
	}

	public ForeignKeyColumn get(int paramInt) {
		return columns.get(paramInt);
	}

	public boolean isEmpty() {
		return columns.isEmpty();
	}

    @Override
	public Iterator<ForeignKeyColumn> iterator() {
		return columns.iterator();
	}

	public int size() {
		return columns.size();
	}

	@Override
	public String toString() {
		return "ForeignKey [columns=" + columns + "]";
	}

}
