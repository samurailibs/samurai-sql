package jp.dodododo.sql.columns;

import java.io.Serializable;

public class ResultSetColumn implements Serializable {
	private static final long serialVersionUID = 1226374843162357017L;

	private int dataType;

	private String name;

	private int displaySize;

	public ResultSetColumn(String name, int dataType, int displaySize) {
		this.name = name;
		this.dataType = dataType;
		this.displaySize = displaySize;
	}

	public int getDataType() {
		return dataType;
	}

	public String getName() {
		return name;
	}

	public int getDisplaySize() {
		return displaySize;
	}

}
