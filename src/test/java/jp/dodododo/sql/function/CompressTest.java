package jp.dodododo.sql.function;

import jp.dodododo.sql.SamuraiSqlClient;
import jp.dodododo.sql.annotation.*;
import jp.dodododo.sql.compress.CompressType;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.DialectManager;
import jp.dodododo.sql.dialect.sqlite.SQLite;
import jp.dodododo.sql.id.Identity;
import jp.dodododo.sql.id.Sequence;
import jp.dodododo.sql.row.Row;
import jp.dodododo.sql.types.TypeConverter;
import jp.dodododo.sql.unit.DbTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static jp.dodododo.sql.unit.UnitTestUtil.newTestClient;
import static jp.dodododo.sql.util.SqlUtil.args;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompressTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();


	private SamuraiSqlClient client;

	@Test
	public void testNoCompress() throws SQLException {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {

			return;
		}

		client = newTestClient(getDataSource());

		NoCompress bean = new NoCompress();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), NoCompress.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(record.getInputStream("BINARY"), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	private DataSource getDataSource() {
		return dbTestExtension.getDataSource();
	}

	private Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}

	@Table("BINARY_TABLE")
	public static class NoCompress {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testGZIPCompressAutoUncompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		GZIPCompress bean = new GZIPCompress();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), GZIPCompress.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class GZIPCompress {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.GZIP)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_BEST_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_BEST_COMPRESSION bean = new ZLIB_BEST_COMPRESSION();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_BEST_COMPRESSION.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_BEST_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_BEST_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_BEST_SPEED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_BEST_SPEED bean = new ZLIB_BEST_SPEED();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_BEST_SPEED.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_BEST_SPEED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_BEST_SPEED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFAULT_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_DEFAULT_COMPRESSION bean = new ZLIB_DEFAULT_COMPRESSION();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFAULT_COMPRESSION.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFAULT_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFAULT_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFAULT_STRATEGY() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_DEFAULT_STRATEGY bean = new ZLIB_DEFAULT_STRATEGY();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFAULT_STRATEGY.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFAULT_STRATEGY {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFAULT_STRATEGY)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFLATED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_DEFLATED bean = new ZLIB_DEFLATED();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFLATED.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFLATED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFLATED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_FILTERED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_FILTERED bean = new ZLIB_FILTERED();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_FILTERED.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_FILTERED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_FILTERED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_HUFFMAN_ONLY() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ZLIB_HUFFMAN_ONLY bean = new ZLIB_HUFFMAN_ONLY();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_HUFFMAN_ONLY.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_HUFFMAN_ONLY {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_HUFFMAN_ONLY)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_NO_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}
		String binaryColumnName = "BINARY"; // TODO change to BIN
//		if (dialect instanceof MySQL) {
//			 binaryColumnName = "BIN";
//		}
		client = newTestClient(getDataSource());

		ZLIB_NO_COMPRESSION bean = new ZLIB_NO_COMPRESSION();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_NO_COMPRESSION.class).get();
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream(binaryColumnName)), String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_NO_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.ZLIB_NO_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testGZIPCompressNoAutoUncompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		GZIPCompressNoAutoUncompress bean = new GZIPCompressNoAutoUncompress();
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), GZIPCompressNoAutoUncompress.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(bean.binary), String.class));
	}

	@Table("BINARY_TABLE")
	public static class GZIPCompressNoAutoUncompress {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		public long id;
		@Compress(compressType = CompressType.GZIP, autoUncompress = false)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testConstructorArgHasCompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		// if (dialect instanceof SQLite || dialect instanceof MySQL) {
		if (dialect instanceof SQLite) {
			return;
		}

		client = newTestClient(getDataSource());

		ConstructorArgHasCompress bean = new ConstructorArgHasCompress(new ByteArrayInputStream("abcdefg".getBytes()));
		client.insert(bean);
		bean = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ConstructorArgHasCompress.class).get();
		Row record = client.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.getStream(), String.class));
	}

	@Table("BINARY_TABLE")
	public static class ConstructorArgHasCompress {
		@Property
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class)})
		private int id;

		@Compress
		@Column("BINARY")
		private InputStream stream;

		public ConstructorArgHasCompress(InputStream stream) {
			this.stream = stream;
		}
		public ConstructorArgHasCompress(
				@Column("ID") int id,
				@Column("BINARY") @Compress InputStream stream) {
			this.id = id;
			this.stream = stream;
		}

		public int getId() {
			return id;
		}

		public InputStream getStream() {
			return stream;
		}
	}
}
