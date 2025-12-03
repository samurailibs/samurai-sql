package jp.dodododo.dao.object;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jp.dodododo.dao.access.AccessMode;
import jp.dodododo.dao.annotation.Property;
import jp.dodododo.dao.exception.FailLazyLoadException;
import jp.dodododo.dao.exception.IllegalPropertyRuntimeException;
import jp.dodododo.dao.exception.PropertyNotFoundRuntimeException;
import jp.dodododo.dao.lazyloading.LazyLoadingProxy;
import jp.dodododo.dao.lazyloading.ProxyFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropertyDescTest {

	@BeforeEach
	public void clear(){
		ObjectDescFactory.clearCache();
	}

	@Test
	public void testReadable() {
		ObjectDesc<Target> objectDesc = ObjectDescFactory.getObjectDesc(Target.class);

		try {
			objectDesc.getPropertyDesc("property0");
			fail();
		} catch (PropertyNotFoundRuntimeException success) {
		}

		PropertyDesc propertyDesc1 = objectDesc.getPropertyDesc("property1");
		PropertyDesc propertyDesc2 = objectDesc.getPropertyDesc("property2");
		PropertyDesc propertyDesc3 = objectDesc.getPropertyDesc("property3");
		PropertyDesc propertyDesc4 = objectDesc.getPropertyDesc("property4");
		PropertyDesc propertyDesc5 = objectDesc.getPropertyDesc("property5");
		PropertyDesc propertyDesc6 = objectDesc.getPropertyDesc("property6");
		PropertyDesc propertyDesc7 = objectDesc.getPropertyDesc("property7");
		PropertyDesc propertyDesc8 = objectDesc.getPropertyDesc("property8");

		assertFalse(propertyDesc1.isReadable());
		assertTrue(propertyDesc2.isReadable());
		assertTrue(propertyDesc3.isReadable());
		assertFalse(propertyDesc4.isReadable());
		assertTrue(propertyDesc5.isReadable());
		assertFalse(propertyDesc6.isReadable());
		assertFalse(propertyDesc7.isReadable());
		assertTrue(propertyDesc8.isReadable());
	}

	@Test
	public void testWriteable() {
		ObjectDesc<Target> objectDesc = ObjectDescFactory.getObjectDesc(Target.class);

		try {
			objectDesc.getPropertyDesc("property0");
			fail();
		} catch (PropertyNotFoundRuntimeException success) {
		}

		PropertyDesc propertyDesc1 = objectDesc.getPropertyDesc("property1");
		PropertyDesc propertyDesc2 = objectDesc.getPropertyDesc("property2");
		PropertyDesc propertyDesc3 = objectDesc.getPropertyDesc("property3");
		PropertyDesc propertyDesc4 = objectDesc.getPropertyDesc("property4");
		PropertyDesc propertyDesc5 = objectDesc.getPropertyDesc("property5");
		PropertyDesc propertyDesc6 = objectDesc.getPropertyDesc("property6");
		PropertyDesc propertyDesc7 = objectDesc.getPropertyDesc("property7");
		PropertyDesc propertyDesc8 = objectDesc.getPropertyDesc("property8");

		assertFalse(propertyDesc1.isWritable());
		assertTrue(propertyDesc2.isWritable());
		assertFalse(propertyDesc3.isWritable());
		assertTrue(propertyDesc4.isWritable());
		assertFalse(propertyDesc5.isWritable());
		assertTrue(propertyDesc6.isWritable());
		assertFalse(propertyDesc7.isWritable());
		assertTrue(propertyDesc8.isWritable());
	}

	@SuppressWarnings("unused")
	public static class Target {
		private List<String> property1;

		public List<String> property2;

		@Property(AccessMode.READ_ONLY)
		private List<String> property3;

		@Property(AccessMode.WRITE_ONLY)
		private List<String> property4;

		private List<String> property5;

		private List<String> property6;

		@Property(AccessMode.IGNORE)
		private List<String> property7;

		@Property()
		private List<String> property8;

		public List<String> getProperty7() {
			return property7;
		}

		public void setProperty7(List<String> property7) {
			this.property7 = property7;
		}

		public List<String> getProperty5() {
			return property5;
		}

		public void setProperty6(List<String> property6) {
			this.property6 = property6;
		}

		public List<String> getProperty8() {
			return property8;
		}

		public void setProperty8(List<String> property8) {
			this.property8 = property8;
		}
	}

	@Test
	public void testAccessProxyFiled() throws Exception {
		ProxyFactory.newInstance(Proxy.class);

		HasProxyField hasProxyField = new HasProxyField();
		assertNull(hasProxyField.proxy.real);

		ObjectDesc<Proxy> objectDesc = ObjectDescFactory.getObjectDesc(Proxy.class);
		PropertyDesc propertyDesc = objectDesc.getPropertyDesc("dummy");
		Object val = propertyDesc.getValue(hasProxyField.proxy);
		assertNotNull(val);
		assertNotNull(hasProxyField.proxy.real);
		assertNotNull(objectDesc.getPropertyDesc("property2").getValue(hasProxyField.proxy));
	}

	@Test
	public void testAccessNullProxyFiled() throws Exception {
		ProxyFactory.newInstance(Proxy.class);

		HasProxyField hasProxyField = new HasNullProxyField();
		assertNull(hasProxyField.proxy.real);

		ObjectDesc<Proxy> objectDesc = ObjectDescFactory.getObjectDesc(Proxy.class);
		PropertyDesc propertyDesc = objectDesc.getPropertyDesc("dummy");
		try {
			@SuppressWarnings("unused")
			Object val = propertyDesc.getValue(hasProxyField.proxy);
			fail();
		} catch (IllegalPropertyRuntimeException e) {
			FailLazyLoadException failLazyLoadException = (FailLazyLoadException) e.getCause();
			String message = failLazyLoadException.getMessage();
			assertTrue(message.contains("real=null"));
			assertTrue(message.contains("dummy=java.lang.Object"));
			assertTrue(message.contains("property1=null"));
			assertTrue(message.contains("property2=null"));
			assertTrue(message.contains("property3=null"));
			assertTrue(message.contains("property4=null"));
			assertTrue(message.contains("property5=null"));
			assertTrue(message.contains("property6=null"));
			assertTrue(message.contains("property7=null"));
			assertTrue(message.contains("property8=null"));
		}
	}

	public static class HasProxyField {
		public Proxy proxy = new Proxy();
	}

	public static class HasNullProxyField extends HasProxyField {
		public HasNullProxyField() {
			proxy = new NullProxy();
		}
	}

	public static class NullProxy extends Proxy {
		@Override
		public Target lazyLoad() {
			return null;
		}
	}

	public static class Proxy extends Target implements LazyLoadingProxy<Target> {
		public Object dummy = new Object();

		public Target real;

	    @Override
		public Target lazyLoad() {
			Target target = new Target();
			target.property2 = new ArrayList<String>();
			return target;
		}

	    @Override
		public Target real() {
			return real;
		}

	    @Override
		public void setReal(Target real) {
			this.real = real;
		}

	}

	@Test
	public void testPropName() throws Exception {
		ObjectDesc<PropNamesTarget> objectDesc = ObjectDescFactory.getObjectDesc(PropNamesTarget.class);
		assertEquals("foo", objectDesc.getPropertyDesc("foo").getPropertyName());
		assertEquals("BAr", objectDesc.getPropertyDesc("bar").getPropertyName());
		assertEquals("BAZ", objectDesc.getPropertyDesc("baz").getPropertyName());
		assertEquals("qUX", objectDesc.getPropertyDesc("qux").getPropertyName());
		assertEquals("quux", objectDesc.getPropertyDesc("Quux").getPropertyName());
	}

	public static class PropNamesTarget {
		public String foo;
		public String BAr;
		public String BAZ;
		public String qUX;
		public String Quux;
	}

	@Test
	public void testIgnoreException() throws Exception {
		ObjectDesc<IgnoreExceptionTarget> objectDesc = ObjectDescFactory.getObjectDesc(IgnoreExceptionTarget.class);

		Object valueA = objectDesc.getPropertyDesc("a").getValue(new IgnoreExceptionTarget());
		assertNull(valueA);

		try {
			@SuppressWarnings("unused")
			Object valueB = objectDesc.getPropertyDesc("b").getValue(new IgnoreExceptionTarget());
			fail();
		} catch (IllegalPropertyRuntimeException success) {
			assertTrue(success.getCause() instanceof NullPointerException);
		}
	}

	public static class IgnoreExceptionTarget {

		@SuppressWarnings("null")
		@Property(ignoreExceptions = { NullPointerException.class })
		public String getA() {
			String s = null;
			return s.toString();
		}

		@SuppressWarnings("null")
		public String getB() {
			String s = null;
			return s.toString();
		}
	}

	@Test
	public void testJava8() {
		OptionalBean bean = new OptionalBean();
		ObjectDesc<OptionalBean> objectDesc = ObjectDescFactory.getObjectDesc(OptionalBean.class);
		PropertyDesc sPropertyDesc = objectDesc.getPropertyDesc("s");
		PropertyDesc iPropertyDesc = objectDesc.getPropertyDesc("i");

		assertEquals(Optional.empty(), sPropertyDesc.getValue(bean));
		assertEquals(Optional.empty(), iPropertyDesc.getValue(bean));

		sPropertyDesc.setValue(bean, "a");
		iPropertyDesc.setValue(bean, 1);
		assertEquals(Optional.of("a"), sPropertyDesc.getValue(bean));
		assertEquals(Optional.of(1), iPropertyDesc.getValue(bean));

		sPropertyDesc.setValue(bean, Optional.of("b"));
		iPropertyDesc.setValue(bean, Optional.of(2));
		assertEquals(Optional.of("b"), sPropertyDesc.getValue(bean));
		assertEquals(Optional.of(2), iPropertyDesc.getValue(bean));

		sPropertyDesc.setValue(bean, 3);
		iPropertyDesc.setValue(bean, "13");
		assertEquals(Optional.of("3"), sPropertyDesc.getValue(bean));
		assertEquals(Optional.of(13), iPropertyDesc.getValue(bean));

		sPropertyDesc.setValue(bean, Optional.of(14));
		iPropertyDesc.setValue(bean, Optional.of("4"));
		assertEquals(Optional.of("14"), sPropertyDesc.getValue(bean));
		assertEquals(Optional.of(4), iPropertyDesc.getValue(bean));

		sPropertyDesc.setValue(bean, null);
		iPropertyDesc.setValue(bean, null);
		assertEquals(Optional.empty(), sPropertyDesc.getValue(bean));
		assertEquals(Optional.empty(), iPropertyDesc.getValue(bean));
	}

	public static class OptionalBean {
		public Optional<String> s;
		public Optional<Integer> i;
	}
}
