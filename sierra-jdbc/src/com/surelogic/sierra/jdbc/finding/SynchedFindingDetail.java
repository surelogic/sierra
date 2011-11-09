package com.surelogic.sierra.jdbc.finding;

import java.util.Date;
import java.util.List;

import com.surelogic.sierra.tool.message.Importance;

public class SynchedFindingDetail {

	private final FindingOverview overview;
	private final List<AuditDetail> audits;
	private final List<AuditDetail> commits;

	SynchedFindingDetail(final FindingOverview overview,
			final List<AuditDetail> audits, final List<AuditDetail> commits) {
		this.overview = overview;
		this.audits = audits;
		this.commits = commits;
	}

	public List<AuditDetail> getAudits() {
		return audits;
	}

	public List<AuditDetail> getCommits() {
		return commits;
	}

	public String getClassName() {
		return overview.getClassName();
	}

	public String getCompilation() {
		return overview.getCompilation();
	}

	public long getFindingId() {
		return overview.getFindingId();
	}

	public String getFindingType() {
		return overview.getFindingType();
	}

	public Importance getImportance() {
		return overview.getImportance();
	}

	public Date getLastChanged() {
		return overview.getLastChanged();
	}

	public int getLineOfCode() {
		return overview.getLineOfCode();
	}

	public int getNumberOfArtifacts() {
		return overview.getNumberOfArtifacts();
	}

	public int getNumberOfAudits() {
		return overview.getNumberOfAudits();
	}

	public String getPackageName() {
		return overview.getPackageName();
	}

	public String getProject() {
		return overview.getProject();
	}

	public FindingStatus getStatus() {
		return overview.getStatus();
	}

	public String getSummary() {
		return overview.getSummary();
	}

	public String getTool() {
		return overview.getTool();
	}

	public boolean isExamined() {
		return overview.isExamined();
	}

}
