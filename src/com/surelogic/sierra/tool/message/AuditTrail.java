package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class AuditTrail {

	String trail;
	
	private List<Audit> transaction;

	public List<Audit> getTransaction() {
		return transaction;
	}

	public void setTransactions(List<Audit> transaction) {
		this.transaction = transaction;
	}

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

}
