package jp.dodododo.dao.function;

import static jp.dodododo.dao.unit.UnitTestUtil.*;
import static jp.dodododo.dao.util.DaoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.sql.DataSource;

import jp.dodododo.dao.Dao;
import jp.dodododo.dao.access.AccessMode;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.annotation.Compress;
import jp.dodododo.dao.annotation.Id;
import jp.dodododo.dao.annotation.IdDefSet;
import jp.dodododo.dao.annotation.Property;
import jp.dodododo.dao.annotation.Table;
import jp.dodododo.dao.commons.Bool;
import jp.dodododo.dao.compress.CompressType;
import jp.dodododo.dao.dialect.Dialect;
import jp.dodododo.dao.dialect.DialectManager;
import jp.dodododo.dao.dialect.MySQL;
import jp.dodododo.dao.dialect.sqlite.SQLite;
import jp.dodododo.dao.id.Identity;
import jp.dodododo.dao.id.Sequence;
import jp.dodododo.dao.row.Row;
import jp.dodododo.dao.types.TypeConverter;
import jp.dodododo.dao.unit.DbTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CompressTest {

	@RegisterExtension
	static DbTestExtension dbTestExtension = new DbTestExtension();


	private Dao dao;

	@Test
	public void testNoCompress() throws SQLException {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		NoCompress bean = new NoCompress();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), NoCompress.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
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
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testGZIPCompressAutoUncompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		GZIPCompress bean = new GZIPCompress();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), GZIPCompress.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class GZIPCompress {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.GZIP)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_BEST_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_BEST_COMPRESSION bean = new ZLIB_BEST_COMPRESSION();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_BEST_COMPRESSION.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_BEST_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_BEST_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_BEST_SPEED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_BEST_SPEED bean = new ZLIB_BEST_SPEED();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_BEST_SPEED.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_BEST_SPEED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class), })
		public long id;
		@Compress(compressType = CompressType.ZLIB_BEST_SPEED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFAULT_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_DEFAULT_COMPRESSION bean = new ZLIB_DEFAULT_COMPRESSION();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFAULT_COMPRESSION.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFAULT_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFAULT_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFAULT_STRATEGY() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_DEFAULT_STRATEGY bean = new ZLIB_DEFAULT_STRATEGY();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFAULT_STRATEGY.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFAULT_STRATEGY {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFAULT_STRATEGY)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_DEFLATED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_DEFLATED bean = new ZLIB_DEFLATED();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_DEFLATED.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_DEFLATED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_DEFLATED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_FILTERED() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_FILTERED bean = new ZLIB_FILTERED();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_FILTERED.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_FILTERED {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_FILTERED)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_HUFFMAN_ONLY() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ZLIB_HUFFMAN_ONLY bean = new ZLIB_HUFFMAN_ONLY();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_HUFFMAN_ONLY.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_HUFFMAN_ONLY {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_HUFFMAN_ONLY)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testZLIB_NO_COMPRESSION() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}
		String binaryColumnName = "BINARY"; // TODO change to BIN
		if (dialect instanceof MySQL) {
			 binaryColumnName = "BIN";
		}
		dao = newTestDao(getDataSource());

		ZLIB_NO_COMPRESSION bean = new ZLIB_NO_COMPRESSION();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ZLIB_NO_COMPRESSION.class).get();
		assertEquals("abcdefg", TypeConverter.convert(bean.binary, String.class));
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new InflaterInputStream(record.getInputStream(binaryColumnName)), String.class));
	}

	@Table("BINARY_TABLE")
	public static class ZLIB_NO_COMPRESSION {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.ZLIB_NO_COMPRESSION)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testGZIPCompressNoAutoUncompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		GZIPCompressNoAutoUncompress bean = new GZIPCompressNoAutoUncompress();
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), GZIPCompressNoAutoUncompress.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(bean.binary), String.class));
	}

	@Table("BINARY_TABLE")
	public static class GZIPCompressNoAutoUncompress {
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
		public long id;
		@Compress(compressType = CompressType.GZIP, autoUncompress = false)
		public InputStream binary = new ByteArrayInputStream("abcdefg".getBytes());
	}

	@Test
	public void testConstructorArgHasCompress() throws Exception {
		Dialect dialect = DialectManager.getDialect(getConnection());
		if (dialect instanceof SQLite || dialect instanceof MySQL) {
			return;
		}

		dao = newTestDao(getDataSource());

		ConstructorArgHasCompress bean = new ConstructorArgHasCompress(new ByteArrayInputStream("abcdefg".getBytes()));
		dao.insert(bean);
		bean = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), ConstructorArgHasCompress.class).get();
		Row record = dao.selectOne("select * from BINARY_TABLE where id = /*id*/0", args("id", bean.id), Row.class).get();
		assertEquals("abcdefg", TypeConverter.convert(new GZIPInputStream(record.getInputStream("BINARY")), String.class));
		assertEquals("abcdefg", TypeConverter.convert(bean.getStream(), String.class));
	}

	@Table("BINARY_TABLE")
	public static class ConstructorArgHasCompress {
		@Property
		@Id(value = { @IdDefSet(strategy = Sequence.class, name = "sequence"),
				@IdDefSet(strategy = Identity.class, db = SQLite.class),
				@IdDefSet(strategy = Identity.class, db = MySQL.class) })
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
