package com.surelogic.sierra.jdbc;

import java.sql.Connection;

import com.surelogic.common.jdbc.QB;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.Record;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.UpdatableRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class RecordFactory {

	private final Connection conn;

	public RecordFactory(Connection conn) {
		this.conn = conn;
	}

	<T> T createRecord(Class<T> record) {
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

}
