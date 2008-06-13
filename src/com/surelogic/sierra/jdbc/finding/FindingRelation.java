package com.surelogic.sierra.jdbc.finding;

import com.surelogic.common.jdbc.*;

public class FindingRelation {
	private final Long f_parentId;
	private final Long f_childId;
	private final Long f_project;
	private final String f_relationType;
	private final String f_status;
	
	private FindingRelation(Row r) {
		f_parentId = r.nextLong();
		f_childId = r.nextLong();
		f_project = r.nextLong();
		f_relationType = r.nextString();
		f_status = r.nextString();
	}
	
	static class Handler implements RowHandler<FindingRelation> {
		public FindingRelation handle(Row r) {
			return new FindingRelation(r);
		}		
	}

	public Long getParentId() {
		return f_parentId;
	}

	public Long getChildId() {
		return f_childId;
	}

	public Long getProjectId() {
		return f_project;
	}

	public String getRelationType() {
		return f_relationType;
	}

	public String getStatus() {
		return f_status;
	}
}
