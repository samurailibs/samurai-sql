package jp.dodododo.sql.handler.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jp.dodododo.sql.IterationCallback;
import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.dialect.Dialect;

/**
 *
 * @author Satoshi Kimura
 */
public class BigDecimalResultSetHandler extends AbstractResultSetHandler<BigDecimal> {

	protected Dialect dialect;

	public BigDecimalResultSetHandler(IterationCallback<BigDecimal> callback, Dialect dialect) {
		super(callback);
		this.dialect = dialect;
	}

	@Override
	protected BigDecimal createRow(ResultSet rs, List<ResultSetColumn> resultSetColumnList) throws SQLException {
		return dialect.getBigDecimal(rs, 1);
	}
}
