package com.surelogic.sierra.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class ResultSetResult implements Result {

	private final ResultSet set;
	private final ResultSetRow row;

	ResultSetResult(ResultSet set) {
		this.set = set;
		this.row = new ResultSetRow(set);
	}

	public Iterator<Row> iterator() {
		return new Iterator<Row>() {

			private boolean hasNexted = false;
			private boolean hasNext = true;

			public boolean hasNext() {
				if (!hasNexted) {
					hasNexted = true;
					try {
						hasNext = set.next();
					} catch (SQLException e) {
						throw new ResultSetException(e);
					}
				}
				return hasNext;
			}

			public Row next() {
				if (!hasNexted) {
					try {
						if (!set.next()) {
							throw new NoSuchElementException();
						}
					} catch (SQLException e) {
						throw new ResultSetException(e);
					}
				} else if (!hasNext) {
					throw new NoSuchElementException();
				}
				hasNexted = false;
				row.clear();
				return row;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

	void close() throws SQLException {
		set.close();
	}
}
