package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class MergeAuditTrailResponse {

	private List<String> trail;
	private Long revision;

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public List<String> getTrail() {
		return trail;
	}

	public void setTrail(List<String> trail) {
		this.trail = trail;
	}

}
