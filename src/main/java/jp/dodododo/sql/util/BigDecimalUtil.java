package jp.dodododo.sql.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

	/**
	 * The value 0, with a scale of 0.
	 *
	 * @see BigDecimal#ZERO
	 */
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	/**
	 * The value 1, with a scale of 0.
	 *
	 * @see BigDecimal#ONE
	 */
	public static final BigDecimal ONE = BigDecimal.ONE;
	/**
	 * The value 10, with a scale of 0.
	 *
	 * @see BigDecimal#TEN
	 */
	public static final BigDecimal TEN = BigDecimal.TEN;
	/**
	 * The value 100, with a scale of 0.
	 */
	public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);
	/**
	 * The value -1, with a scale of 0.
	 */
	public static final BigDecimal MINUS_ONE = BigDecimal.valueOf(-1L);
	/**
	 * The value -10, with a scale of 0.
	 */
	public static final BigDecimal MINUS_TEN = minus(TEN).setScale(0);

	/**
	 * reverse +,- sign.
	 *
	 * @see #minus(BigDecimal)
	 */
	public static BigDecimal reverseSign(BigDecimal decimal) {
		return minus(decimal);
	}

	/**
	 * decimal * -1;
	 *
	 * @param decimal decimal
	 * @return decimal * -1
	 */
	public static BigDecimal minus(BigDecimal decimal) {
		return decimal.multiply(MINUS_ONE);
	}
}
