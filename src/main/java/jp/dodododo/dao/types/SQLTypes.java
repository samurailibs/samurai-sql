package jp.dodododo.dao.types;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import jp.dodododo.dao.error.SQLError;
import jp.dodododo.dao.util.TypesUtil;

public enum SQLTypes implements SQLType {
	STRING(Types.CHAR, String.class, false),
	//
	VARCHAR(Types.VARCHAR, String.class, false),
	//
	NUMBER(Types.NUMERIC, BigDecimal.class, false),
	//
	DATE(Types.TIMESTAMP, java.util.Date.class, false),
	//
	SQL_DATE(Types.DATE, Date.class, false),
	//
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class, false),
	//
	TIME(Types.TIME, Time.class, false),
	//
	BINARY(Types.BINARY, Object.class, true),
	//
	BLOB(Types.BLOB, Blob.class, true),
	//
	CLOB(Types.CLOB, Clob.class, true),
	//
	NCLOB(Types.NCLOB, NClob.class, true),
	//
	OBJECT(Types.JAVA_OBJECT, Object.class, false),
	//
	BOOLEAN(Types.BOOLEAN, Boolean.class, false),
	//
	SQLXML(Types.SQLXML, Object.class, false),
	//
	STRUCT(Types.STRUCT, Object.class, false),
	//
	ARRAY(Types.ARRAY, Object[].class, false),
	//
	ROW_ID(Types.ROWID, RowId.class, false),
	//
	REF(Types.REF, Object.class, false) {
		@Override
		public Object convert(Object value) {
			if (value instanceof Ref) {
				Ref ref = (Ref) value;
				try {
					return ref.getObject();
				} catch (SQLException e) {
					throw new SQLError(e);
				}
			}
			return super.convert(value);
		}
	},
	//
	NULL(Types.NULL, Object.class, false);

	private int type;

	private Class<?> javaType;

	private  boolean isBinary;

	private static final Map<Integer, SQLType> VALUES = new HashMap<>();

	SQLTypes(int type, Class<?> javaType, boolean isBinary) {
		init(type, javaType, isBinary);

	}

	private void init(int type, Class<?> javaType, boolean isBinary) {
		this.type = type;
		this.javaType = javaType;
		this.isBinary = isBinary;
		if (VALUES != null) {
			add(type, this);
		}
	}

    public static SQLType valueOf(int type) {
    		SQLType ret = VALUES.get(Integer.valueOf(type));
    		if(ret == null) {
    			throw new UnsupportedOperationException(Integer.toString(type)+ " : " + VALUES);
    		}
        return ret;
    }

    @Override
	public int getType() {
		return type;
	}

    @Override
	public Object convert(Object value) {
		return convert(value, new String[0]);
	}

    @Override
	public Object convert(Object value, String... fromats) {
		return TypesUtil.getJavaType(javaType).convert(value, fromats);
	}

	public static void add(int type, SQLType sqlType) {
		VALUES.put(type, sqlType);
	}

    @Override
	public JavaType<?> toJavaType() {
		return TypesUtil.getJavaType(this.javaType);
	}

    @Override
	public boolean isBinary() {
		return isBinary;
	}
}
