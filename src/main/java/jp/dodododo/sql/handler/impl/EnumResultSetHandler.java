package jp.dodododo.sql.handler.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.util.EnumConverter;

/**
 *
 * @author Satoshi Kimura
 */
public class EnumResultSetHandler<ENUM extends Enum<?>> extends AbstractResultSetHandler<ENUM> {

	protected Dialect dialect;

	protected Class<ENUM> type;

	public EnumResultSetHandler(IterationCallback<ENUM> callback, Class<ENUM> type, Dialect dialect) {
		super(callback);
		this.dialect = dialect;
		this.type = type;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected ENUM createRow(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {
		Object val = rs.getObject(1);
		return (ENUM) EnumConverter.convertToEnum(val, type);
	}
}
