/**
 * 
 */
package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.util.*;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.scan.ScanInfo;

class ProjectStatus {
	final IJavaProject project;
	final String name;
	final File scanDoc;
	final ScanInfo scanInfo;
	
	final int numLocalAudits;

	final Map<String,Integer> userCount = new HashMap<String,Integer>();
	
	final int numProjectProblems;
	final int numServerProblems;
	final ProjectDO localDBInfo;
	final String filterName;
	
	public ProjectStatus(IJavaProject jp, String filterName) {
		this(jp, null, null, 0, 0, 0, null, filterName);
	}
	
	public ProjectStatus(IJavaProject jp, File scan, ScanInfo info, 
			             int numLocalAudits,
			             int numServerProblems, int numProjectProblems,
			             ProjectDO dbInfo, String filterName) {
		project = jp;
		name = jp.getElementName();
		scanDoc = scan;
		scanInfo = info;
		this.numLocalAudits = numLocalAudits;
		this.numServerProblems = numServerProblems;
		this.numProjectProblems = numProjectProblems;
		localDBInfo = dbInfo;
		this.filterName = filterName;
	}
	
}