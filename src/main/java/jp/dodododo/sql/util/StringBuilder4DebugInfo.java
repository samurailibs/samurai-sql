package jp.dodododo.sql.util;

import jp.dodododo.sql.lazyloading.LazyLoadingUtil;

public class StringBuilder4DebugInfo {

	protected StringBuilder delegator;

	public StringBuilder4DebugInfo() {
		delegator = new StringBuilder();
	}

	public StringBuilder4DebugInfo(CharSequence sequence) {
		delegator = new StringBuilder(sequence);
	}

	public StringBuilder4DebugInfo(int capacity) {
		delegator = new StringBuilder(capacity);
	}

	public int length() {
		return delegator.length();
	}

	public int capacity() {
		return delegator.capacity();
	}

	@Override
	public int hashCode() {
		return delegator.hashCode();
	}

	public void ensureCapacity(int minimumCapacity) {
		delegator.ensureCapacity(minimumCapacity);
	}

	public void trimToSize() {
		delegator.trimToSize();
	}

	@Override
	public boolean equals(Object obj) {
		return delegator.equals(obj);
	}

	public void setLength(int newLength) {
		delegator.setLength(newLength);
	}

	public StringBuilder append(Object obj) {
		try {
			if (obj instanceof Class) {
				return delegator.append(obj);
			} else if (LazyLoadingUtil.isProxy(obj) == true) {
				return delegator.append("?");
			} else {
				return delegator.append(obj);
			}
		} catch (Throwable ignore) {
			return delegator.append("?");
		}
	}

	public StringBuilder append(String str) {
		return delegator.append(str);
	}

	public StringBuilder append(StringBuffer sb) {
		return delegator.append(sb);
	}

	public char charAt(int index) {
		return delegator.charAt(index);
	}

	public StringBuilder append(CharSequence s) {
		return delegator.append(s);
	}

	public StringBuilder append(CharSequence s, int start, int end) {
		return delegator.append(s, start, end);
	}

	public int codePointAt(int index) {
		return delegator.codePointAt(index);
	}

	public StringBuilder append(char[] str) {
		return delegator.append(str);
	}

	public StringBuilder append(char[] str, int offset, int len) {
		return delegator.append(str, offset, len);
	}

	public StringBuilder append(boolean b) {
		return delegator.append(b);
	}

	public StringBuilder append(char c) {
		return delegator.append(c);
	}

	public StringBuilder append(int i) {
		return delegator.append(i);
	}

	public StringBuilder append(long lng) {
		return delegator.append(lng);
	}

	public StringBuilder append(float f) {
		return delegator.append(f);
	}

	public StringBuilder append(double d) {
		return delegator.append(d);
	}

	public StringBuilder appendCodePoint(int codePoint) {
		return delegator.appendCodePoint(codePoint);
	}

	public StringBuilder delete(int start, int end) {
		return delegator.delete(start, end);
	}

	public int codePointBefore(int index) {
		return delegator.codePointBefore(index);
	}

	public StringBuilder deleteCharAt(int index) {
		return delegator.deleteCharAt(index);
	}

	public StringBuilder replace(int start, int end, String str) {
		return delegator.replace(start, end, str);
	}

	public StringBuilder insert(int index, char[] str, int offset, int len) {
		return delegator.insert(index, str, offset, len);
	}

	public StringBuilder insert(int offset, Object obj) {
		return delegator.insert(offset, obj);
	}

	public StringBuilder insert(int offset, String str) {
		return delegator.insert(offset, str);
	}

	public StringBuilder insert(int offset, char[] str) {
		return delegator.insert(offset, str);
	}

	public StringBuilder insert(int dstOffset, CharSequence s) {
		return delegator.insert(dstOffset, s);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return delegator.codePointCount(beginIndex, endIndex);
	}

	public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
		return delegator.insert(dstOffset, s, start, end);
	}

	public StringBuilder insert(int offset, boolean b) {
		return delegator.insert(offset, b);
	}

	public StringBuilder insert(int offset, char c) {
		return delegator.insert(offset, c);
	}

	public StringBuilder insert(int offset, int i) {
		return delegator.insert(offset, i);
	}

	public StringBuilder insert(int offset, long l) {
		return delegator.insert(offset, l);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return delegator.offsetByCodePoints(index, codePointOffset);
	}

	public StringBuilder insert(int offset, float f) {
		return delegator.insert(offset, f);
	}

	public StringBuilder insert(int offset, double d) {
		return delegator.insert(offset, d);
	}

	public int indexOf(String str) {
		return delegator.indexOf(str);
	}

	public int indexOf(String str, int fromIndex) {
		return delegator.indexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return delegator.lastIndexOf(str);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return delegator.lastIndexOf(str, fromIndex);
	}

	public StringBuilder reverse() {
		return delegator.reverse();
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		delegator.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	@Override
	public String toString() {
		return delegator.toString();
	}

	public void setCharAt(int index, char ch) {
		delegator.setCharAt(index, ch);
	}

	public String substring(int start) {
		return delegator.substring(start);
	}

	public CharSequence subSequence(int start, int end) {
		return delegator.subSequence(start, end);
	}

	public String substring(int start, int end) {
		return delegator.substring(start, end);
	}

}
