package jp.dodododo.sql.sql.parse;

import java.util.Stack;
import java.util.regex.Pattern;

import jp.dodododo.sql.exception.EndCommentNotFoundRuntimeException;
import jp.dodododo.sql.exception.IfConditionNotFoundRuntimeException;
import jp.dodododo.sql.sql.node.BeginNode;
import jp.dodododo.sql.sql.node.BindVariableNode;
import jp.dodododo.sql.sql.node.ContainerNode;
import jp.dodododo.sql.sql.node.ElseNode;
import jp.dodododo.sql.sql.node.EmbeddedValueNode;
import jp.dodododo.sql.sql.node.IfNode;
import jp.dodododo.sql.sql.node.InNode;
import jp.dodododo.sql.sql.node.Node;
import jp.dodododo.sql.sql.node.OrderByNode;
import jp.dodododo.sql.sql.node.ParenBindVariableNode;
import jp.dodododo.sql.sql.node.PrefixSqlNode;
import jp.dodododo.sql.sql.node.SqlNode;
import jp.dodododo.sql.util.EmptyUtil;
import jp.dodododo.sql.util.StringUtil;

/**
 *
 * @author Satoshi Kimura
 */
public class SqlParser {

	private static final Pattern LINE_BREAK = Pattern.compile("(\\r(\\n)?|\\n)");

	protected String sql;

	protected SqlTokenizer tokenizer;

	protected Stack<Node> nodeStack = new Stack<Node>();

	public SqlParser(String sql) {
		String formattedSql = sql.trim();
		if (formattedSql.endsWith(";") == true || (formattedSql.endsWith("*/") == false && formattedSql.endsWith("/") == true)) {
			formattedSql = formattedSql.substring(0, formattedSql.length() - 1);
		}
		formattedSql = deleteQuestionInLineComment(formattedSql);
		this.sql = formattedSql;
		tokenizer = new SqlTokenizer(formattedSql);
	}

	public Node parse() {
		push(new ContainerNode());
		while (SqlTokenizer.EOF != tokenizer.next()) {
			parseToken();
		}
		Node node = pop();
		node.setSql(this.sql);
		return node;
	}

	protected String deleteQuestionInLineComment(String sql) {
		String[] sqlParts = LINE_BREAK.split(sql);
		StringBuilder buf = new StringBuilder(128);
		for (int i = 0; i < sqlParts.length; i++) {
			int pos = sqlParts[i].indexOf("--");
			if (pos != -1 && (sqlParts[i].indexOf("ELSE") == -1)) {
				buf.append(sqlParts[i].substring(0, pos));
				buf.append(sqlParts[i].substring(pos).replaceAll("\\?", ""));
			} else {
				buf.append(sqlParts[i]);
			}
			if (i + 1 != sqlParts.length) {
				buf.append("\n");
			}
		}
		return buf.toString();
	}

	protected void parseToken() {
		switch (tokenizer.getTokenType()) {
		case SqlTokenizer.SQL:
			parseSql();
			break;
		case SqlTokenizer.COMMENT:
			parseComment();
			break;
		case SqlTokenizer.ELSE:
			parseElse();
			break;
		case SqlTokenizer.BIND_VARIABLE:
			parseBindVariable();
			break;
		case SqlTokenizer.LINE_COMMENT:
			parseLineComment();
			break;
		default:
			// S2Daoのソースが空だったので、空とする
		}
	}

	protected void parseLineComment() {
		// empty
	}

	protected void parseSql() {
		String sql = tokenizer.getToken();
		if (isElseMode()) {
			sql = StringUtil.replace(sql, "--", "");
		}
		Node node = peek();
		if ((node instanceof IfNode || node instanceof ElseNode) && node.isEmpty() == true) {

			SqlTokenizer st = new SqlTokenizer(sql);
			st.skipWhitespace();
			String token = st.skipToken();
			st.skipWhitespace();
			if (sql.startsWith(",")) {
				if (sql.startsWith(", ")) {
					node.addChild(new PrefixSqlNode(", ", sql.substring(2)));
				} else {
					node.addChild(new PrefixSqlNode(",", sql.substring(1)));
				}
			} else if ("AND".equalsIgnoreCase(token) || "OR".equalsIgnoreCase(token)) {
				node.addChild(new PrefixSqlNode(st.getBefore(), st.getAfter()));
			} else {
				node.addChild(new SqlNode(sql));
			}
		} else {
			node.addChild(new SqlNode(sql));
		}
	}

	protected void parseComment() {
		String comment = tokenizer.getToken();
		if (isTargetComment(comment)) {
			if (isIfComment(comment)) {
				parseIf();
			} else if (isBeginComment(comment)) {
				parseBegin();
			} else if (isEndComment(comment)) {
				return;
			} else if (isOrderByComment(comment)) {
				parseOrderBy();
			} else if (isInComment(comment)) {
				parseIn();
			} else {
				parseCommentBindVariable();
			}
		} else if (comment != null && 0 < comment.length()) {
			String before = tokenizer.getBefore();
			peek().addChild(new SqlNode(before.substring(before.lastIndexOf("/*")).replaceAll("\\?", "")));
		}
	}

	/**
	 *
	 */
	protected void parseOrderBy() {
		String expr = tokenizer.getToken().substring("ORDER BY".length()).trim();
		tokenizer.skipToken();
		OrderByNode orderByNode = new OrderByNode(expr);
		peek().addChild(orderByNode);
		push(orderByNode);
		parseEnd();
	}
	/**
	 *
	 */
	protected void parseIn() {
		String expr = tokenizer.getToken().substring("IN".length()).trim();
		tokenizer.skipToken();
		InNode inNode = new InNode(expr);
		peek().addChild(inNode);
		push(inNode);
		parseEnd();
	}

	protected void parseIf() {
		String condition = tokenizer.getToken().substring(2).trim();
		if (EmptyUtil.isEmpty(condition)) {
			throw new IfConditionNotFoundRuntimeException();
		}
		IfNode ifNode = new IfNode(condition);
		peek().addChild(ifNode);
		push(ifNode);
		parseEnd();
	}

	protected void parseBegin() {
		BeginNode beginNode = new BeginNode();
		peek().addChild(beginNode);
		push(beginNode);
		parseEnd();
	}

	protected void parseEnd() {
		while (SqlTokenizer.EOF != tokenizer.next()) {
			if (tokenizer.getTokenType() == SqlTokenizer.COMMENT && isEndComment(tokenizer.getToken())) {

				pop();
				return;
			}
			parseToken();
		}
		throw new EndCommentNotFoundRuntimeException();
	}

	protected void parseElse() {
		Node parent = peek();
		if (!(parent instanceof IfNode)) {
			return;
		}
		IfNode ifNode = (IfNode) pop();
		ElseNode elseNode = new ElseNode();
		ifNode.setElseNode(elseNode);
		push(elseNode);
		tokenizer.skipWhitespace();
	}

	protected void parseCommentBindVariable() {
		String expr = tokenizer.getToken();
		String s = tokenizer.skipToken();
		if (s.startsWith("(") && s.endsWith(")")) {
			peek().addChild(new ParenBindVariableNode(expr));
		} else if (expr.startsWith("$")) {
			peek().addChild(new EmbeddedValueNode(expr.substring(1)));
		} else {
			peek().addChild(new BindVariableNode(expr));
		}
	}

	protected void parseBindVariable() {
		String expr = tokenizer.getToken();
		peek().addChild(new BindVariableNode(expr));
	}

	protected Node pop() {
		return nodeStack.pop();
	}

	protected Node peek() {
		return nodeStack.peek();
	}

	protected void push(Node node) {
		nodeStack.push(node);
	}

	protected boolean isElseMode() {
		for (int i = 0; i < nodeStack.size(); ++i) {
			if (nodeStack.get(i) instanceof ElseNode) {
				return true;
			}
		}
		return false;
	}

	protected boolean isTargetComment(String comment) {
		return comment != null && comment.length() > 0 && Character.isJavaIdentifierStart(comment.charAt(0));
	}

	protected boolean isIfComment(String comment) {
		return comment.startsWith("IF");
	}

	protected boolean isBeginComment(String content) {
		return content != null && "BEGIN".equals(content);
	}

	protected boolean isEndComment(String content) {
		return content != null && "END".equals(content);
	}

	protected boolean isOrderByComment(String comment) {
		return comment.startsWith("ORDER BY");
	}

	protected boolean isInComment(String comment) {
		return comment.startsWith("IN");
	}
}
