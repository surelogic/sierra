package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ServerLocation implements Cacheable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3754570376842782004L;

	public enum Protocol {
		HTTP, HTTPS;

		public static Protocol fromValue(String protocol) {
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

	}

	public ServerLocation(String label, Protocol protocol, String host, int port,
			String context, String user, String pass) {
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

	public void setLabel(String label) {
		this.label = label;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getUuid() {
		// FIXME is this unique?
		return label;
	}

}
