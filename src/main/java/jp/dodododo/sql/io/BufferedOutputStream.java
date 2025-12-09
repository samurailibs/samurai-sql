package jp.dodododo.sql.io;

import java.io.OutputStream;

public class BufferedOutputStream extends java.io.BufferedOutputStream {

	public BufferedOutputStream(OutputStream out, int size) {
		super(out, size);
	}

	public BufferedOutputStream(OutputStream out) {
		super(out);
	}

	public OutputStream unwrap() {
		return out;
	}

}
