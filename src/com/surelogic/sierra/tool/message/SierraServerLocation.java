package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

public class SierraServerLocation {
	public static final String DEFAULT_PATH = "/sl/";
	public static final int DEFAULT_PORT = 13376;
	private static final String UNLABELED_SERVER = "unlabeled server";
	public static final SierraServerLocation DEFAULT = new SierraServerLocation(
			"localhost:13376", null, null);

	/**
	 * A client label for this server location, or <tt>unknown</tt> if not a
	 * client connection, e.g., in the Ant task.
	 */
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

	public SierraServerLocation(final SierraServerLocation loc,
			final String uuid, final boolean isTeamServer) {
		f_label = loc.f_label;
		f_secure = loc.f_secure;
		f_host = loc.f_host;
		f_port = loc.f_port;
		f_contextPath = loc.f_contextPath;
		f_user = loc.f_user;
		f_password = loc.f_password;
		f_autoSync = loc.f_autoSync;
		f_teamServer = isTeamServer;
		f_uuid = uuid;
	}

	public SierraServerLocation(final String label, final String host,
			final boolean secure, final int port, final String contextPath,
			final String user, final String pass, final boolean autoSync,
			final String uuid, final boolean isTeamServer) {
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

	public SierraServerLocation(final String label, final String host,
			final boolean secure, final int port, final String contextPath,
			final String user, final String pass, final boolean autoSync) {
		f_host = host;
		f_secure = secure;
		f_port = port;
		f_user = user;
		f_password = pass;
		f_label = label;
		f_contextPath = contextPath;
		f_autoSync = autoSync;
		f_teamServer = false;
		f_uuid = null;
	}

	public SierraServerLocation(final String server, final String user,
			final String pass) {
		URL url;
		try {
			url = new URL(server);
		} catch (final MalformedURLException e) {
			url = null;
		}
		if (url != null) {
			f_port = url.getPort();
			if ((url.getPath() == null) || "".equals(url.getPath())) {
				f_contextPath = "/sl/";
			} else {
				f_contextPath = url.getPath();
			}
			f_host = url.getHost();
			f_secure = "https".equals(url.getProtocol());
		} else {
			/*
			 * TODO: fix this to set the protocol properly
			 */
			final String[] strArr = server.split(":");
			if (strArr.length > 1) {
				final String strPort = strArr[1];
				f_port = Integer.parseInt(strPort);
			} else {
				f_port = DEFAULT_PORT;
			}
			f_contextPath = "/";
			f_host = strArr[0];
			f_secure = false;
		}
		f_user = user;
		f_password = pass;
		f_label = UNLABELED_SERVER;
		f_autoSync = false;
		f_teamServer = false;
		f_uuid = null;
	}

	public String getLabel() {
		return f_label;
	}

	public boolean isSecure() {
		return f_secure;
	}

	public String getProtocol() {
		return f_secure ? "https" : "http";
	}

	public String getHost() {
		return f_host;
	}

	public int getPort() {
		return f_port;
	}

	public String getContextPath() {
		return f_contextPath;
	}

	public String getUser() {
		return f_user;
	}

	public String getPass() {
		return f_password;
	}

	public boolean isAutoSync() {
		return f_autoSync;
	}

	public boolean isTeamServer() {
		return f_teamServer;
	}

	public String getUuid() {
		return f_uuid;
	}

	/**
	 * Create a url that points to the appropriate service. All services should
	 * be hosted under the context root "/sierra", and have a name that is valid
	 * w/in a url string.
	 * 
	 * @param serviceName
	 * @return
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((f_label == null) ? 0 : f_label.hashCode());
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
		final SierraServerLocation other = (SierraServerLocation) obj;
		if (f_label == null) {
			if (other.f_label != null) {
				return false;
			}
		} else if (!f_label.equals(other.f_label)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("'").append(getLabel()).append("' is ");
		b.append(getProtocol()).append(":/");
		b.append(getHost()).append(":").append(getPort()).append(
				getContextPath());
		b.append("/user=\"").append(getUser()).append("\" ");
		b.append(" password=\"").append(
				getPass() == null ? "" : getPass().replaceAll(".", "*"))
				.append("\"");

		return b.toString();
	}

	public URL createHomeURL() {
		final String host = getHost() + ":" + getPort();
		try {
			return new URL((f_secure ? "https://" : "http://") + host
					+ f_contextPath);
		} catch (final MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}
}
