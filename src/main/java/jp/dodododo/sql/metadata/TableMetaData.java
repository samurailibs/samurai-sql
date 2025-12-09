package jp.dodododo.sql.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jp.dodododo.sql.annotation.Internal;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.DialectManager;
import jp.dodododo.sql.error.SQLError;
import jp.dodododo.sql.types.JavaTypes;
import jp.dodododo.sql.util.CacheUtil;
import jp.dodododo.sql.util.CaseInsensitiveMap;
import jp.dodododo.sql.util.ConnectionUtil;
import jp.dodododo.sql.util.DataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Internal
public class TableMetaData {

	private static final Logger logger = LoggerFactory.getLogger(TableMetaData.class);

	private String tableName;

	private String schema;

	private String catalog;

	private CaseInsensitiveMap<ColumnMetaData> columnMetaData = new CaseInsensitiveMap<>();
	private List<String> columnNames = new ArrayList<>();

	private CaseInsensitiveMap<ColumnMetaData> pkColumnMetaData = new CaseInsensitiveMap<>();
	private List<String> pkColumnNames = new ArrayList<>();

	public TableMetaData(Connection connection, String tableName) {
		init(connection, tableName);
	}

	public TableMetaData(DataSource dataSource, String tableName) {
		Connection connection = null;
		try {
			connection = DataSourceUtil.getConnection(dataSource);
			init(connection, tableName);
		} finally {
			ConnectionUtil.close(connection);
		}
	}

	private void init(Connection connection, String tableName) {
		try {
			Dialect dialect = DialectManager.getDialect(connection);
			this.schema = dialect.getSchema(connection);
			this.catalog = connection.getCatalog();
			this.tableName = dialect.getTableNameResolver().resolve(connection, tableName);
			DatabaseMetaData metaData = connection.getMetaData();

			setUpColumnMetaData(connection, metaData, this.tableName, dialect);
			setUpPks(metaData, this.tableName);
        } catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	private void setUpPks(DatabaseMetaData metaData, String tableName) {
		setUpPks(metaData, tableName, new ArrayList<>());
	}

	private void setUpPks(DatabaseMetaData metaData, String tableName, List<String> triedTableName) {
		triedTableName.add(tableName);
		String catalog = this.catalog;
		String schemaPattern = this.schema;
		try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schemaPattern, tableName)) {
			while (primaryKeys.next()) {
				String columnName = primaryKeys.getString(4);
				ColumnMetaData columnMetaData = getColumnMetaData(columnName);
				columnMetaData.setPrimaryKey(true);
				pkColumnMetaData.put(columnMetaData.getColumnName(), columnMetaData);
				pkColumnNames.add(columnMetaData.getColumnName());
			}
			logger.info("[primary key] setup is success. tableName[" + tableName + "]");
		} catch (SQLException ignore) {
			logger.error("[primary key] setup is fail. tableName[" + tableName + "]");
			logger.debug(ignore.getMessage(), ignore);
			if (triedTableName.contains(tableName.toUpperCase()) == false) {
				logger.info("[primary key] retry setup.");
				setUpPks(metaData, tableName.toUpperCase(), triedTableName);
			} else if (triedTableName.contains(tableName.toLowerCase()) == false) {
				logger.info("[primary key] retry setup.");
				setUpPks(metaData, tableName.toLowerCase(), triedTableName);
			}
		}
	}

	private void setUpColumnMetaData(Connection connection, DatabaseMetaData metaData, String tableName, Dialect dialect) {
		String catalog = this.catalog;
		String schemaPattern = this.schema;
		String tableNamePattern = tableName;
		String columnNamePattern = null;
		try (ResultSet columns = metaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);){

			while (columns.next()) {
				ColumnMetaData columnMetaData = new ColumnMetaData();
				String tableCatalog = columns.getString(1);
				columnMetaData.setTableCat(tableCatalog);
				String schema = columns.getString(2);
				columnMetaData.setTableSchem(schema);
				columnMetaData.setTableName(columns.getString(3));
				columnMetaData.setColumnName(columns.getString(4));
				columnMetaData.setDataType(columns.getInt(5));
				columnMetaData.setTypeName(columns.getString(6));
				columnMetaData.setColumnSize(columns.getInt(7));
				columnMetaData.setBufferLength(columns.getInt(8));
				columnMetaData.setDecimalDigits(columns.getInt(9));
				columnMetaData.setNumPrecRadix(columns.getInt(10));
				columnMetaData.setNullable(columns.getInt(11));
				columnMetaData.setRemarks(columns.getString(12));
				columnMetaData.setColumnDef(columns.getString(13));
				columnMetaData.setSqlDataType(columns.getString(14));
				columnMetaData.setSqlDatetimeSub(columns.getInt(15));
				columnMetaData.setCharOctetLength(columns.getInt(16));
				columnMetaData.setOrdinalPosition(columns.getInt(17));
				columnMetaData.setIsNullable(columns.getString(18));
				columnMetaData.setScopeCatlog(columns.getString(19));
				columnMetaData.setScopeSchema(columns.getString(20));
				columnMetaData.setScopeTable(columns.getString(21));
				columnMetaData.setSqlDataType(columns.getString(22));

				dialect.bugfix(columnMetaData);

				ResultSetMetaData resultSetMetaData = columns.getMetaData();
				if (23 <= resultSetMetaData.getColumnCount()) {
					columnMetaData.setAutoincrement(JavaTypes.BOOLEAN.convert(columns.getString(23)));
				}
				this.columnMetaData.put(columnMetaData.getColumnName(), columnMetaData);
				this.columnNames.add(columnMetaData.getColumnName());
			}
			if (columnMetaData.isEmpty() == true && tableName.equals(tableName.toUpperCase()) == false) {
				this.tableName = tableName.toUpperCase();
                setUpColumnMetaData(connection, metaData, tableName.toUpperCase(), dialect);
			}
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getCatalog() {
		return catalog;
	}

	public String getSchema() {
		return schema;
	}

	public List<String> getColumnNames() {
		return new ArrayList<>(columnNames);
	}

	public List<String> getPkColumnNames() {
		return new ArrayList<>(pkColumnNames);
	}

	public ColumnMetaData getColumnMetaData(String columnName) {
		return this.columnMetaData.get(columnName);
	}

	protected static final Map<String, Map<String, TableMetaData>> TABLE_META_DATA_CACHE = CacheUtil.cacheMap();

	public static TableMetaData getTableMetaData(Connection connection, String tableName) {
		Dialect dialect = DialectManager.getDialect(connection);
		String schema = dialect.getSchema(connection);
		return getTableMetaData(connection, schema, tableName);
	}

	public static TableMetaData getTableMetaData(Connection connection, String schema, String tableName) {
		Map<String, TableMetaData> metadatas = TABLE_META_DATA_CACHE.get(schema);
		if (metadatas == null) {
			metadatas = new CaseInsensitiveMap<>();
			TABLE_META_DATA_CACHE.put(schema, metadatas);
		}
		TableMetaData metaData = metadatas.get(tableName);
		if (metaData != null) {
			return metaData;
		}
		metaData = new TableMetaData(connection, tableName);
		metadatas.put(tableName, metaData);

		Dialect dialect = DialectManager.getDialect(connection);
		String tableSchema = dialect.getSchema(metaData);
		TABLE_META_DATA_CACHE.put(tableSchema, metadatas);
		return metaData;
	}
}
