/**
 * 
 */
package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.jdbc.finding.FindingAudits;
import com.surelogic.sierra.tool.message.Audit;

class ProjectStatus {
	final IJavaProject project;
	final String name;
	final File scanDoc;
	final List<FindingAudits> findings;
	final int numAudits;
	final Date earliestAudit, latestAudit;
	
	public ProjectStatus(IJavaProject jp, File scan, List<FindingAudits> findings) {
		project = jp;
		name = jp.getElementName();
		scanDoc = scan;
		this.findings = findings;

		int newAudits = 0;
		Date earliest = null;
		Date latest = null;
		for(FindingAudits fa : findings) {
			newAudits += fa.getAudits().size();
			for(Audit a : fa.getAudits()) {
				if (earliest == null) {
					earliest = latest = a.getTimestamp();
				} 
				else if (earliest.after(a.getTimestamp())){
					earliest = a.getTimestamp();
				}
				else if (latest.before(a.getTimestamp())){
					latest = a.getTimestamp();
				}
			}
		}
		numAudits = newAudits;
		earliestAudit = earliest;
		latestAudit = latest;
	}
	
}