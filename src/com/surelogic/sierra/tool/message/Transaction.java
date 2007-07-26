package com.surelogic.sierra.tool.message;

import java.util.Date;
import java.util.UUID;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Transaction {

	private String trail;
	private Match match;
	private Date timestamp;
	private String comment;
	private TransactionType type;

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

	public Match getMatch() {
		return match;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

}
