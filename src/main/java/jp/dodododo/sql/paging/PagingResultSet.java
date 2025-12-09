package jp.dodododo.sql.paging;

import java.sql.ResultSet;
import java.sql.SQLException;

import jp.dodododo.sql.error.SQLError;
import jp.dodododo.sql.wrapper.ResultSetWrapper;

public class PagingResultSet extends ResultSetWrapper {

	private LimitOffset limitOffset;

	private long count = 0;

	public PagingResultSet(ResultSet rs, LimitOffset limitOffset, String sql) {
		super(rs, sql);
		this.limitOffset = limitOffset;
		for (long i = 0; i < limitOffset.getOffset(); i++) {
			try {
				rs.next();
			} catch (SQLException e) {
				throw new SQLError(e);
			}
		}
	}

	@Override
	public boolean next() throws SQLException {
		if (limitOffset.getLimit() <= count) {
			return false;
		} else {
			count++;
		}
		return super.next();
	}
}
