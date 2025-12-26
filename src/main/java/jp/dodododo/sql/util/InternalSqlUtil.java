package jp.dodododo.sql.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jp.dodododo.sql.sql.orderby.OrderByArg;
import jp.dodododo.sql.sql.orderby.SortType;

/**
 * sqlファイルから呼び出す場合にパッケージを記述すると可読性が下がるため、 このクラスは意図的に「デフォルトパッケージ」に置く。
 *
 * @author Satoshi Kimura
 */
public abstract class InternalSqlUtil {

	/**
	 * Listが null または空の場合にtrueを返す
	 *
	 * @param list
	 *            比較するList
	 * @return null または空の場合にtrue
	 */
	public static boolean isEmptyList(List<?> list) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Listが null または空の場合にfalseを返す
	 *
	 * @param list
	 *            比較するList
	 * @return null または空の場合にfalse
	 */
	public static boolean isNotEmptyList(List<?> list) {
		return isEmptyList(list) == false;
	}

	/**
	 * 文字列が null または空文字列の場合にtrueを返す
	 *
	 * @param string
	 *            比較する文字列
	 * @return null または空文字列の場合にtrue
	 */
	public static boolean isEmpty(String string) {
		return EmptyUtil.isEmpty(string);
	}

	/**
	 * 文字列が null または空文字列の場合にfalseを返す
	 *
	 * @param string
	 *            比較する文字列
	 * @return null または空文字列の場合にfalse
	 */
	public static boolean isNotEmpty(String string) {
		return isEmpty(string) == false;
	}

	/**
	 * 文字列が等しいかどうかを比較します。
	 *
	 * @param str1
	 *            the first String, may be null
	 * @param str2
	 *            the second String, may be null
	 * @return 等しい場合trueを返す
	 */
	public static boolean equals(String str1, String str2) {
		if (str1 != null) {
			return str1.equals(str2);
		} else if (str2 != null) {
			return str2.equals(str1);
		} else {
			return true;
		}
	}

	/**
	 * 文字列が等しくないかどうかを比較します。
	 *
	 * @param str1
	 *            the first String, may be null
	 * @param str2
	 *            the second String, may be null
	 * @return 等しい場合falseを返す
	 */
	public static boolean notEquals(String str1, String str2) {
		return !equals(str1, str2);
	}

	/**
	 * 数値が等しいかどうかを比較します。
	 *
	 * @param num1
	 *            the first Number, may be null
	 * @param num2
	 *            the second Number, may be null
	 * @return 等しい場合trueを返す
	 */
	public static boolean equals(Number num1, Number num2) {
		if (num1 != null) {
			return num1.equals(num2);
		} else if (num2 != null) {
			return num2.equals(num1);
		} else {
			return true;
		}
	}

	/**
	 * 数値が等しくないかどうかを比較します。
	 *
	 * @param expected
	 *            予想
	 * @param actual
	 *            実際の値
	 * @return 等しい場合falseを返す
	 */
	public static boolean notEquals(Number expected, Number actual) {
		return !equals(expected, actual);
	}

	/**
	 * 引数で受け取った値をSQLに変換します。
	 *
	 * @param argsList
	 *            ORDER BYのリスト
	 * @return ORDER BY句
	 */
	public static String orderBy(List<OrderByArg> argsList) {
		OrderByArg[] args;
		if (argsList == null) {
			args = null;
		} else {
			args = argsList.toArray(new OrderByArg[argsList.size()]);
		}
		return orderBy(args);
	}

	/**
	 * 引数で受け取った値をSQLに変換します。
	 *
	 * @param args
	 *            ORDER BYの配列
	 * @return ORDER BY句
	 */
	public static String orderBy(OrderByArg... args) {
		if (EmptyUtil.isEmpty(args) == true) {
			return "";
		}
		StringBuilder sql = new StringBuilder();
		Arrays.asList(args).forEach(arg -> {
			sql.append(arg.getColumnName());
			SortType sortType = arg.getSortType();
			if (sortType != null) {
				sql.append(" ");
				sql.append(sortType.toString());
			}
			sql.append(", ");
		});
		sql.setLength(sql.length() - ", ".length());
		return sql.toString();
	}

	public static String ins(Collection<Object> argList) {
		return in(argList);
	}

	/**
	 * 引数で受け取った値をSQLに変換します。
	 *
	 * @param args
	 *            INの配列
	 * @return IN句
	 */
	public static String ins(Object[] args) {
		return in(args);
	}

	public static String in(Collection<Object> argList) {
		Object[] args;
		if (argList == null) {
			args = null;
		} else {
			args = CollectionUtil.toArray(argList);
		}
		return in(args);
	}

	/**
	 * 引数で受け取った値をSQLに変換します。
	 *
	 * @param args
	 *            INの配列
	 * @return IN句
	 */
	public static String in(Object[] args) {
		if (EmptyUtil.isEmpty(args) == true) {
			return "";
		}
		StringBuilder sql = new StringBuilder();
		Arrays.asList(args).forEach(arg -> {
			if (arg == null) {
				sql.append("NULL");
			} else if (arg instanceof CharSequence) {
				sql.append("'").append(arg).append("'");
			} else {
				sql.append(arg);
			}
			sql.append(", ");
		});
		sql.setLength(sql.length() - ", ".length());
		return sql.toString().trim();
	}
}
