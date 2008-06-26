package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class ServerLocation implements Cacheable, Serializable {

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

	private String label;
	private Protocol protocol;
	private String host;
	private int port;
	private String context;
	private String user;
	private String pass;

	public ServerLocation() {
		port = 13376;
		protocol = Protocol.HTTP;
	}

	/**
	 * Create a server location with the given label. Other properties will be
	 * initialized to their default values.
	 * 
	 * @param label
	 */
	public ServerLocation(final String label) {
		port = 13376;
		protocol = Protocol.HTTP;
	}

	public ServerLocation(final String label, final Protocol protocol,
			final String host, final int port, final String context,
			final String user, final String pass) {
		this.label = label;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.context = context;
		this.user = user;
		this.pass = pass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
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
		return label;
	}

	public ServerLocation copy(final ServerLocation orig) {
		final ServerLocation l = new ServerLocation();
		l.context = orig.context;
		l.host = orig.host;
		l.label = orig.label;
		l.pass = orig.pass;
		l.port = orig.port;
		l.protocol = orig.protocol;
		l.user = orig.user;
		return l;
	}

}
