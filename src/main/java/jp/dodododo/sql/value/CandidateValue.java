package jp.dodododo.sql.value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Columns;
import jp.dodododo.sql.annotation.Internal;
import jp.dodododo.sql.annotation.Relations;
import jp.dodododo.sql.message.Message;
import jp.dodododo.sql.util.EmptyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class CandidateValue implements Serializable {
	private static final long serialVersionUID = -2173377825923811975L;

	protected static final Logger logger = LoggerFactory.getLogger(CandidateValue.class);

	/**
	 * {@link Relations}で一致。
	 */
	public static final int PRIORITY_LEVEL_RELATIONS_ANNOTATION = 1;
	/**
	 * {@link Columns}で一致。
	 */
	public static final int PRIORITY_LEVEL_COLUMNS_ANNOTATION = 2;
	/**
	 * {@link Column}で一致。
	 */
	public static final int PRIORITY_LEVEL_COLUMN_ANNOTATION = 3;
	/**
	 * 規約で一致。
	 */
	public static final int PRIORITY_LEVEL_CONVENTION = 4;
	/**
	 * 一致しない。
	 */
	public static final int PRIORITY_LEVEL_NO_MATCH = Integer.MAX_VALUE;
	public static final CandidateValue EMPTY = new CandidateValue(new ValueProxy((Object)null), false, CandidateValue.PRIORITY_LEVEL_NO_MATCH);

	public ValueProxy value;
	public boolean matchTableName;
	public int priorityLevel;

	public CandidateValue(final ValueProxy value, final boolean matchTableName, int priorityLevel) {
		this.value = value;
		this.matchTableName = matchTableName;
		this.priorityLevel = priorityLevel;
	}

	@Override
	public String toString() {
		return CandidateValue.class.getName() + " : " + value.getValue();
	}

	public static class Comparator implements java.util.Comparator<CandidateValue> {

	    @Override
		public int compare(CandidateValue o1, CandidateValue o2) {
			int level = o1.priorityLevel - o2.priorityLevel;
			if (level != 0) {
				return level;
			}
			if (o1.matchTableName == o2.matchTableName) {
				return 0;
			}
			if (o1.matchTableName == true) {
				return -1;
			}
			if (o2.matchTableName == true) {
				return 1;
			}
			return 0;
		}
	}

	public static CandidateValue getValue(List<CandidateValue> values, String tableName, String columnName) {
		if (values.isEmpty()) {
			return CandidateValue.EMPTY;
		}

		Collections.sort(values, new CandidateValue.Comparator());

		if (logger.isTraceEnabled() && 2 <= values.size()) {
			List<CandidateValue> matchTableValues = new ArrayList<>(values.size());
			values.stream().filter(value -> value.matchTableName).forEach(value -> matchTableValues.add(value));
			List<CandidateValue> list = new ArrayList<>();
			if(matchTableValues.isEmpty() == false) {
				list.addAll(matchTableValues);
			} else {
				list.addAll(values);
			}

			if (2 <= list.size()) {
				CandidateValue o1 = list.get(0);
				CandidateValue o2 = list.get(1);
				if (new CandidateValue.Comparator().compare(o1, o2) == 0) {
					Object value1 = o1.value.getValue();
					Object value2 = o2.value.getValue();
					Object target1 = o1.value.getTarget();
					Object target2 = o2.value.getTarget();
					if (value1 == target2 || value2 == target1) {
						// ex : enum and enum.getCode()
					} else if (value1 == value2 && o1.value.getPropertyDesc().getPropertyName().equals(o2.value.getPropertyDesc().getPropertyName())) {
						// ex : o1.o3.getXxx() and o2.o3.getXxx()
					} else {
						logger.warn(Message.getMessage("00053", o1.value, o2.value));
					}
				}
			}
		}

		for (CandidateValue value : values) {
			if (value.matchTableName == true) {
				traceReason(tableName, columnName, value);
				return value;
			}
		}
		CandidateValue value = values.get(0);
		traceReason(tableName, columnName, value);
		return value;
	}

	private static void traceReason(String tableName, String columnName, CandidateValue value) {
		if (logger.isTraceEnabled()) {
			if (EmptyUtil.isEmpty(tableName) || EmptyUtil.isEmpty(columnName)) {
				return;
			}
			String reason;
			if (value.priorityLevel != PRIORITY_LEVEL_NO_MATCH) {
				reason = Message.getMessage("000" + (39 - value.priorityLevel));
			} else {
				reason = Message.getMessage("00055");
			}
			logger.trace(Message.getMessage("00054", tableName, columnName, toPdString(value), value.matchTableName, reason));
		}
	}

	private static String toPdString(CandidateValue value) {
		if (value == null) {
			return "root value is null.";
		} else if (value.value == null) {
			return "value.value is null.";
		} else if (value.value.pd == null) {
			return "value.value.pd is null.";
		} else {
			return value.value.pd.toString();
		}
	}

	public void pullRealValue() {
		value.getValue();
	}
}
