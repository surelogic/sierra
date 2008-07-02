package com.surelogic.sierra.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class used to format dates for display
 * 
 * @author nathan
 * 
 */
public final class Dates {

	private Dates() {
		// Not instantiable
	}

	public static String format(final Date date) {
		if (date == null) {
			return "";
		} else {
			final DateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm a");
			return format.format(date);
		}
	}

}
