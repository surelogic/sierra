package com.surelogic.sierra.jdbc.user;

/**
 * SierraGroups is an enumeration that defines the set of system-default Sierra
 * groups.
 * 
 * @author nathan
 * 
 */
public enum SierraGroup {
	USER("Users", "An application user.  This is the default group."), ADMIN(
			"Administrators", "Users responsible for maintaining the system.");

	private final String name;
	private final String description;

	SierraGroup(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * The name of the user group.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * A brief description of the group's purpose.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

}
