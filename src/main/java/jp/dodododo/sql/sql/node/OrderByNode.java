package jp.dodododo.sql.sql.node;

import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.util.EmptyUtil;
import jp.dodododo.sql.util.OgnlUtil;


/**
 * @author Satoshi Kimura
 */
public class OrderByNode extends AbstractNode {
	private String expression;

	public OrderByNode(String expression) {
		this.expression = expression;
	}

    @Override
	public void accept(CommandContext ctx) {
		Object object = OgnlUtil.parseExpression(expression);
		Object value = OgnlUtil.getValue(object, ctx);
		if (value != null) {
			String sql = value.toString().trim();
			if (EmptyUtil.isEmpty(sql) == false) {
				ctx.addSql("ORDER BY " + sql);
			}
		}
	}
}
