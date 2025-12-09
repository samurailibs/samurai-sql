package jp.dodododo.sql.dialect;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import jp.dodododo.sql.annotation.Internal;
import jp.dodododo.sql.metadata.ColumnMetaData;
import jp.dodododo.sql.metadata.TableMetaData;
import jp.dodododo.sql.object.PropertyDesc;
import jp.dodododo.sql.paging.LimitOffset;

// 方言
@Internal
public interface Dialect {
	String limitOffsetSql(String originalSQL, LimitOffset limitOffset) throws UnsupportedOperationException;

	String sequenceNextValSql(String sequenceName);

	String identitySelectSql();

	boolean isPrepareIdentitySelectSql();

	String getSuffix();

	ResultSet resultSet(ResultSet rs);

	PreparedStatement preparedStatement(PreparedStatement ps);

	String toDateString(Date date);

	String toTimestampString(Date date);

	BigDecimal getBigDecimal(ResultSet rs, int columnIndex) throws SQLException;

	void setBinaryStream(PreparedStatement ps, int index, InputStream inputStream) throws SQLException;

	String getVersion();

	void setId(Object entity, PropertyDesc propertyDesc, Object idValue);

	void bugfix(ColumnMetaData columnMetaData);

	boolean isSupportMultiRowInsert();

	String getReplaceCommand();

	String getReplaceOption();

	String getSchema(Connection connection);

	String getSchema(TableMetaData metaData);

	default TableNameResolver getTableNameResolver() {
		return TableNameResolver.NULL_RESOLVER;
	}

	void setTableNameResolver(TableNameResolver tableNameResolver);
}
