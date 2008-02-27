package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.Date;

public class UserOverview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2178621492690389786L;
	
	
	public String userName;
	public Date lastSynch;
	public int audits;
	public int findings;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getLastSynch() {
		return lastSynch;
	}

	public void setLastSynch(Date lastSynch) {
		this.lastSynch = lastSynch;
	}

	public int getAudits() {
		return audits;
	}

	public void setAudits(int audits) {
		this.audits = audits;
	}

	public int getFindings() {
		return findings;
	}

	public void setFindings(int findings) {
		this.findings = findings;
	}

}
