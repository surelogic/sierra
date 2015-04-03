package com.surelogic.sierra.gwt.client.util;

import java.util.Collection;

public final class LangUtil {

	/**
	 * Compares an object against zero or more objects to determine equality of
	 * type and value. This method returns false if only one argument is given.
	 * 
	 * @param o1
	 *            first object to compare
	 * @param oN
	 *            a list of objects to compare against o1
	 * @return true if any of the objects are of the same type and equal in
	 *         value to the first argument
	 */
	public static boolean equals(final Object o1, final Object... oN) {
		for (final Object o : oN) {
			if (o1 == o) {
				return true;
			}
			if (o1 != null && o != null && o1.equals(o)) {
				return true;
			}
		}
		return false;
	}

	public static boolean equalsIgnoreCase(final String s1, final String s2) {
		if (s1 == s2) {
			return true;
		}
		return s1 != null && s1.equalsIgnoreCase(s2);
	}

	public static boolean containsIgnoreCase(final String str, final String part) {
		if (str == null || part == null) {
			return false;
		}
		return str.toLowerCase().contains(part.toLowerCase());
	}

	public static boolean isEmpty(final String value) {
		return value == null ? true : value.equals("");
	}

	public static boolean notEmpty(final String value) {
		return value == null ? false : !"".equals(value.trim());
	}

	public static String emptyZeroString(final int i) {
		return i == 0 ? "" : Integer.toString(i);
	}

	public static <T, U extends Collection<T>> U copy(final U source,
			final U destination) {
		if (source == null) {
			return null;
		}
		destination.addAll(source);
		return destination;
	}
}
