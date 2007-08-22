package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class AuditTrailResponse {

	private Long revision;
	private List<TrailObsoletion> obsolete;
	private List<AuditTrailUpdate> update;

	public List<AuditTrailUpdate> getUpdate() {
		return update;
	}

	public void setUpdate(List<AuditTrailUpdate> update) {
		this.update = update;
	}

	public List<TrailObsoletion> getObsolete() {
		return obsolete;
	}

	public void setObsolete(List<TrailObsoletion> obsolete) {
		this.obsolete = obsolete;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

}
