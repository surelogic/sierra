package com.surelogic.sierra.jdbc.server;

import java.util.UUID;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;

public final class Definitions {

	protected final Query q;

	public Definitions(Query q) {
		this.q = q;
	}

	public String createLocalDefinition(DefinitionType type, long revision) {
		final String uuid = UUID.randomUUID().toString();
		q.prepared("Definitions.insertLocalDefinition").call(uuid, type.name(),
				revision);
		return uuid;
	}

	public boolean checkAndUpdateRevision(String uuid, long old, long revision) {
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
