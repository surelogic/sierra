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
import com.surelogic.sierra.tool.message.SyncTrailResponse;

class ProjectStatus {
	final IJavaProject project;
	final String name;
	final File scanDoc;
	final ScanInfo scanInfo;
	
	final List<FindingAudits> localFindings;
	final int numLocalAudits;
	final Date earliestLocalAudit, latestLocalAudit;

	final List<SyncTrailResponse> serverData;
	final int numServerAudits;
	final Date earliestServerAudit, latestServerAudit;
	final int comments, importance, read, summary;
	final Map<String,Integer> userCount = new HashMap<String,Integer>();
	
	final int numProjectProblems;
	final int numServerProblems;
	final ProjectDO localDBInfo;
	final ScanFilterView filter;
	
	public ProjectStatus(IJavaProject jp) {
		this(jp, null, null, Collections.<FindingAudits>emptyList(), null, 0, 0, null, null);
	}
	
	public ProjectStatus(IJavaProject jp, File scan, ScanInfo info, 
			             List<FindingAudits> findings,
			             List<SyncTrailResponse> responses, 
			             int numServerProblems, int numProjectProblems,
			             ProjectDO dbInfo, ScanFilterView filter) {
		project = jp;
		name = jp.getElementName();
		scanDoc = scan;
		scanInfo = info;
		this.localFindings = findings;
		serverData = responses;
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

		// Preprocess server data
		newAudits = 0;
		earliest = null;
		latest = null;
		int comments = 0, importance = 0, read = 0, summary = 0;
		if (responses != null) {
			for(SyncTrailResponse r : responses) {
				newAudits += r.getAudits().size();
				for(Audit a : r.getAudits()) {
					if (earliest == null) {
						earliest = latest = a.getTimestamp();
					} 
					else if (earliest.after(a.getTimestamp())){
						earliest = a.getTimestamp();
					}
					else if (latest.before(a.getTimestamp())){
						latest = a.getTimestamp();
					}
					Integer i = userCount.get(a.getUser());
					if (i == null) {
						userCount.put(a.getUser(), 1);
					} else {
						userCount.put(a.getUser(), i+1);
					}

					switch (a.getEvent()) {
					case COMMENT:
						comments++;
						break;
					case IMPORTANCE:
						importance++;
						break;
					case READ:
						read++;
						break;
					case SUMMARY:
						summary++;
						break;
					default:
						throw new IllegalStateException("Unknown audit event: "+a.getEvent());
					}
				}			
			}
		}
		numServerAudits = newAudits;
		earliestServerAudit = earliest;
		latestServerAudit = latest;
		this.comments = comments;
		this.importance = importance;
		this.read = read;
		this.summary = summary;
	}
	
}