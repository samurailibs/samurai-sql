package jp.dodododo.sql.sql.node;

import jp.dodododo.sql.context.CommandContext;

public class ContainerNode extends AbstractNode {
    @Override
	public void accept(CommandContext ctx) {
		getChildren().forEach(child -> child.accept(ctx));
	}
}
