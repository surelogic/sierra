package com.surelogic.sierra.tool.message;

import java.util.Date;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Audit {

	private Date timestamp;
	private String value;
	private AuditEvent event;

	public Audit() {
		// Do nothing
	}

	public Audit(Date timestamp, String value, AuditEvent event) {
		this.timestamp = timestamp;
		this.value = value;
		this.event = event;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String comment) {
		this.value = comment;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public void setType(AuditEvent event) {
		this.event = event;
	}

}
