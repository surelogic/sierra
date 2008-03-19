package com.surelogic.sierra.jdbc;

import java.util.Date;

public interface Row {

	int nextInt();

	Integer nullableInt();

	long nextLong();

	Long nullableLong();

	String nextString();

	Date nextDate();
}
