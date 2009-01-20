package com.surelogic.sierra.client.eclipse.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

import com.surelogic.common.ILifecycle;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Manages the set of connected servers.
 * <p>
 * This class persists to the database and is saved to the database each time
 * something is changed. It listens to database changes to detect when it should
 * load or re-load data.
 */
public final class ConnectedServerManager extends
		DatabaseObservable<ISierraServerObserver> implements ILifecycle {

	public static final ConnectedServerManager INSTANCE = new ConnectedServerManager();

	public static ConnectedServerManager getInstance() {
		return INSTANCE;
	}

	private ConnectedServerManager() {
		// singleton
	}

	public void init() {
		load();
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
	}

	/**
	 * The servers managed by this model.
	 * <p>
	 * Access to this list is protected by a lock on {@code this}.
	 */
	private final List<ConnectedServer> f_servers = new ArrayList<ConnectedServer>();

	public boolean isEmpty() {
		synchronized (this) {
			return f_servers.isEmpty();
		}
	}

	/**
	 * Gets the set of servers managed by this model.
	 * 
	 * @return a set of servers.
	 */
	public Set<ConnectedServer> getServers() {
		final Set<ConnectedServer> servers;
		synchronized (this) {
			servers = new HashSet<ConnectedServer>(f_servers);
		}
		return servers;
	}

	/**
	 * Looks for a server managed by this model by its UUID, {@code null} is
	 * returned if no server with the passed UUID can be found.
	 * 
	 * @param uuid
	 *            the UUID of the desired server.
	 * @return a server or {@code null} if none can be found.
	 */
	public ConnectedServer getServerByUuid(final String uuid) {
		if (uuid != null) {
			final Set<ConnectedServer> servers = getServers();
			for (final ConnectedServer server : servers) {
				if (server.getUuid().equals(uuid)) {
					return server;
				}
			}
		}
		return null;
	}

	/**
	 * Looks for a server managed by this model by its location, {@code null} is
	 * returned if no server with the passed location can be found.
	 * 
	 * @param location
	 *            a server location.
	 * @return a server or {@code null} if none can be found.
	 */
	public ConnectedServer getServerByLocation(final ServerLocation location) {
		if (location != null) {
			final Set<ConnectedServer> servers = getServers();
			for (final ConnectedServer server : servers) {
				if (server.getLocation().equals(location)) {
					return server;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the set of connected servers that can act as team servers managed by
	 * this model.
	 * 
	 * @return a set of team servers.
	 */
	public Set<ConnectedServer> getTeamServers() {
		final Set<ConnectedServer> servers = getServers();
		for (final Iterator<ConnectedServer> iterator = servers.iterator(); iterator
				.hasNext();) {
			final ConnectedServer sierraServer = iterator.next();
			if (!sierraServer.isTeamServer()) {
				iterator.remove();
			}
		}
		return servers;
	}

	/**
	 * Deletes the passed server from the set of servers managed by this model.
	 * 
	 * @param server
	 *            the server to delete.
	 */
	public void delete(final ConnectedServer server) {
		synchronized (this) {
			if (f_focus == server) {
				f_focus = null;
			}
			for (final Iterator<ConnectedServer> i = f_servers.iterator(); i
					.hasNext();) {
				final ConnectedServer entry = i.next();
				if (entry == server) {
					i.remove();
				}
			}
			for (final Iterator<Map.Entry<String, ConnectedServer>> j = f_projectNameToServer
					.entrySet().iterator(); j.hasNext();) {
				final Map.Entry<String, ConnectedServer> entry = j.next();
				if (entry.getValue() == server) {
					j.remove();
				}
			}
		}
		saveAndNotifyObservers();
	}

	/**
	 * Gets the list of server names managed by this model in alphabetical
	 * order. The array returned is a copy so mutations to the array will not
	 * affect this model.
	 * 
	 * @return the ordered list of server names.
	 */
	public String[] getNames() {
		final List<String> names = new ArrayList<String>();
		synchronized (this) {
			for (final ConnectedServer server : f_servers) {
				names.add(server.getName());
			}
		}
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Defines a server which is the current focus of this model.
	 * <p>
	 * Access to this field is protected by a lock on {@code this}.
	 */
	private ConnectedServer f_focus;

	/**
	 * Sets the passed server as the current focus of this model.
	 * 
	 * @param server
	 *            the non-null server to be the focus of this model.
	 */
	public void setFocus(final ConnectedServer server) {
		if (server == null) {
			throw new IllegalArgumentException(I18N.err(44, "server"));
		}
		boolean notify = false;
		synchronized (this) {
			if (f_focus != server) {
				f_focus = server;
				notify = true;
			}
		}
		if (notify) {
			saveAndNotifyObservers();
		}
	}

	/**
	 * Gets the server which is the current focus of this model.
	 * 
	 * @return the server which is the current focus of this model.
	 */
	public ConnectedServer getFocus() {
		synchronized (this) {
			return f_focus;
		}
	}

	/**
	 * This method changes the authorization information for a specified server.
	 * 
	 * @param server
	 *            the server to change.
	 * @param user
	 *            the new user name.
	 * @param pass
	 *            the new password.
	 * @param savePassword
	 *            {@code true} if the password should persist longer than this
	 *            session, {@code false} if not.
	 * @return the new server.
	 */
	public ConnectedServer changeAuthorizationFor(final ConnectedServer server,
			final String user, final String pass, final boolean savePassword) {
		final ConnectedServer newServer;
		synchronized (this) {
			f_servers.remove(server);
			newServer = server.changeAuthorization(user, pass, savePassword);
			f_servers.add(newServer);
		}
		saveAndNotifyObservers();
		new SynchronizeJob(null, null, server,
				ServerSyncType.BY_SERVER_SETTINGS, true,
				ServerFailureReport.SHOW_DIALOG).schedule();
		return newServer;
	}

	/**
	 * Changes out the server object with another, updated, server object.
	 * 
	 * @param from
	 *            the old server object.
	 * @param to
	 *            the new server object.
	 * @return the new server.
	 */
	public ConnectedServer changeServer(final ConnectedServer from,
			final ConnectedServer to) {
		synchronized (this) {
			f_servers.remove(from);
			f_servers.add(to);
		}
		saveAndNotifyObservers();
		new SynchronizeJob(null, null, to, ServerSyncType.BY_SERVER_SETTINGS,
				true, ServerFailureReport.SHOW_DIALOG).schedule();
		return to;
	}

	/**
	 * This method changes the setting for automatic synchronization for a
	 * specified server.
	 * 
	 * @param server
	 *            the server to change.
	 * @param autoSync
	 *            {@code true} if the the client should automatically
	 *            synchronize with this server, {@code false} if not.
	 * @return the new server.
	 */
	public ConnectedServer setAutoSyncFor(final ConnectedServer server,
			final boolean autoSync) {
		final ConnectedServer newServer;
		synchronized (this) {
			f_servers.remove(server);
			newServer = server.changeAutoSync(autoSync);
			f_servers.add(newServer);
		}
		saveAndNotifyObservers();
		return newServer;
	}

	/**
	 * The project to server connections managed by this model.
	 * <p>
	 * Access to this map is protected by a lock on {@code this}.
	 */
	private final Map<String, ConnectedServer> f_projectNameToServer = new HashMap<String, ConnectedServer>();

	public boolean isConnected(final String projectName) {
		synchronized (this) {
			return f_projectNameToServer.containsKey(projectName);
		}
	}

	public void connect(final String projectName, final ConnectedServer server) {
		if (projectName == null) {
			throw new IllegalArgumentException(I18N.err(44, "projectName"));
		}
		if (server == null) {
			throw new IllegalArgumentException(I18N.err(44, "server"));
		}
		final boolean notify;
		synchronized (this) {
			notify = server != f_projectNameToServer.put(projectName, server);
		}
		if (notify) {
			saveAndNotifyObservers();
		}
	}

	public void disconnect(final String projectName) {
		final boolean notify;
		synchronized (this) {
			notify = null != f_projectNameToServer.remove(projectName);
		}
		if (notify) {
			saveAndNotifyObservers();
		}
	}

	public ConnectedServer getServer(final String projectName) {
		synchronized (this) {
			return f_projectNameToServer.get(projectName);
		}
	}

	public List<String> getProjectsConnectedTo(final ConnectedServer server) {
		final List<String> projects = new ArrayList<String>();
		synchronized (this) {
			for (final Map.Entry<String, ConnectedServer> entry : f_projectNameToServer
					.entrySet()) {
				if (entry.getValue() == server) {
					projects.add(entry.getKey());
				}
			}
		}
		Collections.sort(projects);
		return projects;
	}

	public Set<String> getConnectedProjects() {
		final Set<String> projects;
		synchronized (this) {
			projects = new HashSet<String>(f_projectNameToServer.keySet());
		}
		return projects;
	}

	/**
	 * Track connection statistics between the client and a server within a
	 * single Eclipse session. The server is identified by a UUID.
	 * <p>
	 * Access to this map is protected by a lock on {@code this}.
	 */
	private final Map<String, ConnectedServerStats> f_uuidToServerStats = new HashMap<String, ConnectedServerStats>();

	/**
	 * Gets (or creates) the connection statistics between the client and a
	 * server within a this Eclipse session.
	 * 
	 * @return the non-null connection statistics between the client and a
	 *         server within a this Eclipse session.
	 */
	public ConnectedServerStats getStats(final ConnectedServer server) {
		if (server == null) {
			throw new IllegalArgumentException(I18N.err(44, "server"));
		}
		final String uuid = server.getUuid();
		ConnectedServerStats stats;
		synchronized (this) {
			stats = f_uuidToServerStats.get(uuid);
			if (stats == null) {
				stats = new ConnectedServerStats(server);
				f_uuidToServerStats.put(uuid, stats);
			}
		}
		return stats;
	}

	/*
	 * Fields needed for caching the password
	 */
	private static final String AUTH_SCHEME = "";
	private static final URL FAKE_URL;
	static {
		final String urlString = "http://com.surelogic.sierra";
		URL temp = null;
		try {
			temp = new URL(urlString);
		} catch (final MalformedURLException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(41, urlString), e);
		}
		FAKE_URL = temp;
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	private synchronized void save() {
		final Map<ConnectedServer, Collection<String>> servers = new HashMap<ConnectedServer, Collection<String>>();
		Map<String, String> map = Platform.getAuthorizationInfo(FAKE_URL, "",
				AUTH_SCHEME);
		if (map == null) {
			map = new java.util.HashMap<String, String>();
		}
		for (final ConnectedServer s : getServers()) {
			servers.put(s, getProjectsConnectedTo(s));
			final ServerLocation l = s.getLocation();
			if (l.isSavePassword() && l.getPass() != null) {
				map.put(l.getUser() + "@" + l.getHost(), l.getPass());
			}
		}
		try {
			if (map != null) {
				Platform.addAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME, map);
			}
			Data.getInstance().withTransaction(
					ServerLocations.saveQuery(servers, false));
		} catch (final TransactionException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(38, "Team Servers", "database"), e);
		} catch (final CoreException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(42), e);
		}
	}

	/**
	 * @return {@code} if any changes were made, {@code false} otherwise.
	 */
	@SuppressWarnings( { "deprecation", "unchecked" })
	private boolean load() {
		final Map<String, String> passwords = Platform.getAuthorizationInfo(
				FAKE_URL, "", AUTH_SCHEME);
		try {
			final Map<ConnectedServer, Collection<String>> map = Data
					.getInstance().withReadOnly(
							ServerLocations.fetchQuery(passwords));
			final Set<ConnectedServer> servers = new HashSet<ConnectedServer>(
					f_servers);
			final Map<String, ConnectedServer> projects = new HashMap<String, ConnectedServer>();
			f_projectNameToServer.clear();
			f_servers.clear();
			for (final Entry<ConnectedServer, Collection<String>> entry : map
					.entrySet()) {
				final ConnectedServer s = entry.getKey();
				f_servers.add(s);
				for (final String project : entry.getValue()) {
					connect(project, s);
				}
			}
			// Check to see if anything has changed.
			// Do we still have the same connected servers?
			if (!servers.equals(map.keySet())) {
				return true;
			}
			// Are all of the new project associations the same as old project
			// associations?
			for (final ConnectedServer s : servers) {
				final Collection<String> projs = map.get(s);
				for (final String project : projs) {
					final ConnectedServer last = projects.get(project);
					if (last != s && !last.equals(s)) {
						return true;
					}
				}
			}
			// Are all of the old project associations the same as new project
			// associations?
			for (final Entry<String, ConnectedServer> entry : projects
					.entrySet()) {
				if (!map.get(entry.getValue()).contains(entry.getKey())) {
					return true;
				}
			}
			return false;

		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure loading connected server data", e);
		} finally {
			try {
				Platform.flushAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME);
			} catch (final CoreException e) {
				SLLogger.getLogger().log(Level.SEVERE, I18N.err(42), e);
			}
		}
		// TODO change this to check for real if something changed.
		return true;
	}

	/**
	 * A method that saves the servers managed by this model to the database and
	 * then notifies observers of this model that something has changed.
	 * <p>
	 * Never call this method holding a lock on {@code this} as we are calling
	 * foreign code.
	 */
	private void saveAndNotifyObservers() {
		save();
		notifyObservers();
	}

	@Override
	protected void notifyThisObserver(final ISierraServerObserver o) {
		o.notify(this);
	}

	@Override
	public void serverSynchronized() {
		/*
		 * This could have updated the information we care about.
		 */
		if (load()) {
			// We don't need to save because we just loaded :-)
			notifyObservers();
		}
	}
}
