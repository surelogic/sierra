package com.surelogic.sierra.jdbc.settings;

import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * This class represents a connected server in the Sierra client or server
 * model. This includes a location of the server (network information
 * primarily), a UUID, a human-friendly name, and a flat indicating if the
 * server can act as a team server (i.e., projects can be connected to it).
 * <p>
 * This class is immutable.
 */
public class ConnectedServer {

	private final String f_uuid;
	private final boolean f_isTeamServer;
	private final String f_name;
	private final ServerLocation f_location;

	/**
	 * Constructs a connected server object.
	 * 
	 * @param uuid
	 *            the non-null identifier of this server.
	 * @param name
	 *            the human-friendly name of this server.
	 * @param isTeamServer
	 *            {@code true} if this server can act as a team server, {@code
	 *            false} if not.
	 * @param location
	 *            the non-null location of this server.
	 * 
	 * @throws IllegalArgumentException
	 *             if <tt>uuid</tt>, <tt>name</tt> or <tt>location</tt> are
	 *             {@code null}.
	 */
	public ConnectedServer(final String uuid, final String name,
			final boolean isTeamServer, final ServerLocation location) {
		if (uuid == null) {
			throw new IllegalArgumentException(I18N.err(44, "uuid"));
		}
		f_uuid = uuid;
		if (name == null) {
			throw new IllegalArgumentException(I18N.err(44, "name"));
		}
		f_name = name;
		f_isTeamServer = isTeamServer;
		if (location == null) {
			throw new IllegalArgumentException(I18N.err(44, "location"));
		}
		f_location = location;
	}

	/**
	 * Creates a new server with changed authorization information.
	 * <p>
	 * Do not call this method if you are working with this object through a
	 * server manager.
	 * 
	 * @param user
	 *            a saved user name to use with this server, or {@code null} is
	 *            the user name is not saved.
	 * @param pass
	 *            a saved password to use with this server, or {@code null} is
	 *            the password is not saved.
	 * @param savePassword
	 *            {{@code true} if the password should persist longer than this
	 *            session, {@code false} if not.
	 * @return a new server.
	 */
	public ConnectedServer changeAuthorization(final String user,
			final String pass, final boolean savePassword) {
		return new ConnectedServer(f_uuid, f_name, f_isTeamServer,
				getLocation().changeAuthorization(user, pass, savePassword));
	}

	/**
	 * Creates a new server with a changed automatic synchronization flag.
	 * <p>
	 * Do not call this method if you are working with this object through a
	 * server manager.
	 * 
	 * @param autoSync
	 *            {@code true} if the the client should automatically
	 *            synchronize with this server, {@code false} if not.
	 * 
	 * @return a new server.
	 */
	public ConnectedServer changeAutoSync(final boolean autoSync) {
		return new ConnectedServer(f_uuid, f_name, f_isTeamServer,
				getLocation().changeAutoSync(autoSync));
	}

	/**
	 * Gets the UUID of this server.
	 * 
	 * @return the UUID of this server.
	 */
	public String getUuid() {
		return f_uuid;
	}

	/**
	 * Gets the name of this server. The server name is set when the server is
	 * first setup and is a human-friendly name for the server.
	 * 
	 * @return the label for this server.
	 */
	public String getName() {
		return f_name;
	}

	/**
	 * Gets the server location object associated with this.
	 * 
	 * @return a server location object.
	 */
	public ServerLocation getLocation() {
		return f_location;
	}

	/**
	 * Gets if this server acts as a team server.
	 * 
	 * @return {@code true} if this server can act as a team server, {@code
	 *         false} if not.
	 */
	public boolean isTeamServer() {
		return f_isTeamServer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (f_isTeamServer ? 1231 : 1237);
		result = prime * result
				+ ((f_location == null) ? 0 : f_location.hashCode());
		result = prime * result + ((f_name == null) ? 0 : f_name.hashCode());
		result = prime * result + ((f_uuid == null) ? 0 : f_uuid.hashCode());
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
		final ConnectedServer other = (ConnectedServer) obj;
		if (f_isTeamServer != other.f_isTeamServer) {
			return false;
		}
		if (f_location == null) {
			if (other.f_location != null) {
				return false;
			}
		} else if (!f_location.equals(other.f_location)) {
			return false;
		}
		if (f_name == null) {
			if (other.f_name != null) {
				return false;
			}
		} else if (!f_name.equals(other.f_name)) {
			return false;
		}
		if (f_uuid == null) {
			if (other.f_uuid != null) {
				return false;
			}
		} else if (!f_uuid.equals(other.f_uuid)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Uuid: " + f_uuid + "Location: " + f_location;
	}
}
