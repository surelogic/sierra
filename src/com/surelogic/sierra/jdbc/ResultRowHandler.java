package com.surelogic.sierra.jdbc;

import java.util.ArrayList;
import java.util.List;

class ResultRowHandler<T> implements ResultHandler<List<T>> {

	private final RowHandler<T> rh;

	ResultRowHandler(RowHandler<T> rh) {
		this.rh = rh;
	}

	public List<T> handle(Result result) {
		final List<T> list = new ArrayList<T>();
		for (final Row row : result) {
			list.add(rh.handle(row));
		}
		return list;
	}

}
