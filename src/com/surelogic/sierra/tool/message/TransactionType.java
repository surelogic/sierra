package com.surelogic.sierra.tool.message;

/**
 * Enumerates all possible transactions.
 * 
 * @author nathan
 * 
 */
public enum TransactionType {
	/**
	 * A user-applied comment to a finding.
	 */
	COMMENT,
	/**
	 * The user has marked a finding as interesting/uninteresting
	 */
	INTEREST,
	/**
	 * The user has changed the priority of a finding.
	 */
	PRIORITY,
	/**
	 * The user has changed the severity of a finding.
	 */
	SEVERITY
}
