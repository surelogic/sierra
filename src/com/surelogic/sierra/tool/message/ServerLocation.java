package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

import com.surelogic.common.i18n.I18N;

/**
 * Represents the information about a connection to a Sierra server.
 * <p>
 * This class is immutable.
 */
public class ServerLocation {

	public static final String DEFAULT_HOST = "localhost";
	public static final String DEFAULT_CONTEXT_PATH = "/sl/";
	public static final int DEFAULT_PORT = 13376;

	private final boolean f_secure;
	private final String f_host;
	private final int f_port;
	private final String f_contextPath;
	private final String f_user;
	private final String f_pass;
	private final boolean f_autoSync;
	private final boolean f_savePassword;

	/**
	 * Constructs a server location object.
	 * 
	 * @param host
	 *            a non-null hostname or IP address.
	 * @param secure
	 *            {@code true} if this server uses <tt>https</tt>, {@code false}
	 *            otherwise.
	 * @param port
	 *            the port the server uses.
	 * @param contextPath
	 *            the URL context path the server uses.
	 * @param user
	 *            a saved user name to use with this server, or {@code null} is
	 *            the user name is not saved.
	 * @param pass
	 *            a saved password to use with this server, or {@code null} is
	 *            the password is not saved.
	 * @param savePassword
	 *            {@code true} if the password should persist longer than this
	 *            session, {@code false} if not.
	 * @param autoSync
	 *            {@code true} if the the client should automatically
	 *            synchronize with this server, {@code false} if not.
	 * 
	 * @throws IllegalArgumentException
	 *             if <tt>host</tt> or <tt>contextPath</tt> are {@code null}.
	 */
	public ServerLocation(final String host, final boolean secure,
			final int port, final String contextPath, final String user,
			final String pass, final boolean savePassword,
			final boolean autoSync) {
		if (host == null) {
			throw new IllegalArgumentException(I18N.err(44, "host"));
		}
		f_host = host;
		f_secure = secure;
		f_port = port;
		f_user = user;
		f_pass = pass;
		if (contextPath == null) {
			throw new IllegalArgumentException(I18N.err(44, "contextPath"));
		}
		f_contextPath = contextPath;
		f_savePassword = savePassword;
		f_autoSync = autoSync;
	}

	/**
	 * Constructs a new default server location.
	 * <p>
	 * This constructor is useful in the user interface at times, but because
	 * this is an immutable class is rarely useful in other contexts.
	 */
	public ServerLocation() {
		this(ServerLocation.DEFAULT_HOST, false, ServerLocation.DEFAULT_PORT,
				ServerLocation.DEFAULT_CONTEXT_PATH, null, null, false, false);
	}

	/**
	 * Creates a new server location with changed authorization information.
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
	 * @return a new server location.
	 */
	public ServerLocation changeAuthorization(final String user,
			final String pass, final boolean savePassword) {
		return new ServerLocation(f_host, f_secure, f_port, f_contextPath,
				user, pass, savePassword, f_autoSync);
	}

	/**
	 * Creates a new server location with a changed automatic synchronization
	 * flag.
	 * <p>
	 * Do not call this method if you are working with this object through a
	 * server manager.
	 * 
	 * @param autoSync
	 *            {@code true} if the the client should automatically
	 *            synchronize with this server, {@code false} if not.
	 * 
	 * @return a new server location.
	 */
	public ServerLocation changeAutoSync(final boolean autoSync) {
		return new ServerLocation(f_host, f_secure, f_port, f_contextPath,
				f_user, f_pass, f_savePassword, autoSync);
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
	 * Gets the saved user name, or the empty string if the user name is not
	 * saved.
	 * 
	 * @return the saved user name, or the empty string if the user name is not
	 *         saved.
	 */
	public String getUserOrEmptyString() {
		return f_user == null ? "" : f_user;
	}

	/**
	 * Gets the saved password, or {@code null} if the password is not saved.
	 * 
	 * @return the saved password, or {@code null} if the password is not saved.
	 */
	public String getPass() {
		return f_pass;
	}

	/**
	 * Gets the saved password, or the empty string if the password is not
	 * saved.
	 * 
	 * @return the saved password, or the empty string if the password is not
	 *         saved.
	 */
	public String getPassOrEmptyString() {
		return f_pass == null ? "" : f_pass;
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
	 * Whether or not the password for this server location should be saved.
	 * 
	 * @return {@code true} if the password should persist longer than this
	 *         session, {@code false} if not.
	 */
	public boolean isSavePassword() {
		return f_savePassword;
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
		b.append(getProtocol()).append(":/");
		b.append(getHost()).append(":").append(getPort()).append(
				getContextPath());
		b.append("/user=\"").append(getUser()).append("\"");

		return b.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (f_autoSync ? 1231 : 1237);
		result = prime * result
				+ ((f_contextPath == null) ? 0 : f_contextPath.hashCode());
		result = prime * result + ((f_host == null) ? 0 : f_host.hashCode());
		result = prime * result + ((f_pass == null) ? 0 : f_pass.hashCode());
		result = prime * result + f_port;
		result = prime * result + (f_savePassword ? 1231 : 1237);
		result = prime * result + (f_secure ? 1231 : 1237);
		result = prime * result + ((f_user == null) ? 0 : f_user.hashCode());
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
		final ServerLocation other = (ServerLocation) obj;
		if (f_autoSync != other.f_autoSync) {
			return false;
		}
		if (f_contextPath == null) {
			if (other.f_contextPath != null) {
				return false;
			}
		} else if (!f_contextPath.equals(other.f_contextPath)) {
			return false;
		}
		if (f_host == null) {
			if (other.f_host != null) {
				return false;
			}
		} else if (!f_host.equals(other.f_host)) {
			return false;
		}
		if (f_pass == null) {
			if (other.f_pass != null) {
				return false;
			}
		} else if (!f_pass.equals(other.f_pass)) {
			return false;
		}
		if (f_port != other.f_port) {
			return false;
		}
		if (f_savePassword != other.f_savePassword) {
			return false;
		}
		if (f_secure != other.f_secure) {
			return false;
		}
		if (f_user == null) {
			if (other.f_user != null) {
				return false;
			}
		} else if (!f_user.equals(other.f_user)) {
			return false;
		}
		return true;
	}
}
