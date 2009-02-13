package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.settings.ScanFilterRecord;

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
				final long id = r.nextLong();
				final String uuid = r.nextString();
				project.setId(id);
				project.setUuid(uuid);
				project.setName(uuid);
				project.setScanFilter(r.nextString());
				return project;
			}
		}).call();
	}

	/**
	 * Updates the existing project's scan filter. Returns the uuid of the old
	 * scan filter.
	 * 
	 * @param project
	 * @param scanFilterUuid
	 * @return
	 */
	public String updateProjectFilter(final String project,
			final String scanFilterUuid) {
		final String old = q.prepared("ScanFilters.selectByProject",
				SingleRowHandler.from(new StringRowHandler())).call(project);
		q.prepared("Projects.deleteScanFilter").call(project);
		final ScanFilterRecord r = q.record(ScanFilterRecord.class);
		r.setUid(scanFilterUuid);
		if (r.select()) {
			q.prepared("Projects.insertScanFilter").call(project,
					scanFilterUuid);
		}
		return old;
	}

	public String getProjectFilter(final String project) {
		String uuid = q.prepared("ScanFilters.selectByProject",
				SingleRowHandler.from(new StringRowHandler())).call(project);
		if (uuid == null) {
			uuid = q.prepared("ScanFilters.selectDefault",
					new StringResultHandler()).call();
			updateProjectFilter(project, uuid);
		}
		return uuid;
	}

}
