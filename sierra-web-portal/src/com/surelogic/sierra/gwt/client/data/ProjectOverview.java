package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ProjectOverview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8195836399029832788L;
	private String name;

	private int critical;
	private int high;
	private int medium;
	private int low;
	private int irrelevant;
	private int findings;

	private int commentedFindings;
	private int comments;

	private String lastSynchDate;
	private String lastSynchUser;

	private String lastScanDate;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getCritical() {
		return critical;
	}

	public void setCritical(final int critical) {
		this.critical = critical;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(final int high) {
		this.high = high;
	}

	public int getMedium() {
		return medium;
	}

	public void setMedium(final int medium) {
		this.medium = medium;
	}

	public int getLow() {
		return low;
	}

	public void setLow(final int low) {
		this.low = low;
	}

	public int getIrrelevant() {
		return irrelevant;
	}

	public void setIrrelevant(final int irrelevant) {
		this.irrelevant = irrelevant;
	}

	public int getTotalFindings() {
		return findings;
	}

	public void setTotalFindings(final int findings) {
		this.findings = findings;
	}

	public int getCommentedFindings() {
		return commentedFindings;
	}

	public void setCommentedFindings(final int findings) {
		this.commentedFindings = findings;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(final int comments) {
		this.comments = comments;
	}

	public String getLastSynchDate() {
		return lastSynchDate;
	}

	public void setLastSynchDate(final String lastSynchDate) {
		this.lastSynchDate = lastSynchDate;
	}

	public String getLastSynchUser() {
		return lastSynchUser;
	}

	public void setLastSynchUser(final String lastSynchUser) {
		this.lastSynchUser = lastSynchUser;
	}

	public String getLastScanDate() {
		return lastScanDate;
	}

	public void setLastScanDate(final String lastScanDate) {
		this.lastScanDate = lastScanDate;
	}

}
