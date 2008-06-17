package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.StringRowHandler;

public class Projects {

	private final Query q;

	public Projects(Query q) {
		this.q = q;
	}

	public Projects(Connection conn) {
		q = new ConnectionQuery(conn);
	}

	public List<ProjectDO> listProjects() {
		final List<ProjectDO> list = new ArrayList<ProjectDO>();
		for (final String s : q.prepared("Projects.listProjects",
				new StringRowHandler()).call()) {
			list.add(getProject(s));
		}
		return list;
	}

	public ProjectDO getProject(String uuid) {
		final ProjectDO project = new ProjectDO();
		project.setUuid(uuid);
		project.setName(uuid);
		return project;
	}
}
