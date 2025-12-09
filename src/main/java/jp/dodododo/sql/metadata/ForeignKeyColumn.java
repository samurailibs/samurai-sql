package jp.dodododo.sql.metadata;

import jp.dodododo.sql.annotation.Internal;

@Internal
public class ForeignKeyColumn {

	protected String table;
	protected String column;
	protected Reference reference;

	public ForeignKeyColumn(String table, String column, String referenceTable, String referenceColumn) {
		this(table, column, new Reference(referenceTable, referenceColumn));
	}

	public ForeignKeyColumn(String table, String column, Reference reference) {
		this.table = table;
		this.column = column;
		this.reference = reference;
	}

	public String getTableName() {
		return table;
	}

	public String getColumnName() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	public String toString() {
		return "Key [column=" + column + ", reference=" + reference + "]";
	}

	public static class Reference {
		protected String table;
		protected String column;

		public Reference(String table, String column) {
			this.table = table;
			this.column = column;
		}

		public String getTable() {
			return table;
		}

		public String getColumn() {
			return column;
		}

		@Override
		public String toString() {
			return "Reference [table=" + table + ", column=" + column + "]";
		}

	}
}
