package jp.dodododo.sql.exception;

import jp.dodododo.sql.message.Message;

public class IllegalBoolExpressionRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 3699428777413602275L;

	private String expression;

	public IllegalBoolExpressionRuntimeException(String expression) {
		super(Message.getMessage("00020", expression));
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}
}
