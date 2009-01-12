package com.surelogic.sierra.client.eclipse.model;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;

import com.surelogic.common.base64.Base64;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoService;
import com.surelogic.sierra.tool.message.Services;
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
		synchronized (f_manager) {
			assert !f_manager.f_labelToServer.containsKey(f_label);
			f_manager.f_labelToServer.put(f_label, this);
		}
	}

	private String f_label = "";

	public String getLabel() {
		return f_label;
	}

	public boolean setLabel(String label) {
		if ((label == null) || label.equals(f_label)) {
			return false;
		}
		setLabel_internal(label);
		f_manager.notifyObservers();
		return true;
	}

	private void setLabel_internal(String label) {
		synchronized (f_manager) {
			// overwrite semantics, i.e., no check if the new name is in use
			f_manager.f_labelToServer.remove(f_label);
			f_label = label;
			f_manager.f_labelToServer.put(f_label, this);
		}
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

	private int f_port = SierraServerLocation.DEFAULT_PORT;

	public int getPort() {
		return f_port;
	}

	public void setPort(int port) {
		f_port = port;
	}

	private String f_contextPath = SierraServerLocation.DEFAULT_PATH;

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

	private boolean f_autoSync = false;

	public boolean autoSync() {
		return f_autoSync;
	}

	public void setAutoSync(boolean autoSync) {
		f_autoSync = autoSync;
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

	/**
	 * @param serverReply
	 * @return true if changed
	 */
	public synchronized boolean setServer(SierraServerLocation loc,
			ServerInfoReply serverReply) {
		final boolean changedInfo = !gotServerInfo && serverReply != null;
		final boolean changed = differs(f_label, loc.getLabel())
				|| differs(f_host, loc.getHost())
				|| (f_secure != loc.isSecure()) || (f_port != loc.getPort())
				|| differs(f_contextPath, loc.getContextPath())
				|| differs(f_user, loc.getUser())
				|| differs(f_password, loc.getPass()) || changedInfo;
		setLabel_internal(loc.getLabel());
		f_host = loc.getHost();
		f_secure = loc.isSecure();
		f_port = loc.getPort();
		f_contextPath = loc.getContextPath();
		f_user = loc.getUser();
		f_password = loc.getPass();
		if (changedInfo) {
			updateServerInfo(serverReply);
		}
		return changed;
	}

	private static boolean differs(String s1, String s2) {
		if (s1 == s2) {
			return false;
		}
		if ((s1 == null) || (s2 == null)) {
			// The other must be non-null
			return true;
		}
		return s1.equals(s2);
	}

	private int f_problemCount = 0;

	public synchronized void markAsConnected() {
		f_problemCount = 0;
	}

	public synchronized int encounteredProblem() {
		f_problemCount++;
		return f_problemCount;
	}

	public synchronized int getProblemCount() {
		return f_problemCount;
	}

	private boolean gotServerInfo;
	private boolean isTeamServer;
	private boolean isBugLink;

	public void updateServerInfo() {
		updateServerInfo(false);
	}

	public synchronized boolean updateServerInfo(boolean force) {
		if (gotServerInfo && !force) {
			return true;
		}
		try {
			final ServerInfoService sis = ServerInfoServiceClient
					.create(getServer());
			final ServerInfoReply reply = sis
					.getServerInfo(new ServerInfoRequest());
			updateServerInfo(reply);
		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					"Exception while updating server info", e);
			encounteredProblem();
		}
		return gotServerInfo;
	}

	/**
	 * @RequiresLock ServerInfoLock
	 */
	private void updateServerInfo(final ServerInfoReply reply) {
		isBugLink = reply.getServices().contains(Services.BUGLINK);
		isTeamServer = reply.getServices().contains(Services.TEAMSERVER);
		gotServerInfo = true;
	}

	public synchronized boolean gotServerInfo() {
		return gotServerInfo;
	}

	public synchronized boolean isBugLink() {
		return isBugLink;
	}

	public synchronized boolean isTeamServer() {
		return isTeamServer;
	}

	synchronized void setServerType(boolean team, boolean bug) {
		isTeamServer = team;
		isBugLink = bug;
		gotServerInfo = true;
	}
}
