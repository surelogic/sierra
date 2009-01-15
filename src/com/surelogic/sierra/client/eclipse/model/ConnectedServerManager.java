package com.surelogic.sierra.client.eclipse.model;

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

import com.surelogic.common.ILifecycle;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ServerLocations;

/**
 * Manages the set of connected servers.
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

	// static public final String BUGLINK_ORG = "buglink.org";
	// static public final String BUGLINK_USER = "buglink-user";
	// static public final String BUGLINK_PASS = "bl!uzer";
	//
	// public boolean isThereABugLinkOrg() {
	// return exists(BUGLINK_ORG);
	// }
	//
	// public void createBugLinkOrg() {
	// final SierraServer server = getOrCreate(BUGLINK_ORG);
	// server.setHost("buglink.org");
	// }
	//
	// public SierraServer getOrCreateBugLinkOrg() {
	// if (!isThereABugLinkOrg()) {
	// createBugLinkOrg();
	// }
	// return getOrCreate(BUGLINK_ORG);
	// }

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
		notifyObservers();
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
			notifyObservers();
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
			notifyObservers();
		}
	}

	public void disconnect(final String projectName) {
		final boolean notify;
		synchronized (this) {
			notify = null != f_projectNameToServer.remove(projectName);
		}
		if (notify) {
			notifyObservers();
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

	private synchronized void save() {
		final Map<ConnectedServer, Collection<String>> map = new HashMap<ConnectedServer, Collection<String>>();
		for (final ConnectedServer s : getServers()) {
			map.put(s, getProjectsConnectedTo(s));
		}
		Data.getInstance().withTransaction(ServerLocations.saveQuery(map));
	}

	private void load() {
		try {
			final Map<ConnectedServer, Collection<String>> map = Data
					.getInstance().withReadOnly(
							ServerLocations.fetchQuery(null));
			f_projectNameToServer.clear();
			f_servers.clear();
			for (final Entry<ConnectedServer, Collection<String>> entry : map
					.entrySet()) {
				final ConnectedServer s = entry.getKey();
				f_servers.add(s);
				for (final String project : entry.getValue()) {
					f_projectNameToServer.put(project, s);
				}
			}
		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure loading connected server data", e);
		}
	}

	@Override
	public void notifyObservers() {
		/*
		 * First save all changes to the database.
		 */
		save();
		super.notifyObservers();
	}

	@Override
	protected void notifyThisObserver(final ISierraServerObserver o) {
		o.notify(this);
	}
}
