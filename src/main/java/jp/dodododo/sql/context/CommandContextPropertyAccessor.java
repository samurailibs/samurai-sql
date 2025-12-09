package jp.dodododo.sql.context;

import java.util.Map;

import jp.dodododo.sql.annotation.Internal;

import ognl.ObjectPropertyAccessor;

/**
 *
 * @author Satoshi Kimura
 */
@Internal
public class CommandContextPropertyAccessor extends ObjectPropertyAccessor {

	@Override
	public Object getProperty(@SuppressWarnings("rawtypes") Map context, Object target, Object name) {

		return getProperty(target, name);
	}

	private Object getProperty(Object target, Object name) {

		CommandContext ctx = (CommandContext) target;
		String argName = name.toString();
		return ctx.getArg(argName);
	}

}
