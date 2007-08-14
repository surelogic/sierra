package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class AuditTrail {

	private String trail;

	private List<Audit> audits;

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

	public List<Audit> getAudits() {
		return audits;
	}

	public void setAudits(List<Audit> audits) {
		this.audits = audits;
	}

}
