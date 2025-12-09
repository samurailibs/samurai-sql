package jp.dodododo.sql.sql.node;

import jp.dodododo.sql.context.CommandContext;

public class ElseNode extends ContainerNode {
	@Override
	public void accept(CommandContext ctx) {
		super.accept(ctx);
		ctx.setEnabled(true);
	}
}