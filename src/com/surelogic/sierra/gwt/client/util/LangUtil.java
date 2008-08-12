package com.surelogic.sierra.gwt.client.util;

import java.util.Collection;

public final class LangUtil {

	/**
	 * Compares two objects to determine equality of type and value.
	 * 
	 * @param o1
	 *            first object to compare
	 * @param o2
	 *            second object to compare
	 * @return true if the objects are of the same type and equal in value
	 */
	public static boolean equals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		return o1 != null && o1.equals(o2);
	}

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		return s1 != null && s1.equalsIgnoreCase(s2);
	}

	public static boolean containsIgnoreCase(String str, String part) {
		if (str == null || part == null) {
			return false;
		}
		return str.toLowerCase().contains(part.toLowerCase());
	}

	public static boolean isEmpty(String value) {
		return value == null ? true : value.equals("");
	}

	public static boolean notEmpty(String value) {
		return value == null ? false : !"".equals(value.trim());
	}

	public static String emptyZeroString(int i) {
		return i == 0 ? "" : Integer.toString(i);
	}

	public static <T, U extends Collection<T>> U copy(U source, U destination) {
		if (source == null) {
			return null;
		}
		destination.addAll(source);
		return destination;
	}
}
