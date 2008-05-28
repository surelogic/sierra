package com.surelogic.sierra.jdbc.settings;

import java.util.List;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class ServerLocations {

	private final Query q;

	public ServerLocations(Query q) {
		this.q = q;
	}

	public void save(List<SierraServerLocation> locations) {
		q.statement("ServerLocation.deleteLocations").call();
		final Queryable<Void> insertLocation = q
				.prepared("ServerLocation.insertLocation");
		for (final SierraServerLocation l : locations) {
			insertLocation.call(l.getLabel(), l.getProtocol(), l.getHost(), l
					.getPort(), l.getContextPath(), l.getUser());
		}
	}

	public List<SierraServerLocation> fetch() {
		return q.statement("ServerLocation.listLocations",
				new RowHandler<SierraServerLocation>() {
					public SierraServerLocation handle(Row r) {

						return null;
					}
				}).call();
	}
}
