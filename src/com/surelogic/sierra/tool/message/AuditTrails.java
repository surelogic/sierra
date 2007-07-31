package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement 
public class AuditTrails {

	private List<AuditTrail> auditTrail;

	public List<AuditTrail> getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(List<AuditTrail> auditTrail) {
		this.auditTrail = auditTrail;
	}

}
