package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SyncProjectRequest {

	private String server;
	private String project;
	private List<SyncTrailRequest> trails;
	private long revision;

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public List<SyncTrailRequest> getTrails() {
		if(trails == null) {
			trails = new ArrayList<SyncTrailRequest>();
		}
		return trails;
	}

	public void setTrails(List<SyncTrailRequest> trails) {
		this.trails = trails;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

}
