package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class AuditTrailRequest {
	private Long revision;
	private Qualifiers qualifiers;

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public Qualifiers getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(Qualifiers qualifiers) {
		this.qualifiers = qualifiers;
	}

}
