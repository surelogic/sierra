package com.surelogic.sierra.tool.message;

/**
 * Enumerates all possible transactions.
 * 
 * @author nathan
 * 
 */
public enum AuditEvent {
	/**
	 * A user-applied comment to a finding.
	 */
	COMMENT,
	/**
	 * The user has changed the importance of a finding.
	 */
	IMPORTANCE

}
