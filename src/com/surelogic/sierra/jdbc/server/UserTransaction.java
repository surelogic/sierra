package com.surelogic.sierra.jdbc.server;

import java.security.Principal;

/**
 * Represents an SQL transaction with a principal in context.
 * 
 * @author nathan
 * 
 */
public interface UserTransaction<T> extends Transaction<T> {

	/**
	 * Returns the principal in context. May not be null. If you do not have a
	 * principal, you should consider using Transaction.
	 * 
	 * @return
	 */
	Principal getPrincipal();

}
