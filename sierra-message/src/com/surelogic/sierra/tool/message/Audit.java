package com.surelogic.sierra.tool.message;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class Audit {
	private String uuid;
	private Date timestamp;
	private String value;
	private AuditEvent event;
	private String user;
	private Long revision;

	public Audit() {
		// Do nothing
	}

	public Audit(final String uuid, final Date timestamp,
			final AuditEvent event, final String value) {
		this.uuid = uuid;
		this.timestamp = timestamp;
		this.value = value;
		this.event = event;
	}

	public Audit(final String uuid, final String user, final Date timestamp,
			final AuditEvent event, final String value, final Long revision) {
		this.uuid = uuid;
		this.user = user;
		this.timestamp = timestamp;
		this.value = value;
		this.event = event;
		this.revision = revision;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String comment) {
		this.value = comment;
	}

	public AuditEvent getEvent() {
		return event;
	}

	public void setEvent(final AuditEvent event) {
		this.event = event;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public Long getRevision() {
		return revision;
	}

	public void setRevision(final Long revision) {
		this.revision = revision;
	}

	@Override
	public String toString() {
		return "Audit(" + event + ", " + timestamp + ", " + value + ", " + user
				+ ")";
	}
}
