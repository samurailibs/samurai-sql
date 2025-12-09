package jp.dodododo.sql.lazyloading;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

public class ProxyFactoryTest extends ProxyFactory {

	@Test
	public void testCreateClassOfT() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			SecurityException, InvocationTargetException, NoSuchMethodException {
		ProxyFactory factory = new ProxyFactory();

		NotAuto notAuto = factory.create(NotAuto.class);
		assertEquals(0, notAuto.i);
	}

	@Test
	public void testCreateClassOfTConstructorOfTObjectArray() throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		ProxyFactory factory = new ProxyFactory();

		NotAuto notAuto = factory.create(NotAuto.class, NotAuto.class.getConstructor(Integer.TYPE), 100);
		assertEquals(100, notAuto.i);
	}

	@Test
	public void testNewInstance() {
		ProxyFactory factory1 = ProxyFactory.newInstance(NotAuto.class);
		assertEquals(ProxyFactory.class, factory1.getClass());

		try {
			ProxyFactory.newInstance(Auto.class);
			fail();
		} catch (Exception success) {
		}
	}

	public static class NotAuto implements LazyLoadingProxy<NotAuto> {

		public int i;

		public NotAuto() {

		}

		public NotAuto(int i) {
			this.i = i;
		}

	    @Override
		public NotAuto lazyLoad() {
			return null;
		}

	    @Override
		public NotAuto real() {
			return null;
		}

	    @Override
		public void setReal(NotAuto real) {
		}

	}

	public static class Auto implements AutoLazyLoadingProxy<Auto> {

	    @Override
		public Auto lazyLoad() {
			return null;
		}

	    @Override
		public Auto real() {
			return null;
		}

	    @Override
		public void setReal(Auto real) {
		}
	}

}
