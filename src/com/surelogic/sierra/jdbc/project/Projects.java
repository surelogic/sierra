package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;

public final class Projects {

	private final Query q;

	public Projects(final Query q) {
		this.q = q;
	}

	public Projects(final Connection conn) {
		q = new ConnectionQuery(conn);
	}

	public List<ProjectDO> listProjects() {
		return q.prepared("Projects.listProjects", new RowHandler<ProjectDO>() {
			public ProjectDO handle(final Row r) {
				final ProjectDO project = new ProjectDO();
				final String uuid = r.nextString();
				project.setUuid(uuid);
				project.setName(uuid);
				project.setScanFilter(r.nextString());
				return project;
			}
		}).call();
	}

	public void updateProjectFilter(final String project,
			final String scanFilterUuid) {
		q.prepared("Projects.deleteScanFilter").call(project);
		q.prepared("Projects.insertScanFilter").call(project, scanFilterUuid);
	}

}
