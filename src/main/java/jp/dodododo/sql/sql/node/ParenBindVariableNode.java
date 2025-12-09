package jp.dodododo.sql.sql.node;

import java.util.List;

import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.util.OgnlUtil;

public class ParenBindVariableNode extends AbstractNode {

	private String expression;

	private Object parsedExpression;

	public ParenBindVariableNode(String expression) {
		this.expression = expression;
		this.parsedExpression = OgnlUtil.parseExpression(expression);
	}

	public String getExpression() {
		return expression;
	}

    @Override
	public void accept(CommandContext ctx) {
		Object var = OgnlUtil.getValue(parsedExpression, ctx);
		if (var == null) {
			return;
		} else if (var instanceof List<?>) {
			bindArray(ctx, ((List<?>) var).toArray());
		} else {
			Class<?> clazz = var.getClass();
			if (clazz.isArray()) {
				bindArray(ctx, var);
			} else {
				int type = getType(clazz);
				ctx.addSql("?", var, type);
			}
		}

	}

}