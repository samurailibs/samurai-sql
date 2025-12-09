package jp.dodododo.sql.dialect.sqlite;

import java.sql.Types;

import jp.dodododo.sql.metadata.ColumnMetaData;

public class SQLite3_7_2 extends SQLite {

	@Override
	public void bugfix(ColumnMetaData columnMetaData) {
		if ("REAL".equals(columnMetaData.getTypeName()) && (//
				columnMetaData.getDataType() == Types.VARCHAR || //
						columnMetaData.getDataType() == Types.CHAR || //
						columnMetaData.getDataType() == Types.LONGNVARCHAR || //
						columnMetaData.getDataType() == Types.LONGVARCHAR || //
						columnMetaData.getDataType() == Types.NCHAR || columnMetaData.getDataType() == Types.NVARCHAR)) {
			columnMetaData.setDataType(Types.DECIMAL);
		}
	}
}
