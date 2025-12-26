package jp.dodododo.sql.sql.node;

import jp.dodododo.sql.annotation.Dialects;
import jp.dodododo.sql.context.CommandContext;
import jp.dodododo.sql.dialect.Dialect;
import jp.dodododo.sql.dialect.HSQL;
import jp.dodododo.sql.util.SqlUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractNodeTest {

	@Test
	public void testIsEnclosedBySingleQuot() {
		AbstractNode node = new AbstractNode() {
		    @Override
			public void accept(CommandContext ctx) {
			}
		};

		assertFalse(node.isEnclosedBySingleQuot(";", ";"));
		assertTrue(node.isEnclosedBySingleQuot(" ';' ", ";"));
		assertTrue(node.isEnclosedBySingleQuot(" ';;;;;;;;;;;' ", ";"));
		assertTrue(node.isEnclosedBySingleQuot(" ' ; ;  ;  ; ; ; ; ; ;     ;;' ", ";"));
		assertFalse(node.isEnclosedBySingleQuot(" ';'; ", ";"));
		assertFalse(node.isEnclosedBySingleQuot(" ;';' ", ";"));

		try {
			node.isEnclosedBySingleQuot(" ' ", ";");
			fail();
		} catch (IllegalArgumentException success) {
		}
	}

	@Test
	public void testGetValueOgnl() {
		AbstractNode node = new AbstractNode() {
		    @Override
			public void accept(CommandContext ctx) {
			}
		};

		String expression = "foo.bar.baz";
		String[] names = new String[] { "foo", "bar", "baz" };
		Object root = SqlUtil.args("foo", new Foo());
		Dialect dialect = null;
		assertTrue(node.getValue(expression, names, root, dialect) instanceof Baz);

		expression = "foo.bar.baz.foo";
		names = new String[] { "foo", "bar", "baz", "foo" };
		root = SqlUtil.args("foo", new Foo());
		assertTrue(node.getValue(expression, names, root, dialect) instanceof Foo);
	}

	@Test
	public void testGetValueDialect() {
		AbstractNode node = new AbstractNode() {
		    @Override
			public void accept(CommandContext ctx) {
			}
		};

		String expression = "foo.d";
		String[] names = new String[] { "foo", "d" };
		Object root = SqlUtil.args("foo", new Foo());
		Dialect dialect = new HSQL();
		assertEquals("fooVal", node.getValue(expression, names, root, dialect));

		expression = "foo.bar.d";
		names = new String[] { "foo", "bar", "d" };
		root = SqlUtil.args("foo", new Foo());
		assertEquals("barVal", node.getValue(expression, names, root, dialect));

		expression = "foo.bar.baz.d";
		names = new String[] { "foo", "bar", "baz", "d" };
		root = SqlUtil.args("foo", new Foo());
		assertEquals("bazVal", node.getValue(expression, names, root, dialect));
	}

	public static class Foo {
		public Bar bar = new Bar();

		@Dialects(dialect = HSQL.class, value = "fooVal")
		public String d;
	}

	public static class Bar {
		public Baz getBaz() {
			return new Baz();
		}

		@Dialects(dialect = HSQL.class, value = "barVal")
		public String d;
	}

	public static class Baz {
		public Foo foo = new Foo();

		@Dialects(dialect = HSQL.class, value = "bazVal")
		public String getD() {
			return null;
		}
	}
}
