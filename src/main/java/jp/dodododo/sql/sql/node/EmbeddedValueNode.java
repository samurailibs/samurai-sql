package jp.dodododo.sql.sql.node;

import java.util.HashMap;
import java.util.Map;

import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.util.StringUtil;

public class EmbeddedValueNode extends AbstractNode {

	private String expression;

	private String[] names;

	public EmbeddedValueNode(String expression) {
		this.expression = expression;
		names = StringUtil.split(expression, "\\.");
	}

	public String getExpression() {
		return expression;
	}

    @Override
	public void accept(CommandContext ctx) {
		String name = names[0];
		Object value = ctx.getArg(name);
		if (value == null) {
			return;
		}
		if (1 < names.length) {
			Map<String, Object> root = new HashMap<>();
			root.put(name, value);
			Dialect dialect = ctx.getDialect();
			value = getValue(expression, names, root, dialect);
		}
		if (value == null) {
			return;
		}
		String sql = value.toString();
		validateSql(sql);
		if (value != null) {
			ctx.addSql(sql);
		}
	}

	private void validateSql(String sql) {
		if (sql.contains("?") == true && isEnclosedBySingleQuot(sql, "?") == false) {
			throw new IllegalArgumentException(Message.getMessage("00009"));
		}
		if (sql.contains(";") == true && isEnclosedBySingleQuot(sql, ";") == false) {
			throw new IllegalArgumentException(Message.getMessage("00010"));
		}
	}

}
