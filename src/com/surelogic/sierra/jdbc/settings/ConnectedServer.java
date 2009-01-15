package com.surelogic.sierra.jdbc.settings;

import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.tool.message.ServerLocation;

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
	public ConnectedServer(String uuid, String name, boolean isTeamServer,
			ServerLocation location) {
		if (uuid == null)
			throw new IllegalArgumentException(I18N.err(44, "uuid"));
		f_uuid = uuid;
		if (name == null)
			throw new IllegalArgumentException(I18N.err(44, "name"));
		f_name = name;
		f_isTeamServer = isTeamServer;
		if (location == null)
			throw new IllegalArgumentException(I18N.err(44, "location"));
		f_location = location;
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
}
