package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surelogic.common.jdbc.QB;
import com.surelogic.sierra.jdbc.qrecord.BaseMapper;
import com.surelogic.sierra.jdbc.qrecord.Record;
import com.surelogic.sierra.jdbc.qrecord.RecordMapper;
import com.surelogic.sierra.jdbc.qrecord.UpdatableRecord;
import com.surelogic.sierra.jdbc.qrecord.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.qrecord.UpdateRecordMapper;

/**
 * Implementation of {@link Query} using a {@link Connection}. ConnectionQuery
 * produces JDBC statements and prepared statements. The statements are stored
 * in the query bank accessible by {@link QB}. See
 * {@link QueryablePreparedStatement} and {@link QueryableStatement} for more on
 * the behavior of this class.
 * 
 * 
 * 
 * @author nathan
 * 
 */
// TODO: Change the Record pattern such that LazyPreparedStatement is no longer
// necessary.
public class ConnectionQuery implements Query {

	private final Connection conn;

	private final Map<String, PreparedStatement> map;

	public ConnectionQuery(Connection conn) {
		this.conn = conn;
		map = new HashMap<String, PreparedStatement>();
	}

	public Queryable<Void> prepared(String key) {
		return new QueryablePreparedStatement<Void>(findOrCreate(key),
				new EmptyResultHandler());
	}

	public <T> Queryable<List<T>> prepared(String key, RowHandler<T> rh) {
		return new QueryablePreparedStatement<List<T>>(findOrCreate(key),
				new ResultRowHandler<T>(rh));
	}

	public <T> Queryable<T> prepared(String key, ResultHandler<T> rh) {
		return new QueryablePreparedStatement<T>(findOrCreate(key), rh);
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

	/**
	 * Returns a record backed by prepared statements associated w/ this JDBC
	 * Connection. If the name of the record is {@code foo}, then the following
	 * keys will be looked up:
	 * 
	 * <pre>
	 * foo.select            - The select statement by natural key
	 * foo.delete            - The delete statement by primary key
	 * foo.insert            - The insert statement
	 * foo.update (optional) - An update statement by primary key
	 * foo.generated         - Whether or not this record has an auto-generated primary key
	 * </pre>
	 */
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

	private PreparedStatement findOrCreate(String key) {
		PreparedStatement st = map.get(key);
		if (st == null) {
			try {
				st = conn.prepareStatement(QB.get(key));
				map.put(key, st);
			} catch (final SQLException e) {
				throw new StatementException(e);
			}
		}
		return st;
	}
}
