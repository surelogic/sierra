package com.surelogic.sierra.jdbc.server;

import java.util.UUID;

import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Result;
import com.surelogic.sierra.jdbc.ResultHandler;
import com.surelogic.sierra.jdbc.Row;

public abstract class DefinitionalDAO {

	protected final Query q;

	public DefinitionalDAO(Query q) {
		this.q = q;
	}

	protected String createLocalDefinition(DefinitionType type, long revision) {
		final String uuid = UUID.randomUUID().toString();
		q.prepared("Definitions.insertLocalDefinition").call(uuid, type.name(),
				revision);
		return uuid;
	}

	protected boolean checkAndUpdateRevision(String uuid, long old,
			long revision) {
		q.prepared("Definitions.updateRevision").call(revision, uuid, old);
		final Long rev = q.prepared("Definitions.selectRevision",
				new ResultHandler<Long>() {
					public Long handle(Result r) {
						for (Row row : r) {
							return row.nextLong();
						}
						return null;
					}
				}).call(uuid);
		return (rev != null) && (rev == revision);
	}

	/**
	 * 
	 * @param uuid
	 * @param revision
	 */
	public void updateRevision(String uuid, long revision) {

	}

}
