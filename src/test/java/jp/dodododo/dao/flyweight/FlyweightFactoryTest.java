package jp.dodododo.dao.flyweight;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlyweightFactoryTest {


	@BeforeEach
	public void tearDown() throws Exception {
		FlyweightFactory.setFactory(FlyweightFactory.DEFAULT_FLYWEIGHT_FACTORY);
	}

	@Test
	public void testFlyweightFactory() {
		FlyweightFactory.setFactory(new FlyweightFactory());

		assertEquals((Short)FlyweightFactory.get(null), (Short)FlyweightFactory.get(null));

		assertSame(FlyweightFactory.get(new Boolean(true)), FlyweightFactory.get(new Boolean(true)));

		assertSame(FlyweightFactory.get(new Character('a')), FlyweightFactory.get(new Character('a')));

		assertSame(FlyweightFactory.get(new String("a")), FlyweightFactory.get(new String("a")));

		assertSame(FlyweightFactory.get(new Byte((byte) 100)), FlyweightFactory.get(new Byte((byte) 100)));
		assertSame(FlyweightFactory.get(new Short((short) 500)), FlyweightFactory.get(new Short((short) 500)));
		assertSame(FlyweightFactory.get(new Integer(500)), FlyweightFactory.get(new Integer(500)));
		assertSame(FlyweightFactory.get(new Long(500)), FlyweightFactory.get(new Long(500)));
		assertSame(FlyweightFactory.get(new Float(500)), FlyweightFactory.get(new Float(500)));
		assertSame(FlyweightFactory.get(new Double(500)), FlyweightFactory.get(new Double(500)));

		assertSame(FlyweightFactory.get(new BigInteger("500")), FlyweightFactory.get(new BigInteger("500")));
		assertSame(FlyweightFactory.get(new BigDecimal("500")), FlyweightFactory.get(new BigDecimal("500")));

		assertSame(FlyweightFactory.get(new Date(100)), FlyweightFactory.get(new Date(100)));
		assertSame(FlyweightFactory.get(new java.sql.Date(100)), FlyweightFactory.get(new java.sql.Date(100)));
		assertSame(FlyweightFactory.get(new java.sql.Timestamp(100)), FlyweightFactory.get(new java.sql.Timestamp(100)));

		assertNotSame(FlyweightFactory.get(new Date(100)), FlyweightFactory.get(new java.sql.Date(100)));
		assertNotSame(FlyweightFactory.get(new Date(100)), FlyweightFactory.get(new java.sql.Timestamp(100)));
		assertNotSame(FlyweightFactory.get(new java.sql.Date(100)), FlyweightFactory.get(new java.sql.Timestamp(100)));

		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(new Date(100));
		calendar2.setTime(new java.sql.Date(100));
		assertSame(FlyweightFactory.get(calendar1), FlyweightFactory.get(calendar2));
	}

	@Test
	public void testNullFlyweightFactory() {
		FlyweightFactory.setFactory(new NullFlyweightFactory());

		assertEquals(FlyweightFactory.get(null), (Short) FlyweightFactory.get(null));

		assertNotSame(FlyweightFactory.get(new Boolean(true)), FlyweightFactory.get(new Boolean(true)));

		assertNotSame(FlyweightFactory.get(new Character('a')), FlyweightFactory.get(new Character('a')));

		assertNotSame(FlyweightFactory.get(new String("a")), FlyweightFactory.get(new String("a")));

		assertNotSame(FlyweightFactory.get(new Byte((byte) 100)), FlyweightFactory.get(new Byte((byte) 100)));
		assertNotSame(FlyweightFactory.get(new Short((short) 500)), FlyweightFactory.get(new Short((short) 500)));
		assertNotSame(FlyweightFactory.get(new Integer(500)), FlyweightFactory.get(new Integer(500)));
		assertNotSame(FlyweightFactory.get(new Long(500)), FlyweightFactory.get(new Long(500)));
		assertNotSame(FlyweightFactory.get(new Float(500)), FlyweightFactory.get(new Float(500)));
		assertNotSame(FlyweightFactory.get(new Double(500)), FlyweightFactory.get(new Double(500)));

		assertNotSame(FlyweightFactory.get(new BigInteger("500")), FlyweightFactory.get(new BigInteger("500")));
		assertNotSame(FlyweightFactory.get(new BigDecimal("500")), FlyweightFactory.get(new BigDecimal("500")));

		assertNotSame(FlyweightFactory.get(new Date(100)), FlyweightFactory.get(new java.sql.Date(100)));

		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(new Date(100));
		calendar2.setTime(new java.sql.Date(100));
		assertNotSame(FlyweightFactory.get(calendar1), FlyweightFactory.get(calendar2));
	}
}
