package jp.dodododo.sql.sql.node;

import java.util.Collection;

import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.util.CollectionUtil;
import jp.dodododo.sql.util.OgnlUtil;
import jp.dodododo.sql.util.TypesUtil;

/**
 * @author Satoshi Kimura
 */
public class InNode extends AbstractNode {
	private String expression;

	public InNode(String expression) {
		this.expression = expression;
	}

    @Override
	@SuppressWarnings({ "rawtypes" })
	public void accept(CommandContext ctx) {
		Object object = OgnlUtil.parseExpression(expression);
		Object value = OgnlUtil.getValue(object, ctx);
		if (value != null) {
			ctx.addSql("(");
			Object[] values;
			if (value instanceof Collection) {
				values =  CollectionUtil.toArray((Collection) value);
			} else if (value instanceof Object[]) {
				values = (Object[]) value;
			} else {
				values = new Object[]{ value};
			}
			for (int i = 0; i < values.length; i++) {
				Object val = values[i];
				ctx.addSql("?", val, TypesUtil.getSQLType(val).getType());
				if (i < values.length - 1) {
					ctx.addSql(", ");
				}
			}
			ctx.addSql(")");
		} else {
			throw new NullPointerException("'" + expression + "'" + " is null.");
		}
	}
}
