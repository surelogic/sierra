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
	private int comments;

	private String lastSynchDate;
	private String lastSynchUser;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCritical() {
		return critical;
	}

	public void setCritical(int critical) {
		this.critical = critical;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public int getMedium() {
		return medium;
	}

	public void setMedium(int medium) {
		this.medium = medium;
	}

	public int getLow() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public int getIrrelevant() {
		return irrelevant;
	}

	public void setIrrelevant(int irrelevant) {
		this.irrelevant = irrelevant;
	}

	public int getFindings() {
		return findings;
	}

	public void setFindings(int findings) {
		this.findings = findings;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public String getLastSynchDate() {
		return lastSynchDate;
	}

	public void setLastSynchDate(String lastSynchDate) {
		this.lastSynchDate = lastSynchDate;
	}

	public String getLastSynchUser() {
		return lastSynchUser;
	}

	public void setLastSynchUser(String lastSynchUser) {
		this.lastSynchUser = lastSynchUser;
	}

}
