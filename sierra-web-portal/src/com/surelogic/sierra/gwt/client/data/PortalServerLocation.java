package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class PortalServerLocation implements Cacheable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3754570376842782004L;

	public enum Protocol {
		HTTP, HTTPS;

		public static Protocol fromValue(final String protocol) {
			if (protocol == null) {
				return null;
			} else if (protocol.equalsIgnoreCase(HTTP.name())) {
				return HTTP;
			} else if (protocol.equalsIgnoreCase(HTTPS.name())) {
				return HTTPS;
			}
			throw new IllegalArgumentException(protocol
					+ " is not a valid protocol string");
		}
	}

	private String name;
	private String uuid;
	private Protocol protocol;
	private String host;
	private int port;
	private String context;
	private String user;
	private String pass;
	private boolean teamServer;

	public PortalServerLocation() {
		port = 13376;
		protocol = Protocol.HTTP;
	}

	public PortalServerLocation(final Protocol protocol, final String host,
			final int port, final String context, final String user,
			final String pass) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.context = context;
		this.user = user;
		this.pass = pass;
	}

	public PortalServerLocation(final String name, final String uuid,
			final Protocol protocol, final String host, final int port,
			final String context, final String user, final String pass,
			final boolean teamServer) {
		this(protocol, host, port, context, user, pass);
		this.name = name;
		this.uuid = uuid;
		this.teamServer = teamServer;
	}

	public String getName() {
		return name;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(final Protocol protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(final String context) {
		this.context = context;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(final String pass) {
		this.pass = pass;
	}

	public String getUuid() {
		return uuid;
	}

	public boolean isTeamServer() {
		return teamServer;
	}

	public void setTeamServer(final boolean teamServer) {
		this.teamServer = teamServer;
	}

}
