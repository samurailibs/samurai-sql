package jp.dodododo.dao.handler.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.dodododo.dao.annotation.Bean;
import jp.dodododo.dao.annotation.Column;
import jp.dodododo.dao.columns.ResultSetColumn;
import jp.dodododo.dao.exception.InstantiationRuntimeException;
import jp.dodododo.dao.util.ClassUtil;

import org.junit.jupiter.api.Test;

public class BeanResultSetHandlerTest {

	@Test
	public void testGetUsableConstructor() {
		Class<Target> targetClass = Target.class;
		List<Constructor<Target>> constructors = ClassUtil.getConstructors(targetClass, Modifier.PUBLIC);
		List<ResultSetColumn> resultSetColumnList = new ArrayList<ResultSetColumn>();
		Constructor<Target> constructor = BeanResultSetHandler.getUsableConstructor(targetClass, constructors, resultSetColumnList, targetClass);
		assertEquals(2, constructor.getParameterTypes().length);
	}

	@Test
	public void testContains() throws Exception {
		List<ResultSetColumn> resultSetColumnList = new ArrayList<>();
		resultSetColumnList.add(new ResultSetColumn("a", 1, 1));
		resultSetColumnList.add(new ResultSetColumn("b", 1, 1));
		assertTrue(BeanResultSetHandler.contains(resultSetColumnList, "a"));
		assertTrue(BeanResultSetHandler.contains(resultSetColumnList, "A"));
		assertTrue(BeanResultSetHandler.contains(resultSetColumnList, "b"));
		assertTrue(BeanResultSetHandler.contains(resultSetColumnList, "B"));
		assertFalse(BeanResultSetHandler.contains(resultSetColumnList, "c"));
		assertFalse(BeanResultSetHandler.contains(resultSetColumnList, "C"));
	}

	@Test
	public void testInterface() {
		try {
			new BeanResultSetHandler(null, Interface.class, null, null, null);
		} catch (InstantiationRuntimeException e) {
			assertEquals("This class is invalid, because jp.dodododo.dao.handler.impl.BeanResultSetHandlerTest$Interface is interface.", e.getMessage());
		}
	}

	@Test
	public void testAbstract() {
		try {
			new BeanResultSetHandler(null, AbstractClass.class, null, null, null);
		} catch (InstantiationRuntimeException e) {
			assertEquals("This class is invalid, because jp.dodododo.dao.handler.impl.BeanResultSetHandlerTest$AbstractClass is abstract.", e.getMessage());
		}
	}

	@Test
	public void testAbstractClassHaveFactoryMethod() throws Exception {
		try {
			BeanResultSetHandler handler = new BeanResultSetHandler(null, AbstractClassHaveFactoryMethod.class, new HashMap<>(), null, null);
			Object row = handler.createRow(null, new ArrayList<>());
			assertTrue(row instanceof AbstractClassHaveFactoryMethod);
		} catch (InstantiationRuntimeException e) {
			fail(e.getMessage());
		}
	}

	public static class Target {
		public Target() {
		}

		public Target(int i) {
		}

		public Target(@Column("i") int i, @Column("j") int j) {
		}

		public Target(int i, @Column("j") int j, int k) {
		}
	}

	public interface Interface {
	}

	public static abstract class AbstractClass {
	}

	@Bean(createMethod = "@jp.dodododo.dao.handler.impl.BeanResultSetHandlerTest$AbstractClassHaveFactoryMethod@create()")
	public static abstract class AbstractClassHaveFactoryMethod {
		static AbstractClassHaveFactoryMethod create() {
			return new AbstractClassHaveFactoryMethod() {
			};
		}
	}
}
