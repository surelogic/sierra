package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.util.List;

import com.surelogic.common.jdbc.QB;
import com.surelogic.sierra.jdbc.qrecord.BaseMapper;
import com.surelogic.sierra.jdbc.qrecord.Record;
import com.surelogic.sierra.jdbc.qrecord.RecordMapper;
import com.surelogic.sierra.jdbc.qrecord.UpdatableRecord;
import com.surelogic.sierra.jdbc.qrecord.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.qrecord.UpdateRecordMapper;

public class ConnectionQuery implements Query {

	private final Connection conn;

	public ConnectionQuery(Connection conn) {
		this.conn = conn;
	}

	public Queryable<Void> prepared(String key) {
		return new QueryablePreparedStatement<Void>(conn, key,
				new EmptyResultHandler());
	}

	public <T> Queryable<List<T>> prepared(String key, RowHandler<T> rh) {
		return new QueryablePreparedStatement<List<T>>(conn, key,
				new ResultRowHandler<T>(rh));
	}

	public <T> Queryable<T> prepared(String key, ResultHandler<T> rh) {
		return new QueryablePreparedStatement<T>(conn, key, rh);
	}

	public Queryable<Void> statement(String key) {
		return new QueryableStatement<Void>(conn, key, new EmptyResultHandler());

	}

	public <T> Queryable<T> statement(String key, ResultHandler<T> rh) {
		return new QueryableStatement<T>(conn, key, rh);
	}

	public <T> Queryable<List<T>> statement(String key, RowHandler<T> rh) {
		return new QueryableStatement<List<T>>(conn, key,
				new ResultRowHandler<T>(rh));
	}

	public <T extends Record<?>> T record(Class<T> record) {
		if (!Record.class.isAssignableFrom(record)) {
			throw new IllegalArgumentException(
					"Parameter must implement Record");
		}
		final String keyBase = record.getSimpleName();
		final String select = QB.get(keyBase + ".select");
		final String delete = QB.get(keyBase + ".delete");
		final String insert = QB.get(keyBase + ".insert");
		final boolean generated = Boolean.valueOf(QB
				.get(keyBase + ".generated"));
		try {
			if (UpdatableRecord.class.isAssignableFrom(record)) {
				final String update = QB.get(keyBase + ".update");
				return record.getConstructor(UpdateRecordMapper.class)
						.newInstance(
								new UpdateBaseMapper(conn, insert, select,
										delete, update, generated));
			} else {
				return record.getConstructor(RecordMapper.class)
						.newInstance(
								new BaseMapper(conn, insert, select, delete,
										generated));
			}
		} catch (final Exception e) {
			throw new IllegalStateException("Record " + record
					+ " was not instantiable", e);
		}
	}

	public Connection getConnection() {
		return conn;
	}
}
