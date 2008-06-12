package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FindingType implements Cacheable, Serializable {
	private static final long serialVersionUID = 1766277814214421247L;

	private String uuid;
	private String name;
	private String shortMessage;
	private String info;
	private List<String> reportedBy;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uid) {
		this.uuid = uid;
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

	public List<String> getReportedBy() {
		if (reportedBy == null) {
			reportedBy = new ArrayList<String>();
		}
		return reportedBy;
	}

}
