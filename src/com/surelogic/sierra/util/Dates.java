package com.surelogic.sierra.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dates {

	private Dates() {
		// Not instantiable
	}

	public static String format(Date date) {
		if (date == null) {
			return "";
		} else {
			DateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm a");
			return format.format(date);
		}
	}

}
