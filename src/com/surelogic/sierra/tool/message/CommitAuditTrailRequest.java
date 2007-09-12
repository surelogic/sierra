package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class CommitAuditTrailRequest {

	private String server;

	private List<AuditTrail> auditTrail;

	public List<AuditTrail> getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(List<AuditTrail> auditTrail) {
		this.auditTrail = auditTrail;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}
