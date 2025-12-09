package jp.dodododo.sql.impl;

import java.io.InputStream;

import jp.dodododo.sql.annotation.Column;
import jp.dodododo.sql.annotation.Id;
import jp.dodododo.sql.annotation.IdDefSet;
import jp.dodododo.sql.dialect.HSQL;
import jp.dodododo.sql.id.Sequence;

public class BinaryTable {
	private int id;

	@Column(value = "bin", alias = { "binary" })
	private InputStream binary;

	public InputStream getBinary() {
		return binary;
	}

	public void setBinary(InputStream binary) {
		this.binary = binary;
	}

	public int getId() {
		return id;
	}

	@Id( { @IdDefSet(strategy = Sequence.class, name = "sequence", db = HSQL.class) })
	public void setId(int id) {
		this.id = id;
	}

}
