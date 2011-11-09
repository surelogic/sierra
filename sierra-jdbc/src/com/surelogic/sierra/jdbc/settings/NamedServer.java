package com.surelogic.sierra.jdbc.settings;

import java.io.Serializable;

/**
 * A small value object that holds a server's name and uuid.
 * 
 * @author nathan
 * 
 */
public class NamedServer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2718880778215291475L;
	private final String uuid;
	private final String name;

	public NamedServer(final String name, final String uuid) {
		this.uuid = uuid;
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NamedServer other = (NamedServer) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

}
