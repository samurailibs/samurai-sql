package jp.dodododo.sql.columns;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Satoshi Kimura
 */
public abstract class Columns {
	private List<String> columnList;

	public Columns(String... columns) {
		columnList = Arrays.asList(columns);
	}

	public List<String> getColumnList() {
		return columnList;
	}

}
