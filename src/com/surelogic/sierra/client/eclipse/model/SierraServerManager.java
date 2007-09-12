package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
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

public final class SierraServerManager {

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
	public boolean exists(final String label) {
		return f_labelToServer.containsKey(label);
	}

	public boolean isEmpty() {
		return f_labelToServer.isEmpty();
	}

	public Set<SierraServer> getServers() {
		Set<SierraServer> servers = new HashSet<SierraServer>(f_labelToServer
				.values());
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
		if (label == null)
			throw new IllegalArgumentException("label must be non-null");
		SierraServer server = f_labelToServer.get(label);
		if (server == null) {
			server = new SierraServer(this, label);
			notifyObservers();
		}
		return server;
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

	public void delete(SierraServer server) {
		if (server.getManager() != this) {
			SLLogger.getLogger().log(
					Level.WARNING,
					"A server can only be deleted from its associated manager : "
							+ server);
			return;
		}
		if (f_focus == server)
			f_focus = null;
		for (Iterator<Map.Entry<String, SierraServer>> i = f_labelToServer
				.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, SierraServer> entry = i.next();
			if (entry.getValue() == server) {
				i.remove();
			}
		}
		notifyObservers();
	}

	public void delete(String label) {
		SierraServer server = f_labelToServer.get(label);
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
			int index = prefix.lastIndexOf("(");
			if (index != -1 && index > 1) {
				if (prefix.charAt(index - 1) == ' ')
					prefix = prefix.substring(0, index - 1);
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
	public String[] getLabels() {
		List<String> labels = new ArrayList<String>(f_labelToServer.keySet());
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
		if (server == null)
			throw new IllegalArgumentException("server must be non-null");
		f_focus = server;
		notifyObservers();
	}

	/**
	 * Gets the server which is the current focus of this model.
	 * 
	 * @return the server which is the current focus of this model.
	 */
	public SierraServer getFocus() {
		return f_focus;
	}

	private Map<String, SierraServer> f_projectNameToServer = new HashMap<String, SierraServer>();

	public boolean isConnected(String projectName) {
		return f_projectNameToServer.containsKey(projectName);
	}

	public void connect(String projectName, SierraServer server) {
		if (projectName == null)
			throw new IllegalArgumentException("project name must be non-null.");
		if (server == null)
			throw new IllegalArgumentException("server must be non-null.");
		f_projectNameToServer.put(projectName, server);
		notifyObservers();

	}

	public void disconnect(String projectName) {
		if (f_projectNameToServer.remove(projectName) != null)
			notifyObservers();
	}

	public SierraServer getServer(String projectName) {
		return f_projectNameToServer.get(projectName);
	}

	public List<String> getProjectsConnectedTo(SierraServer server) {
		List<String> projects = new ArrayList<String>();
		for (Map.Entry<String, SierraServer> entry : f_projectNameToServer
				.entrySet()) {
			if (entry.getValue() == server) {
				projects.add(entry.getKey());
			}
		}
		Collections.sort(projects);
		return projects;
	}

	public Set<String> getConnectedProjects() {
		Set<String> projects = new HashSet<String>(f_projectNameToServer
				.keySet());
		return projects;
	}

	/**
	 * The set of observers to changes to the state of this class.
	 */
	private Set<ISierraServerObserver> f_serverObservers = new HashSet<ISierraServerObserver>();

	/**
	 * Registers a {@link ISierraServerObserver} object to receive notifications
	 * of changes to the state of this class.
	 * 
	 * @param o
	 *            the observer.
	 */
	public void addObserver(final ISierraServerObserver o) {
		f_serverObservers.add(o);
	}

	/**
	 * Removes a {@link SierraServerObserver} object from the set of registered
	 * observers.
	 * 
	 * @param o
	 *            the observer.
	 */
	public void removeObserver(final ISierraServerObserver o) {
		f_serverObservers.remove(o);
	}

	/**
	 * Notifies all registered {@link ISierraServerObserver} objects.
	 */
	public void notifyObservers() {
		for (ISierraServerObserver o : f_serverObservers)
			o.notify(this);
	}

	public void save(File file) {
		SierraServerPersistence.save(this, file);
	}

	public void load(File file) throws Exception {
		SierraServerPersistence.load(this, file);
	}
}
