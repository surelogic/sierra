package com.surelogic.sierra.jdbc.user;

import java.io.Serializable;

/**
 * Represents a sierra database user.
 * 
 * @author nathan
 * 
 */
public interface User extends Serializable {

	String getUserName();

	long getId();

}
