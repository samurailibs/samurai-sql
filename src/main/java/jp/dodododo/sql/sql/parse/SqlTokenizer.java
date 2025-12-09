package jp.dodododo.sql.sql.parse;

import jp.dodododo.sql.exception.TokenNotClosedRuntimeException;

public class SqlTokenizer {
	public static final int SQL = 1;

	/**
	 * BEGIN, IF, END, ORDERBY
	 */
	public static final int COMMENT = 2;

	/**
	 * --ELSE
	 */
	public static final int ELSE = 3;

	/**
	 * ?
	 */
	public static final int BIND_VARIABLE = 4;

	/**
	 * --
	 */
	public static final int LINE_COMMENT = 5;

	public static final int EOF = 99;

	private String sql;

	private int position = 0;

	private String token;

	private int tokenType = SQL;

	private int nextTokenType = SQL;

	private int bindVariableNum = 0;

	public SqlTokenizer(String sql) {
		this.sql = sql;
	}

	public int getPosition() {
		return position;
	}

	public String getToken() {
		return token;
	}

	public String getBefore() {
		return sql.substring(0, position);
	}

	public String getAfter() {
		return sql.substring(position);
	}

	public int getTokenType() {
		return tokenType;
	}

	public int getNextTokenType() {
		return nextTokenType;
	}

	public int next() {
		if (position >= sql.length()) {
			token = null;
			tokenType = EOF;
			nextTokenType = EOF;
			return tokenType;
		}
		switch (nextTokenType) {
		case SQL:
			parseSql();
			break;
		case COMMENT:
			parseComment();
			break;
		case LINE_COMMENT:
			parseLineComment();
			break;
		case ELSE:
			parseElse();
			break;
		case BIND_VARIABLE:
			parseBindVariable();
			break;
		default:
			parseEof();
			break;
		}
		if (position < 0) {
			throw new RuntimeException();
		}
		return tokenType;
	}

	protected void parseSql() {
		int commentStartPos = sql.indexOf("/*", position);
		int commentStartPos2 = sql.indexOf("#*", position);
		if (0 < commentStartPos2 && commentStartPos2 < commentStartPos) {
			commentStartPos = commentStartPos2;
		}
		int lineCommentStartPos = sql.indexOf("--", position);
		int bindVariableStartPos = sql.indexOf("?", position);
		int elseCommentStartPos = -1;
		int elseCommentLength = -1;
		if (0 <= lineCommentStartPos) {
			int skipPos = skipWhitespace(lineCommentStartPos + 2);
			if (skipPos + 4 < sql.length() && "ELSE".equals(sql.substring(skipPos, skipPos + 4))) {
				elseCommentStartPos = lineCommentStartPos;
				elseCommentLength = skipPos + 4 - lineCommentStartPos;
			}
		}
		int nextStartPos = getNextStartPos(commentStartPos, elseCommentStartPos, bindVariableStartPos, lineCommentStartPos);
		if (nextStartPos < 0) {
			token = sql.substring(position);
			nextTokenType = EOF;
			position = sql.length();
			tokenType = SQL;
		} else {
			token = sql.substring(position, nextStartPos);
			tokenType = SQL;
			boolean needNext = nextStartPos == position;
			if (nextStartPos == commentStartPos) {
				nextTokenType = COMMENT;
				position = commentStartPos + 2;
			} else if (nextStartPos == elseCommentStartPos) {
				nextTokenType = ELSE;
				position = elseCommentStartPos + elseCommentLength;
			} else if (nextStartPos == lineCommentStartPos) {
				nextTokenType = LINE_COMMENT;
				position = lineCommentStartPos;
			} else if (nextStartPos == bindVariableStartPos) {
				nextTokenType = BIND_VARIABLE;
				position = bindVariableStartPos;
			}
			if (needNext) {
				next();
			}
		}
	}

	protected int getNextStartPos(int commentStartPos, int elseCommentStartPos, int bindVariableStartPos, int lineCommentStartPos) {

		int nextStartPos = -1;
		if (0 <= commentStartPos) {
			nextStartPos = commentStartPos;
		}
		if (0 <= lineCommentStartPos && (nextStartPos < 0 || lineCommentStartPos < nextStartPos)) {
			nextStartPos = lineCommentStartPos;
			return nextStartPos;
		}
		if (0 <= elseCommentStartPos && (nextStartPos < 0 || elseCommentStartPos < nextStartPos)) {
			nextStartPos = elseCommentStartPos;
		}
		if (0 <= bindVariableStartPos && (nextStartPos < 0 || bindVariableStartPos < nextStartPos)) {
			nextStartPos = bindVariableStartPos;
		}
		return nextStartPos;
	}

	protected String nextBindVariableName() {
		return "$" + ++bindVariableNum;
	}

	protected void parseComment() {
		int commentEndPos = sql.indexOf("*/", position);
		int commentEndPos2 = sql.indexOf("*#", position);
		if (0 < commentEndPos2 && commentEndPos2 < commentEndPos) {
			commentEndPos = commentEndPos2;
		}
		if (commentEndPos < 0) {
			throw new TokenNotClosedRuntimeException("*/", sql.substring(position));
		}
		token = sql.substring(position, commentEndPos);
		nextTokenType = SQL;
		position = commentEndPos + 2;
		tokenType = COMMENT;
	}

	protected void parseLineComment() {
		int commentEndPos1 = sql.indexOf("\r", position);
		int commentEndPos2 = sql.indexOf("\n", position);
		int commentEndPos = commentEndPos1;
		if (commentEndPos < 0) {
			commentEndPos = commentEndPos2;
		}
		if (commentEndPos < 0) {
			commentEndPos = sql.length();
		}
		token = sql.substring(position, commentEndPos);
		nextTokenType = SQL;
		position = commentEndPos;
		tokenType = LINE_COMMENT;
	}

	protected void parseBindVariable() {
		token = nextBindVariableName();
		nextTokenType = SQL;
		position += 1;
		tokenType = BIND_VARIABLE;
	}

	protected void parseElse() {
		token = null;
		nextTokenType = SQL;
		tokenType = ELSE;
	}

	protected void parseEof() {
		token = null;
		tokenType = EOF;
		nextTokenType = EOF;
	}

	public String skipToken() {
		int index = sql.length();
		char quote = position < sql.length() ? sql.charAt(position) : '\0';
		boolean quoting = quote == '\'' || quote == '(';
		if (quote == '(') {
			quote = ')';
		}
		for (int i = quoting ? position + 1 : position; i < sql.length(); ++i) {
			char c = sql.charAt(i);
			if ((Character.isWhitespace(c) || c == ',' || c == ')' || c == '(') && !quoting) {
				index = i;
				break;
			} else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
				index = i;
				break;
			} else if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
				index = i;
				break;
			} else if (quoting && quote == '\'' && c == '\'' && (i + 1 >= sql.length() || sql.charAt(i + 1) != '\'')) {
				index = i + 1;
				break;
			} else if (quoting && c == quote) {
				index = i + 1;
				break;
			}
		}
		token = sql.substring(position, index);
		tokenType = SQL;
		nextTokenType = SQL;
		position = index;
		return token;
	}

	public String skipWhitespace() {
		int index = skipWhitespace(position);
		token = sql.substring(position, index);
		position = index;
		return token;
	}

	private int skipWhitespace(int position) {
		int index = sql.length();
		for (int i = position; i < sql.length(); ++i) {
			char c = sql.charAt(i);
			if (!Character.isWhitespace(c)) {
				index = i;
				break;
			}
		}
		return index;
	}
}
