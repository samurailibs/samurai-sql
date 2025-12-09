package jp.dodododo.sql.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Ignore UpperCase,LowerCase,CamelCase and SnakeCase.
 *
 * <br>
 *
 * UpperCase,LowerCase,CamelCase,SnakeCaseを無視。
 *
 * @author Satoshi Kimura
 *
 */
public class CaseInsensitiveSet extends HashSet<String> {

	private static final long serialVersionUID = -8728602037296874375L;

	public static final Set<String> EMPTY = Collections.unmodifiableSet(new CaseInsensitiveSet(0){
		private static final long serialVersionUID = 1L;

		@Override public final boolean contains(Object o) {return false;}
		@Override public final int size() {return 0;}
		@Override public final boolean isEmpty() {return true;}
	});

	private final CaseInsensitiveMap<Object> map;

	public CaseInsensitiveSet() {
		map = new CaseInsensitiveMap<>();
	}

	public CaseInsensitiveSet(String... addValues) {
		this();
		for (String value : addValues) {
			add(value);
		}
	}

	public CaseInsensitiveSet(Collection<? extends String> collection) {
		int size = collection.size();
		map = new CaseInsensitiveMap<>(size);
		addAll(collection);
	}

	public CaseInsensitiveSet(int initialCapacity, float loadFactor) {
		map = new CaseInsensitiveMap<>(initialCapacity, loadFactor);
	}

	public CaseInsensitiveSet(int initialCapacity) {
		map = new CaseInsensitiveMap<>(initialCapacity);
	}

	@Override
	public boolean add(String e) {
		Object o = map.put(e, null);
		return o != null;
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		c.forEach(string -> map.put(string, null));
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Object clone() {
		return new CaseInsensitiveSet(map.keySet());
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object object : c) {
			if (contains(object) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		return map.keySet().equals(o);
	}

	@Override
	public int hashCode() {
		return map.keySet().hashCode();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<String> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		Object remove = map.remove(o);
		return remove != null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		c.forEach(object -> remove(object));
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		c.stream().filter(object -> map.containsKey(object) == false).forEach(object -> map.remove(object));
		return true;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray(new String[map.size()]);
	}

	@Override
	public String toString() {
		return map.keySet().toString();
	}

	public String getFirstElement() {
		List<String> tmp = new ArrayList<>(map.size());
		tmp.addAll(map.keySet());
		return tmp.get(0);
	}

}
