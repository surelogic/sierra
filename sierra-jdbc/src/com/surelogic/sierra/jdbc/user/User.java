package com.surelogic.sierra.jdbc.user;

import java.io.Serializable;
import java.security.Principal;

/**
 * Represents a sierra database user.
 * 
 * @author nathan
 * 
 */
public interface User extends Serializable, Principal {

	long getId();

	boolean isActive();
}
