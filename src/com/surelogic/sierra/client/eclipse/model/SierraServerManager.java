package com.surelogic.sierra.client.eclipse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public final class SierraServerManager extends
		DatabaseObservable<ISierraServerObserver> {

	public static final SierraServerManager INSTANCE = new SierraServerManager();

	public static SierraServerManager getInstance() {
		return INSTANCE;
	}

	private SierraServerManager() {
		// singleton
	}

	/**
	 * Maps servers by their label.
	 */
	final Map<String, SierraServer> f_labelToServer = new HashMap<String, SierraServer>();

	/**
	 * Checks if a given label is the label for an existing server.
	 * 
	 * @param label
	 *            a server label.
	 * @return <code>true</code> if the label is used for an existing server,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean exists(final String label) {
		return f_labelToServer.containsKey(label);
	}

	public synchronized boolean isEmpty() {
		return f_labelToServer.isEmpty();
	}

	public synchronized Set<SierraServer> getServers() {
		final Set<SierraServer> servers = new HashSet<SierraServer>(
				f_labelToServer.values());
		return servers;
	}

	public Set<SierraServer> getTeamServers() {
		final Set<SierraServer> servers = getServers();
		for (final Iterator<SierraServer> iterator = servers.iterator(); iterator
				.hasNext();) {
			final SierraServer sierraServer = iterator.next();
			if (!sierraServer.isTeamServer()) {
				iterator.remove();
			}
		}
		return servers;
	}

	/**
	 * Gets the server with the passed label. If the server does not exist then
	 * it is created.
	 * 
	 * @param label
	 *            a server label.
	 * @return the server with the passed label.
	 */
	public SierraServer getOrCreate(final String label) {
		if (label == null) {
			throw new IllegalArgumentException("label must be non-null");
		}
		boolean created = false;
		SierraServer server;

		synchronized (this) {
			server = f_labelToServer.get(label);
			if (server == null) {
				server = new SierraServer(this, label);
				server.setPassword(BUGLINK_PASS);
				server.setUser(BUGLINK_USER);
				server.setSavePassword(true);
				created = true;
			}
		}
		if (created) {
			notifyObservers();
		}
		return server;
	}

	static public final String BUGLINK_ORG = "buglink.org";
	static public final String BUGLINK_USER = "buglink-user";
	static public final String BUGLINK_PASS = "bl!uzer";

	public boolean isThereABugLinkOrg() {
		return exists(BUGLINK_ORG);
	}

	public void createBugLinkOrg() {
		final SierraServer server = getOrCreate(BUGLINK_ORG);
		server.setHost("buglink.org");
	}

	public SierraServer getOrCreateBugLinkOrg() {
		if (!isThereABugLinkOrg()) {
			createBugLinkOrg();
		}
		return getOrCreate(BUGLINK_ORG);
	}

	/**
	 * Creates a server with a unique label. The new server is set as the focus
	 * server.
	 * 
	 * @return the new server.
	 */
	public SierraServer create() {
		final String label = newUniqueLabel("server");
		final SierraServer query = new SierraServer(this, label);
		setFocus(query);
		return query;
	}

	/**
	 * Creates a new {@link SierraServerLocation} for editing.
	 * 
	 * @return a new server location object.
	 */
	public SierraServerLocation createLocation() {
		final String label = newUniqueLabel("server");
		return new SierraServerLocation(label, "", false,
				SierraServerLocation.DEFAULT_PORT,
				SierraServerLocation.DEFAULT_PATH, "", "", false);
	}

	/**
	 * Deletes the passed server from the set of server managed by this.
	 * 
	 * @param server
	 *            the server to delete.
	 */
	public void delete(final SierraServer server) {
		synchronized (this) {
			if (server.getManager() != this) {
				SLLogger.getLogger().log(
						Level.WARNING,
						"A server can only be deleted from its associated manager : "
								+ server);
				return;
			}
			if (f_focus == server) {
				f_focus = null;
			}
			for (final Iterator<Map.Entry<String, SierraServer>> i = f_labelToServer
					.entrySet().iterator(); i.hasNext();) {
				final Map.Entry<String, SierraServer> entry = i.next();
				if (entry.getValue() == server) {
					i.remove();
				}
			}
			for (final Iterator<Map.Entry<String, SierraServer>> j = f_projectNameToServer
					.entrySet().iterator(); j.hasNext();) {
				final Map.Entry<String, SierraServer> entry = j.next();
				if (entry.getValue() == server) {
					j.remove();
				}
			}
		}
		notifyObservers();
	}

	/**
	 * Deletes the server identified by the passed label from this server if it
	 * exists.
	 * 
	 * @param label
	 *            of the server to delete.
	 */
	public synchronized void delete(final String label) {
		final SierraServer server = f_labelToServer.get(label);
		if (server != null) {
			delete(server);
		}
	}

	/**
	 * Creates a duplicate of the current focus server with a new unique label.
	 * The new server becomes the focus of this model.
	 */
	public void duplicate() {
		final SierraServer server = getFocus();
		if (server != null) {
			final String label = newUniqueLabel(server.getLabel());
			final SierraServer newServer = new SierraServer(this, label);
			newServer.setHost(server.getHost());
			newServer.setPassword(server.getPassword());
			newServer.setPort(server.getPort());
			newServer.setSavePassword(server.savePassword());
			newServer.setAutoSync(server.autoSync());
			newServer.setSecure(server.isSecure());
			newServer.setUser(server.getUser());
			setFocus(newServer);
		}
	}

	/**
	 * Creates a unique server label. For example, given a prefix of
	 * <code>"server"</code>, it could return <code>"server (1)"</code>.
	 * 
	 * @param prefix
	 *            non-null prefix for the label.
	 * @return a unique server label.
	 */
	private String newUniqueLabel(String prefix) {
		// strip off any previous number, e.g., (1)
		if (prefix.endsWith(")")) {
			final int index = prefix.lastIndexOf('(');
			if ((index != -1) && (index > 1)) {
				if (prefix.charAt(index - 1) == ' ') {
					prefix = prefix.substring(0, index - 1);
				}
			}
		}
		// create a new label
		int id = 1;
		String label;
		do {
			label = prefix + " (" + id++ + ")";
		} while (exists(label));
		return label;
	}

	/**
	 * Gets the list of server labels managed by this model in alphabetical
	 * order. The array returned is a copy so mutations to the array will not
	 * affect this model.
	 * 
	 * @return the ordered list of server labels.
	 */
	public synchronized String[] getLabels() {
		final List<String> labels = new ArrayList<String>(f_labelToServer
				.keySet());
		Collections.sort(labels);
		return labels.toArray(new String[labels.size()]);
	}

	/**
	 * Defines a server which is the current focus of this model.
	 */
	private SierraServer f_focus;

	/**
	 * Sets the passed server as the current focus of this model.
	 * 
	 * @param server
	 *            the non-null server to be the focus of this model.
	 */
	public void setFocus(final SierraServer server) {
		if (server == null) {
			throw new IllegalArgumentException("server must be non-null");
		}
		if (f_focus != server) {
			f_focus = server;
			notifyObservers();
		}
	}

	/**
	 * Gets the server which is the current focus of this model.
	 * 
	 * @return the server which is the current focus of this model.
	 */
	public SierraServer getFocus() {
		return f_focus;
	}

	private final Map<String, SierraServer> f_projectNameToServer = new HashMap<String, SierraServer>();

	public boolean isConnected(final String projectName) {
		return f_projectNameToServer.containsKey(projectName);
	}

	public void connect(final String projectName, final SierraServer server) {
		if (projectName == null) {
			throw new IllegalArgumentException("project name must be non-null.");
		}
		if (server == null) {
			throw new IllegalArgumentException("server must be non-null.");
		}
		f_projectNameToServer.put(projectName, server);
		notifyObservers();

	}

	public void disconnect(final String projectName) {
		if (f_projectNameToServer.remove(projectName) != null) {
			notifyObservers();
		}
	}

	public SierraServer getServer(final String projectName) {
		return f_projectNameToServer.get(projectName);
	}

	public List<String> getProjectsConnectedTo(final SierraServer server) {
		final List<String> projects = new ArrayList<String>();
		for (final Map.Entry<String, SierraServer> entry : f_projectNameToServer
				.entrySet()) {
			if (entry.getValue() == server) {
				projects.add(entry.getKey());
			}
		}
		Collections.sort(projects);
		return projects;
	}

	public Set<String> getConnectedProjects() {
		final Set<String> projects = new HashSet<String>(f_projectNameToServer
				.keySet());
		return projects;
	}

	/**
	 * Notifies all registered {@link ISierraServerObserver} objects.
	 */
	@Override
	protected void notifyObserver(final ISierraServerObserver o) {
		o.notify(this);
	}

	public synchronized void save() {
		SierraServerPersistence.save(this);
	}

	public void load() throws Exception {
		SierraServerPersistence.load(this);
		/*
		 * Add BugLink server if necessary
		 */
		getOrCreateBugLinkOrg();
	}

	public synchronized SierraServer getServerByLabel(final String label) {
		if (label == null) {
			return null;
		}
		return f_labelToServer.get(label);
	}

	@Override
	public void notifyObservers() {
		save();
		super.notifyObservers();
	}
}
