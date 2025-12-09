package jp.dodododo.sql.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HasInitialValueHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -8788760582942614129L;

	protected Factory<V> factory;

	/**
	 * @deprecated use {@link HasInitialValueHashMap.NF} .
	 */
	@Deprecated
	public HasInitialValueHashMap(V value) {
		super();
		if (value == null) {
			throw new IllegalArgumentException();
		} else if (value instanceof Number) {
			this.factory = new NF<V>(value);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public HasInitialValueHashMap(Factory<V> factory) {
		super();
		this.factory = factory;
	}

	public HasInitialValueHashMap(Factory<V> factory, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.factory = factory;
	}

	public HasInitialValueHashMap(Factory<V> factory, int initialCapacity) {
		super(initialCapacity);
		this.factory = factory;
	}

	public HasInitialValueHashMap(Factory<V> factory, Map<? extends K, ? extends V> m) {
		super(m);
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		V val = super.get(key);
		if (val != null) {
			return val;
		}
		if (factory == null) {
			return val;
		}
		V value = factory.create();
		put((K) key, value);
		return value;
	}

	public static interface Factory<V> {
		V create();
	}

	public static class ArrayListFactory<E> implements Factory<List<E>> {
		@Override
		public List<E> create() {
			return new ArrayList<>();
		}
	}

	/**
	 * ArrayListFactory
	 * @param <E>
	 */
	public static class ALF<E> extends ArrayListFactory<E> {
	}

	public static class LinkedListFactory<E> implements Factory<List<E>> {
		@Override
		public List<E> create() {
			return new LinkedList<>();
		}
	}
	/**
	 * LinkedListFactory
	 * @param <E>
	 */
	public static class LLF<E> extends LinkedListFactory<E> {
	}

	public static class HashMapFactory<K, V> implements Factory<Map<K, V>> {
		@Override
		public Map<K, V> create() {
			return new HashMap<>();
		}
	}

	/**
	 * HashMapFactory
	 * @param <K>
	 * @param <V>
	 */
	public static class HMF<K, V> extends HashMapFactory<K, V> {
	}

	public static class TreeMapFactory<K, V> implements Factory<Map<K, V>> {
		@Override
		public Map<K, V> create() {
			return new TreeMap<>();
		}
	}

	/**
	 * TreeMapFactory
	 * @param <K>
	 * @param <V>
	 */
	public static class TMF<K, V> extends TreeMapFactory<K, V> {
	}

	public static class HashSetFactory<E> implements Factory<Set<E>> {
		@Override
		public Set<E> create() {
			return new HashSet<E>();
		}
	}

	/**
	 * HashSetFactory
	 * @param <E>
	 */
	public static class HSF<E> extends HashSetFactory<E> {
	}

	public static class NumberFactory<NUMBER> implements Factory<NUMBER> {
		protected NUMBER num;

		public NumberFactory(NUMBER num) {
			this.num = num;
		}

		@Override
		public NUMBER create() {
			return num;
		}
	}

	/**
	 * NumberFactory
	 *
	 * @param <NUMBER>
	 */
	public static class NF<NUMBER> extends NumberFactory<NUMBER> {
		public NF(NUMBER num) {
			super(num);
		}
	}
}
