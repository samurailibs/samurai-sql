package jp.dodododo.dao.object;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jp.dodododo.dao.object.target.Target;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectDescTest {

	@Test
	public void testNoPublicClass() throws Exception {
		ObjectDesc<Target> objectDesc = ObjectDescFactory.getObjectDesc(Target.class);
		List<PropertyDesc> propertyDescs = objectDesc.getPropertyDescs();
		for (PropertyDesc propertyDesc : propertyDescs) {
			Object value = propertyDesc.getValue(new Target());
			ObjectDesc<?> od= ObjectDescFactory.getObjectDesc(value);
			List<PropertyDesc> propertyDescs2 = od.getPropertyDescs();
			for (PropertyDesc propertyDesc2 : propertyDescs2) {
				System.out.println("" + propertyDesc2.getValue(value));
			}
		}
	}

	@Test
	public void testGetTargetClass() {
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		assertEquals(Bean.class, objectDesc.getTargetClass());
	}

	@Test
	public void testHasPropertyDesc() {
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		assertTrue(objectDesc.hasPropertyDesc("foo"));
		assertTrue(objectDesc.hasPropertyDesc("bar"));
		assertFalse(objectDesc.hasPropertyDesc("baz"));
	}

	@Test
	public void testGetPropertyDescString() {
		Bean bean = new Bean();
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		PropertyDesc fooDesc = objectDesc.getPropertyDesc("foo");
		PropertyDesc barDesc = objectDesc.getPropertyDesc("bar");
		assertEquals(0, (int)fooDesc.getValue(bean));
		assertEquals("", barDesc.getValue(bean));
		fooDesc.setValue(bean, 10);
		barDesc.setValue(bean, "val");
		assertEquals(10, (int)fooDesc.getValue(bean));
		assertEquals("val", barDesc.getValue(bean));
	}

	@Test
	public void testGetPropertyDescStringPublicField() {
		PublicFieldBean bean = new PublicFieldBean();
		ObjectDesc<PublicFieldBean> objectDesc = ObjectDescFactory.getObjectDesc(PublicFieldBean.class);
		PropertyDesc fooDesc = objectDesc.getPropertyDesc("foo");
		PropertyDesc barDesc = objectDesc.getPropertyDesc("bar");
		PropertyDesc bazDesc = objectDesc.getPropertyDesc("baz");
		assertEquals(0, (int)fooDesc.getValue(bean));
		assertEquals("", barDesc.getValue(bean));
		assertEquals(0, (int)bazDesc.getValue(bean));
		fooDesc.setValue(bean, 10);
		barDesc.setValue(bean, "val");
		bazDesc.setValue(bean, 10);
		assertEquals(10, (int)fooDesc.getValue(bean));
		assertEquals("val", barDesc.getValue(bean));
		assertEquals(10, (int)bazDesc.getValue(bean));
	}

	@Test
	public void testGetPropertyDescNoException() {
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		assertNotNull(objectDesc.getPropertyDescNoException("foo"));
		assertNotNull(objectDesc.getPropertyDescNoException("bar"));
		assertNull(objectDesc.getPropertyDescNoException("baz"));
	}

	@Test
	public void testGetPropertyDescSize() {
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		assertEquals(2, objectDesc.getPropertyDescSize());
	}

	@Test
	public void testGetPropertyDescInt() {
		ObjectDesc<Bean> objectDesc = ObjectDescFactory.getObjectDesc(Bean.class);
		if("foo".equals(objectDesc.getPropertyDesc(0).getPropertyName())) {
			assertEquals("foo", objectDesc.getPropertyDesc(0).getPropertyName());
			assertEquals("bar", objectDesc.getPropertyDesc(1).getPropertyName());
		} else {
			assertEquals("bar", objectDesc.getPropertyDesc(0).getPropertyName());
			assertEquals("foo", objectDesc.getPropertyDesc(1).getPropertyName());
		}
	}

	public class Bean implements Serializable {
		private static final long serialVersionUID = 5547370364076816042L;

		private int foo;

		private String bar = "";

		public int getFoo() {
			return foo;
		}

		public void setFoo(int foo) {
			this.foo = foo;
		}

			public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}
	}

	public class PublicFieldBean implements Serializable {
		private static final long serialVersionUID = 558934486764281801L;

		public int foo;

		public String bar = "";

		public String baz = "";

		public int baz2;

		public int getBaz() {
			return baz2;
		}

		public void setBaz(int baz) {
			this.baz2 = baz;
		}

	}

	@Test
	public void testToMapWithFields() throws Exception {
		ToMapTarget target = new ToMapTarget();
		ObjectDesc<ToMapTarget> objectDesc = ObjectDescFactory.getObjectDesc(target);
		Map<String, Object> map = objectDesc.toMapWithFields(target);
		assertEquals(1, map.get("a"));
		assertEquals(2, map.get("b"));
	}

	public class ToMapTarget {
		@SuppressWarnings("unused")
		private int a = 1;
		@SuppressWarnings("unused")
		private int b = 2;

		public int getA() {
			return 10;
		}
	}
}
