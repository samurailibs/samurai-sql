package jp.dodododo.sql.types;

import java.sql.RowId;
import java.sql.Types;
import java.util.Arrays;

public class RowIdImpl implements RowId {

	public static final int SQL_TYPE = Types.ROWID;

	private final byte[] id;

	private int hash;

	public RowIdImpl(byte[] id) {
		if (id == null) {
			throw new IllegalArgumentException("id");
		}
		this.id = id;
		this.hash = Arrays.hashCode(this.id);
	}

	public RowIdImpl(RowId rowId) {
		this(rowId.getBytes());
	}

	public RowIdImpl(String id) {
		this(TypeConverter.convert(id, byte[].class));
	}

	@Override
	public boolean equals(Object o) {
		return ((o instanceof RowIdImpl) && (Arrays.equals(id, ((RowIdImpl) o).id)));
	}

	@Override
	public byte[] getBytes() {
		byte[] ret = new byte[id.length];
		System.arraycopy(id, 0, ret, 0, id.length);
		return ret;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public int hashCode() {
		return this.hash;
	}
}
