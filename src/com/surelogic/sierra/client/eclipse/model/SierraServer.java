package com.surelogic.sierra.client.eclipse.model;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import com.surelogic.common.base64.Base64;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public final class SierraServer {

	private final SierraServerManager f_manager;

	public SierraServerManager getManager() {
		return f_manager;
	}

	public SierraServer(final SierraServerManager manager, final String label) {
		assert manager != null;
		f_manager = manager;
		assert label != null;
		f_label = label;
		assert !f_manager.f_labelToServer.containsKey(f_label);
		f_manager.f_labelToServer.put(f_label, this);
	}

	private String f_label = "";

	public String getLabel() {
		return f_label;
	}

	public void setLabel(String label) {
		if (label == null || label.equals(f_label))
			return;
		// overwrite semantics, i.e., no check if the new name is in use
		f_manager.f_labelToServer.remove(f_label);
		f_label = label;
		f_manager.f_labelToServer.put(f_label, this);
		f_manager.notifyObservers();
	}

	private boolean f_secure = false;

	public boolean isSecure() {
		return f_secure;
	}

	/**
	 * Is this server the current focus of its model.
	 * 
	 * @return <code>true</code> if it is the focus, <code>false</code>
	 *         otherwise.
	 * 
	 * @see SierraServerManager#getFocus()
	 */
	public boolean isFocus() {
		return f_manager.getFocus() == this;
	}

	public void setFocus() {
		f_manager.setFocus(this);
	}

	public String getProtocol() {
		return f_secure ? "https" : "http";
	}

	public void setSecure(boolean secure) {
		f_secure = secure;
	}

	private String f_host = "";

	public String getHost() {
		return f_host;
	}

	public void setHost(String host) {
		f_host = host;
	}

	private int f_port = 13376;

	public int getPort() {
		return f_port;
	}

	public void setPort(int port) {
		f_port = port;
	}

	private String f_contextPath = "/sl/";

	public String getContextPath() {
		return f_contextPath;
	}

	public void setContextPath(String contextPath) {
		f_contextPath = contextPath;
	}

	private String f_user = "";

	public String getUser() {
		return f_user;
	}

	public void setUser(String user) {
		f_user = user;
	}

	private String f_password = "";

	public String getPassword() {
		return f_password;
	}

	public void setPassword(String password) {
		f_password = password;
	}

	private boolean f_savePassword = false;

	public boolean savePassword() {
		return f_savePassword;
	}

	public void setSavePassword(boolean savePassword) {
		f_savePassword = savePassword;
	}

	/**
	 * Not persisted, only used during an Eclipse session.
	 */
	private boolean f_usedToConnectToAServer = false;

	public boolean usedToConnectToAServer() {
		return f_usedToConnectToAServer;
	}

	public void setUsed() {
		f_usedToConnectToAServer = true;
	}

	/**
	 * The path off the site context path where the portal is located.
	 */
	private static final String SIERRA_PORTAL_PATH = "portal/";
	private static final String ENCODING = "UTF-8";

	public String toURLWithContextPath() {
		final StringBuilder b = new StringBuilder();
		b.append(getProtocol()).append("://");
		b.append(getHost()).append(":").append(getPort());
		b.append(getContextPath());
		return b.toString();
	}

	public URL toAuthorizedURL() throws Exception {
		final StringBuilder b = new StringBuilder(toURLWithContextPath());
		b.append(SIERRA_PORTAL_PATH);
		if (savePassword()) {
			b.append("login?SierraAuthName=");
			b.append(URLEncoder.encode(getUser(), ENCODING));
			b.append("&SierraAuthPass=");
			final String base64Password = Base64.encodeBytes(getPassword()
					.getBytes("UTF-8"));
			b.append(URLEncoder.encode(base64Password, ENCODING));
		}
		SLLogger.getLogger().fine("getAuthorizedURL() = " + b.toString());
		final URI uri = new URI(b.toString());
		final URL url = uri.toURL();
		return url;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(getLabel()).append(" is ");
		b.append(toURLWithContextPath());
		b.append(" user=\"").append(getUser()).append("\" ");
		return b.toString();
	}

	public SierraServerLocation getServer() {
		return new SierraServerLocation(f_label, f_host, f_secure, f_port,
				f_contextPath, f_user, f_password);
	}
}
