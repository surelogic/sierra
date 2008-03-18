package com.surelogic.sierra.gwt.client.util;

public final class LangUtil {

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

}
