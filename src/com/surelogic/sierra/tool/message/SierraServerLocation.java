package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents the information about a connection to a team server. This class is
 * immutable.
 */
public class SierraServerLocation {

	public static final String DEFAULT_PATH = "/sl/";

	public static final int DEFAULT_PORT = 13376;

	private final String f_label;
	private final boolean f_secure;
	private final String f_host;
	private final int f_port;
	private final String f_contextPath;
	private final String f_user;
	private final String f_password;
	private final boolean f_autoSync;
	private final boolean f_teamServer;
	private final String f_uuid;

	public SierraServerLocation(final SierraServerLocation from,
			final String uuid, final boolean isTeamServer) {
		this(from.f_label, from.f_host, from.f_secure, from.f_port,
				from.f_contextPath, from.f_user, from.f_password,
				from.f_autoSync, uuid, isTeamServer);
	}

	public SierraServerLocation(String label, String host, boolean secure,
			int port, String contextPath, String user, String pass,
			boolean autoSync) {
		this(label, host, secure, port, contextPath, user, pass, autoSync,
				null, false);
	}

	public SierraServerLocation(String label, String host, boolean secure,
			int port, String contextPath, String user, String pass,
			boolean autoSync, String uuid, boolean isTeamServer) {
		f_host = host;
		f_secure = secure;
		f_port = port;
		f_user = user;
		f_password = pass;
		f_label = label;
		f_contextPath = contextPath;
		f_autoSync = autoSync;
		f_teamServer = isTeamServer;
		f_uuid = uuid;
	}

	/**
	 * Gets the label for this server.
	 * 
	 * @return the label for this server.
	 */
	public String getLabel() {
		return f_label;
	}

	/**
	 * Gets if this server uses a secure connection protocol (<tt>https</tt>).
	 * 
	 * @return {@code true} if this server uses a secure connection protocol,
	 *         {@code false} otherwise.
	 */
	public boolean isSecure() {
		return f_secure;
	}

	/**
	 * Gets the protocol, either <tt>http</tt> or <tt>https</tt>, to this
	 * server.
	 * 
	 * @return the protocol, either <tt>http</tt> or <tt>https</tt>, to this
	 *         server.
	 */
	public String getProtocol() {
		return f_secure ? "https" : "http";
	}

	/**
	 * Gets the host name to this server.
	 * 
	 * @return the host name to this server.
	 */
	public String getHost() {
		return f_host;
	}

	/**
	 * Gets the port to this server.
	 * 
	 * @return the port to this server.
	 */
	public int getPort() {
		return f_port;
	}

	/**
	 * Gets the context path used for creating URLs to this server.
	 * 
	 * @return the context path used for creating URLs to this server.
	 */
	public String getContextPath() {
		return f_contextPath;
	}

	/**
	 * Gets the saved user name, or {@code null} if the user name is not saved.
	 * 
	 * @return the saved user name, or {@code null} if the user name is not
	 *         saved.
	 */
	public String getUser() {
		return f_user;
	}

	/**
	 * Gets the saved password, or {@code null} if the password is not saved.
	 * 
	 * @return the saved password, or {@code null} if the password is not saved.
	 */
	public String getPass() {
		return f_password;
	}

	/**
	 * Gets if this server is intended to be automatically synchronized
	 * 
	 * @return {@code true} if this server is intended to be automatically
	 *         synchronized, {@code false} otherwise.
	 */
	public boolean isAutoSync() {
		return f_autoSync;
	}

	/**
	 * Gets if this server acts as a team server, meaning projects can be
	 * connected to it. If this information hasn't been queried from the server
	 * then this method will return {@code false}.
	 * 
	 * @return {@code true} if this server acts as a team server, {@code false}
	 *         otherwise.
	 */
	public boolean isTeamServer() {
		return f_teamServer;
	}

	/**
	 * Gets the UUID of this server, or {@code null} if it hasn't been queried
	 * from the server.
	 * 
	 * @return the UUID of this server, or {@code null} if it hasn't been
	 *         queried from the server.
	 */
	public String getUuid() {
		return f_uuid;
	}

	/**
	 * Create a URL that points to the appropriate service of this server. All
	 * services should be hosted under the context root "/sierra", and have a
	 * name that is valid w/in a URL string.
	 * 
	 * @param serviceName
	 *            a service name.
	 * @return a URL that points to the appropriate service.
	 */
	public URL createServiceURL(final String serviceName) {
		final String host = getHost() + ":" + getPort();
		try {
			return new URL((f_secure ? "https://" : "http://") + host
					+ f_contextPath + "services/" + serviceName);
		} catch (final MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create a URL to the root of this server.
	 * 
	 * @return a URL to the root of this server.
	 */
	public URL createHomeURL() {
		final String host = getHost() + ":" + getPort();
		try {
			return new URL((f_secure ? "https://" : "http://") + host
					+ f_contextPath);
		} catch (final MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("'").append(getLabel()).append("' is ");
		b.append(getProtocol()).append(":/");
		b.append(getHost()).append(":").append(getPort()).append(
				getContextPath());
		b.append("/user=\"").append(getUser()).append("\"");

		return b.toString();
	}
}
