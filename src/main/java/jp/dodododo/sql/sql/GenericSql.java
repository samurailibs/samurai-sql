package jp.dodododo.sql.sql;

import jp.dodododo.sql.util.FreeMarkerUtil;

public enum GenericSql implements Sql {
	/**
	 *
	 */
	ALL("SELECT * FROM ${tableName} /*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/"),
	/**
	 *
	 */
	COUNT_ALL("SELECT count(*) FROM ${tableName}"),
	/**
	 *
	 */
	SIMPLE_WHERE(
		"SELECT * FROM ${tableName} <#list columns as c ><#if c_index == 0>WHERE </#if>${c.name} ${c.operator} <#if c_has_next>AND </#if></#list>/*ORDER BY @SqlUtil@orderBy(orderBy)*/ORDER BY XXX/*END*/"),
	/**
	 *
	 */
	BY(SIMPLE_WHERE),
	/**
	 *
	 */
	SIMPLE_COUNT_WHERE(
			"SELECT count(*) FROM ${tableName} <#list columns as c ><#if c_index == 0>WHERE</#if> ${c.name} ${c.operator} <#if c_has_next>AND</#if> </#list>"),
	/**
	 *
	 */
	COUNT_BY(SIMPLE_COUNT_WHERE),
	/**
	 *
	 */
	INSERT(
			"INSERT INTO ${tableName} (<#list columns as c >${c.name}<#if c_has_next> ,</#if></#list>) VALUES (<#list columns as c >/*${c.name}*/'dummy'<#if c_has_next> ,</#if></#list>)"),
	/**
	 * update by primary key.
	 */
	UPDATE(
			"UPDATE ${tableName} SET <#list columns as c >${c.name} = /*${c.name}*/'dummy' <#if c_has_next>,</#if></#list>WHERE <#list pks as pk >${pk.name} = /*${pk.name}*/'dummy'<#if pk_has_next> AND </#if></#list>"),
	/**
	 * delete by primary key.
	 */
	DELETE("DELETE FROM ${tableName} WHERE <#list pks as pk >${pk.name} = /*${pk.name}*/'dummy'<#if pk_has_next> AND </#if></#list>"),
	/**
	 * MySQL(INSERT ... ON DUPLICATE KEY UPDATE), SQLite(INSERT OR REPLACE),Firebird(UPDATE OR INSERT), other(UnsupportedOperationException)
	 */
	REPLACE(
			"${dialect.replaceCommand} INTO ${tableName} (<#list columns as c >${c.name}<#if c_has_next> ,</#if></#list>) VALUES (<#list columns as c >/*${c.name}*/'dummy'<#if c_has_next> ,</#if></#list>) ${dialect.replaceOption}"),
	/**
	 *
	 */
	DELETE_ALL("DELETE FROM ${tableName}"),
	/**
	 * INSERT INTO table (col1, col2) VALUES (val1, val2), (val21, va22), (val31, va32), ...
	 */
	INSERT_BATCH(
			"INSERT INTO ${tableName} (<#list columns as c >${c.name}<#if c_has_next> ,</#if></#list>) VALUES <#list vals as val>(<#list columns as c >/*vals[${val_index}].${c.name}*/'dummy'<#if c_has_next> ,</#if></#list>) <#if val_has_next>,</#if> </#list>");

	private final String sqlTemplate;

	GenericSql(String sqlTemplate) {
		this.sqlTemplate = sqlTemplate;
	}

	GenericSql(GenericSql genericSql) {
		this(genericSql.sqlTemplate);
	}

    @Override
	public String getSql(SqlContext context) {
		return FreeMarkerUtil.process(sqlTemplate, context).trim();
	}
}
