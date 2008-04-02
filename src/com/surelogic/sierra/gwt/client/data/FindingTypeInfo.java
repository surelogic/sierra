package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FindingTypeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1766277814214421247L;

	private String uid;
	private String name;
	private String shortMessage;
	private String info;
	/**
	 * @gwt.typeArgs <java.lang.String>
	 */
	private List reportedBy;
	private Ticket foundInTicket;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Ticket getFoundInTicket() {
		return foundInTicket;
	}

	public void setFoundInTicket(Ticket foundInTicket) {
		this.foundInTicket = foundInTicket;
	}

	public List getReportedBy() {
		if (reportedBy == null) {
			reportedBy = new ArrayList();
		}
		return reportedBy;
	}

}
