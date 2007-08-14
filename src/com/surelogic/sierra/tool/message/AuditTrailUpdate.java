package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class AuditTrailUpdate {

	private String trail;
	private List<Match> match;
	private List<Audit> audit;

	public AuditTrailUpdate() {
		// Do nothing
	}

	public AuditTrailUpdate(String uid) {
		this.trail = uid;
	}

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

	public List<Match> getMatch() {
		return match;
	}

	public void setMatch(List<Match> match) {
		this.match = match;
	}

	public List<Audit> getAudit() {
		return audit;
	}

	public void setAudit(List<Audit> audit) {
		this.audit = audit;
	}

}
