package jp.dodododo.dao.impl;

import java.io.InputStream;

import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.dialect.HSQL;
import jp.dodododo.dao.id.Sequence;

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
