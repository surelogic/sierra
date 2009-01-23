/**
 * 
 */
package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.util.*;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.jdbc.finding.FindingAudits;
import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.settings.ScanFilterView;
import com.surelogic.sierra.tool.message.Audit;

class ProjectStatus {
	final IJavaProject project;
	final String name;
	final File scanDoc;
	final ScanInfo scanInfo;
	
	final List<FindingAudits> localFindings;
	final int numLocalAudits;
	final Date earliestLocalAudit, latestLocalAudit;

	final Map<String,Integer> userCount = new HashMap<String,Integer>();
	
	final int numProjectProblems;
	final int numServerProblems;
	final ProjectDO localDBInfo;
	final ScanFilterView filter;
	
	public ProjectStatus(IJavaProject jp) {
		this(jp, null, null, Collections.<FindingAudits>emptyList(), 0, 0, null, null);
	}
	
	public ProjectStatus(IJavaProject jp, File scan, ScanInfo info, 
			             List<FindingAudits> findings,
			             int numServerProblems, int numProjectProblems,
			             ProjectDO dbInfo, ScanFilterView filter) {
		project = jp;
		name = jp.getElementName();
		scanDoc = scan;
		scanInfo = info;
		this.localFindings = findings;
		this.numServerProblems = numServerProblems;
		this.numProjectProblems = numProjectProblems;
		localDBInfo = dbInfo;
		this.filter = filter;
		
		// Preprocess local data
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
		numLocalAudits = newAudits;
		earliestLocalAudit = earliest;
		latestLocalAudit = latest;
	}
	
}