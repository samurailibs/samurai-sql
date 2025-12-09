package jp.dodododo.sql.sql;

import jp.dodododo.sql.value.OperatorAndValues;

/**
 */
public class Operator {
	/** = (equals) */
	public static OperatorAndValues eq(Object value) {
		return new OperatorAndValues("= /*${name}*/'dummy'".intern(), value);
	}

	/** &lt;&gt; (not equals) */
	public static OperatorAndValues ne(Object value) {
		return new OperatorAndValues("<> /*${name}*/'dummy'".intern(), value);

	}

	/** &lt; (less than) */
	public static OperatorAndValues lt(Object value) {
		return new OperatorAndValues("< /*${name}*/'dummy'".intern(), value);
	}

	/** &lt;= (less than or equal) */
	public static OperatorAndValues le(Object value) {
		return new OperatorAndValues("<= /*${name}*/'dummy'".intern(), value);
	}

	/** &gt; (greater than) */
	public static OperatorAndValues gt(Object value) {
		return new OperatorAndValues("> /*${name}*/'dummy'".intern(), value);
	}

	/** &gt;= (greater than or equal) */
	public static OperatorAndValues ge(Object value) {
		return new OperatorAndValues(">= /*${name}*/'dummy'".intern(), value);
	}

	/** IN */
	public static OperatorAndValues in(Object... values) {
		return new OperatorAndValues("IN /*IN ${name}*/('dummy1', 'dummy2')/*END*/".intern(), values, false);
	}

	/** NOT IN */
	public static OperatorAndValues notIn(Object... values) {
		return new OperatorAndValues("NOT IN /*IN ${name}*/('dummy1', 'dummy2')/*END*/".intern(), values, false);
	}

	/** LIKE */
	public static OperatorAndValues like(Object value) {
		return new OperatorAndValues("LIKE /*${name}*/'dummy'".intern(), addPercentLast(addPercentFirst(value)));
	}

	/** NOT LIKE */
	public static OperatorAndValues notLike(Object value) {
		return new OperatorAndValues("NOT LIKE /*${name}*/'dummy'".intern(), addPercentLast(addPercentFirst(value)));
	}

	/** LIKE xxx% */
	public static OperatorAndValues starts(Object value) {
		return new OperatorAndValues("LIKE /*${name}*/'dummy'".intern(), addPercentLast(value));
	}

	/** NOT LIKE xxx% */
	public static OperatorAndValues notStarts(Object value) {
		return new OperatorAndValues("NOT LIKE /*${name}*/'dummy'".intern(), addPercentLast(value));
	}

	/** LIKE %xxx */
	public static OperatorAndValues ends(Object value) {
		return new OperatorAndValues("LIKE /*${name}*/'dummy'".intern(), addPercentFirst(value));
	}

	/** NOT LIKE %xxx */
	public static OperatorAndValues notEnds(Object value) {
		return new OperatorAndValues("NOT LIKE /*${name}*/'dummy'".intern(), addPercentFirst(value));
	}

	/** LIKE %xxx% */
	public static OperatorAndValues contains(Object value) {
		return new OperatorAndValues("LIKE /*${name}*/'dummy'".intern(), addPercentLast(addPercentFirst(value)));
	}

	/** NOT LIKE %xxx% */
	public static OperatorAndValues notContains(Object value) {
		return new OperatorAndValues("NOT LIKE /*${name}*/'dummy'".intern(), addPercentLast(addPercentFirst(value)));
	}

	/** IS NULL */
	public static OperatorAndValues isNull() {
		return new OperatorAndValues("IS NULL".intern(), "");
	}

	/** IS NOT NULL */
	public static OperatorAndValues isNotNull() {
		return new OperatorAndValues("IS NOT NULL".intern(), "");
	}

	private static String addPercentFirst(Object value) {
		String str = value.toString();
		if (str.startsWith("%")) {
			return str;
		}
		return "%" + str;
	}

	private static String addPercentLast(Object value) {
		String str = value.toString();
		if (str.endsWith("%")) {
			return str;
		}
		return str + "%";
	}
}
