package com.surelogic.sierra.tool.message;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class Audit {
    private Date timestamp;
    private String value;
    private AuditEvent event;
    private String user;
    private Long revision;

    public Audit() {
        // Do nothing
    }

    public Audit(Date timestamp, AuditEvent event, String value) {
        this.timestamp = timestamp;
        this.value = value;
        this.event = event;
    }

    public Audit(String user, Date timestamp, AuditEvent event, String value,
        Long revision) {
        this.user = user;
        this.timestamp = timestamp;
        this.value = value;
        this.event = event;
        this.revision = revision;
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

    public void setEvent(AuditEvent event) {
        this.event = event;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        return "Audit(" + event + ", " + timestamp + ", " + value + ", " +
        user + ")";
    }
}
