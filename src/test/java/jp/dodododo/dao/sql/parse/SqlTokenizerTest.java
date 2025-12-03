package jp.dodododo.dao.sql.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.Reader;

import jp.dodododo.dao.util.InputStreamReaderUtil;
import jp.dodododo.dao.util.ReaderUtil;

import org.junit.jupiter.api.Test;

public class SqlTokenizerTest {

	@Test
	public void testNext() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jp/dodododo/dao/sql/parse/nest_if2.sql");
		Reader reader = InputStreamReaderUtil.create(is, "UTF-8");
		String sql = ReaderUtil.readText(reader);
		SqlTokenizer tokenizer = new SqlTokenizer(sql);

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("SELECT * FROM EMP".trim(), tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("BEGIN", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.LINE_COMMENT, tokenizer.next());
		assertEquals("-- comment0", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("where", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.LINE_COMMENT, tokenizer.next());
		assertEquals("-- comment1", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("IF true", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("IF false", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("END", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("END", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.LINE_COMMENT, tokenizer.next());
		assertEquals("-- comment2", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.SQL, tokenizer.next());
		assertEquals("", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.COMMENT, tokenizer.next());
		assertEquals("END", tokenizer.getToken().trim());

		assertEquals(SqlTokenizer.EOF, tokenizer.next());
	}

	@Test
	public void testParseSql() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jp/dodododo/dao/sql/parse/nest_if2.sql");
		Reader reader = InputStreamReaderUtil.create(is, "UTF-8");
		String sql = ReaderUtil.readText(reader);
		SqlParser parser = new SqlParser(sql);
		parser.parse();
		// success
	}

}
