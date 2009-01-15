package com.surelogic.sierra.client.eclipse.model;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;

import com.surelogic.common.base64.Base64;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoService;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.Services;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class SierraServer {

	private final SierraServerManager f_manager;

	public SierraServerManager getManager() {
		return f_manager;
	}

	/**
	 * Requires a lock on {@code this} when mutated.
	 */
	private ServerLocation f_location;

	/**
	 * Returns the server location object associated with this. This object is
	 * immutable and contains a lot of information about.
	 * 
	 * @return a server location object.
	 */
	public ServerLocation getLocation() {
		synchronized (this) {
			return f_location;
		}
	}

	/**
	 * Replaces the server location object associated with this. Server location
	 * objects are immutable, so when we change something about this object's
	 * server location we change our reference.
	 * 
	 * @param location
	 *            a server location object.
	 */
	private void setLocation(final ServerLocation location) {
		synchronized (this) {
			f_location = location;
		}
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


	public boolean setLabel(final String label) {
		if ((label == null) || label.equals(f_label)) {
			return false;
		}
		setLabel_internal(label);
		f_manager.notifyObservers();
		return true;
	}

	private void setLabel_internal(final String label) {
		synchronized (f_manager) {
			// overwrite semantics, i.e., no check if the new name is in use
			f_manager.f_labelToServer.remove(f_label);
			f_label = label;
			f_manager.f_labelToServer.put(f_label, this);
		}
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

	public ServerLocation getServer() {
		return new ServerLocation(f_label, f_host, f_secure, f_port,
				f_contextPath, f_user, f_password, f_autoSync);
	}

	/**
	 * Sets the information about this object from the passed server location
	 * object.
	 * 
	 * @param location
	 *            the server location object to extract data from.
	 * @param serverReply
	 *            the reply from the server, may be {@code null} if one is not
	 *            available.
	 * @return {@true} if this was changed, {@code false} otherwise.
	 */
	public synchronized boolean setServer(final ServerLocation location,
			final ServerInfoReply serverReply) {
		final boolean changedInfo = !gotServerInfo && serverReply != null;
		final boolean changed = differs(f_label, location.getLabel())
				|| differs(f_host, location.getHost())
				|| (f_secure != location.isSecure())
				|| (f_port != location.getPort())
				|| differs(f_contextPath, location.getContextPath())
				|| differs(f_user, location.getUser())
				|| differs(f_password, location.getPass()) || changedInfo;
		setLabel_internal(location.getLabel());
		f_host = location.getHost();
		f_secure = location.isSecure();
		f_port = location.getPort();
		f_contextPath = location.getContextPath();
		f_user = location.getUser();
		f_password = location.getPass();
		if (changedInfo) {
			updateServerInfo(serverReply);
		}
		return changed;
	}

	private static boolean differs(final String s1, final String s2) {
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

	public synchronized boolean updateServerInfo(final boolean force) {
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

	synchronized void setServerType(final boolean team, final boolean bug) {
		isTeamServer = team;
		isBugLink = bug;
		gotServerInfo = true;
	}
}
