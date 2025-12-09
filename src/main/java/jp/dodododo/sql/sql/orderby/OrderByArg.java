package jp.dodododo.sql.sql.orderby;

public class OrderByArg {
	private String columnName;

	private SortType sortType;

	public OrderByArg(String columnName) {
		this(columnName, null);
	}

	public OrderByArg(String columnName, SortType sortType) {
		if (columnName == null) {
			throw new NullPointerException("columnName is null");
		}
		this.columnName = columnName;
		this.sortType = sortType;
	}

	public String getColumnName() {
		return columnName;
	}

	public SortType getSortType() {
		return sortType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(columnName);
		if (sortType != null) {
			builder.append(" ");
			builder.append(sortType);
		}
		return builder.toString();
	}
}
