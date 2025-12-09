package jp.dodododo.sql.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jp.dodododo.sql.columns.ResultSetColumn;
import jp.dodododo.sql.types.SQLType;
import jp.dodododo.sql.types.SQLTypes;
import jp.dodododo.sql.types.TypeConverter;

public class DbArrayList<T> extends ArrayList<T> {
	private static final long serialVersionUID = -1400549097017247568L;

	protected List<ResultSetColumn> resultSetColumnList = new ArrayList<>();

	protected List<ColMetadata> cols = new ArrayList<>();
	protected List<Row> rows = new ArrayList<>();

	public DbArrayList() {
		super();
	}

	public DbArrayList(Collection<? extends T> c) {
		super(c);

		if (c instanceof DbArrayList) {
			@SuppressWarnings("unchecked")
			DbArrayList<T> dbArrayList = (DbArrayList<T>) c;
			this.resultSetColumnList = dbArrayList.getResultSetColumnList();
			this.cols = dbArrayList.cols;
			this.rows = dbArrayList.rows;
		}
	}

	public DbArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public void setHeader(List<ResultSetColumn> resultSetColumnList) {
		setResultSetColumnList(resultSetColumnList);
		resultSetColumnList.forEach(resultSetColumn -> {
			ColMetadata col = new ColMetadata();
			col.setName(resultSetColumn.getName());
			col.setDataType(resultSetColumn.getDataType());
			col.setMaxDataLength(resultSetColumn.getDisplaySize());
			cols.add(col);
		});
	}

	public void addRow(List<Object> datas) {
		Row row = new Row(datas);
		this.rows.add(row);
		for (int i = 0; i < row.datas.size(); i++) {
			String data = row.datas.get(i);
			cols.get(i).setMaxDataLength(StringUtil.length(data));
		}
	}

	public List<String> getColumnNames() {
		List<String> ret = new ArrayList<>(resultSetColumnList.size());
		resultSetColumnList.forEach(column -> ret.add(column.getName()));
		return ret;
	}

	public void setResultSetColumnList(List<ResultSetColumn> resultSetColumnList) {
		this.resultSetColumnList.addAll(resultSetColumnList);
	}

	public List<ResultSetColumn> getResultSetColumnList() {
		return this.resultSetColumnList;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(1024);
		buffer.append("\r\n");

		for (ColMetadata col : cols) {
			buffer.append(format(col.name, col.maxDataLength, col.dataType, true));
			buffer.append('\t');
		}
		buffer.append("\r\n");
		int length = buffer.length() + cols.size() * 2;

		for (int i = 0; i < length; i++) {
			buffer.append("-");
		}
		buffer.append("\r\n");

		for (int i = 0; i < rows.size(); i++) {
			buffer.append(rows.get(i)).append("\r\n");
		}

		for (int i = 0; i < length; i++) {
			buffer.append("-");
		}
		buffer.append("\r\n");

		return buffer + super.toString();
	}

	@Override
	public Object clone() {
		return new DbArrayList<>(this);
	}

	protected boolean isBinary(int dataType) {
		SQLType sqlType = TypesUtil.getSQLType(dataType);
		return sqlType.isBinary();
	}

	protected Object format(Object data, int maxLength, int dataType, boolean header) {
		StringBuilder ret = new StringBuilder();
		if (header == true) {
			ret.append(data);
		} else if (isBinary(dataType) == true) {
		} else {
			ret.append(data);
		}
		SQLType sqlType = TypesUtil.getSQLType(dataType);
		while (StringUtil.length(ret.toString()) < maxLength) {
			if (header == true) {
				ret.append(' ');
			} else if (SQLTypes.NUMBER.equals(sqlType)) {
				ret.insert(0, ' ');
			} else {
				ret.append(' ');
			}
		}

		return ret;
	}

	public class Row implements Serializable{
		private static final long serialVersionUID = -8684091440216292108L;

		protected List<String> datas = new ArrayList<>();

		public Row() {
		}

		public Row(List<Object> datas) {
			for (Object data : datas) {
				addData(data);
			}
		}

		public void addData(Object data) {
			String s;
			if (data == null) {
				s = "NULL";
			} else if (data instanceof Date) {
				s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(data);
			} else if (data instanceof Calendar) {
				s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(TypeConverter.convert(data, Date.class));
			} else {
				s = data.toString();
			}
			this.datas.add(s);
		}

		@Override
		public String toString() {
			if (EmptyUtil.isEmpty(datas) == true) {
				return "";
			}
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < datas.size(); i++) {
				Object data = datas.get(i);
				buffer.append(format(data, getMaxLength(i), getDataType(i), false));
				buffer.append('\t');
			}
			buffer.setLength(buffer.length() - 1);
			return buffer.toString();
		}

		protected int getMaxLength(int colIndex) {
			return cols.get(colIndex).maxDataLength;
		}

		protected int getDataType(int colIndex) {
			return cols.get(colIndex).dataType;
		}
	}

	public class ColMetadata implements Serializable {
		private static final long serialVersionUID = 3133972248320887014L;

		protected String name;
		protected int dataType;
		protected int maxDataLength = 10;

		public void setName(String name) {
			this.name = name;
			int length = StringUtil.length(name);
			setMaxDataLength(length);
		}

		public void setMaxDataLength(int maxDataLength) {
			if (isBinary(dataType) == true) {
				return;
			}

			if (this.maxDataLength < maxDataLength) {
				this.maxDataLength = maxDataLength;
			}
		}

		public void setDataType(int dataType) {
			this.dataType = dataType;
		}
	}
}
