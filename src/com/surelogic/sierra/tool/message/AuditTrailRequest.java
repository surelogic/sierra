package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlRootElement
@XmlType
public class AuditTrailRequest {
	private String project;
	private Long revision;
	private String qualifier;

	public AuditTrailRequest() {
		// Do Nothing
	}

	public AuditTrailRequest(String project, String qualifier, Long revision) {
		this.project = project;
		this.qualifier = qualifier;
		this.revision = revision;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

}
