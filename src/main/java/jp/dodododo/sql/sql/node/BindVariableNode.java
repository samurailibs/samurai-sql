package jp.dodododo.sql.sql.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.exception.ArgNotFoundException;
import jp.dodododo.sql.util.StringUtil;

/**
 *
 * @author Satoshi Kimura
 */
public class BindVariableNode extends AbstractNode {

	private String expression;

	private String[] names;

	public BindVariableNode(String expression) {
		this.expression = expression;
		initNames(expression);
	}

	private void initNames(String expression) {
		names = StringUtil.split(expression, "\\.");
	}

    @Override
	public void accept(CommandContext ctx) {
		String name = topObjectName();
		Object value;
		try {
			value = ctx.getArg(name);
		} catch (ArgNotFoundException e) {
			throw new ArgNotFoundException(name, getSql());
		}

		if (value != null && 1 < names.length) {
			Map<String, Object> root = new HashMap<>();
			root.put(name, value);
			Dialect dialect = ctx.getDialect();
			value = getValue(expression, names, root, dialect);
		}
		int type;
		if (expression.equals(name)) {
			type = ctx.getArgType(name);
		} else {
			type = getType(value);
		}
		if (value instanceof List<?>) {
			bindArray(ctx, ((List<?>) value).toArray());
		} else {
			if (value == null) {
				ctx.addSql("?", value, type);
				return;
			}
			Class<?> clazz = value.getClass();
			if (clazz.isArray()) {
				bindArray(ctx, value);
			} else {
				ctx.addSql("?", value, type);
			}
		}
	}

	private String topObjectName() {
		String firstName = names[0];
		String[] split = firstName.split("\\[");
		return split[0];
	}
}
