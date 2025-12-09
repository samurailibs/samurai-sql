package jp.dodododo.sql.sql.node;

import jp.dodododo.sql.context.CommandContext;

public class SqlNode extends AbstractNode {

	private String sql;

	public SqlNode(String sql) {
		this.sql = sql;
	}

    @Override
	public void accept(CommandContext ctx) {
		ctx.addSql(sql);
	}

}
