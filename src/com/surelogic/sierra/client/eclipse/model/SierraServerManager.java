package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SierraServerManager {

	/**
	 * Maps servers by their name.
	 */
	final Map<String, SierraServerModel> f_nameToServer = new HashMap<String, SierraServerModel>();

	/**
	 * Checks if a given name is the name for an existing server.
	 * 
	 * @param name
	 *            a server name.
	 * @return <code>true</code> if the name is used for an existing server,
	 *         <code>false</code> otherwise.
	 */
	public boolean exists(final String name) {
		return f_nameToServer.containsKey(name);
	}

	/**
	 * Gets the server with the passed name. If the server does not exist then
	 * it is created.
	 * 
	 * @param name
	 *            a server name.
	 * @return the server with the passed name.
	 */
	public SierraServerModel getOrCreate(final String name) {
		if (name == null)
			throw new IllegalArgumentException("name must be non-null");
		SierraServerModel server = f_nameToServer.get(name);
		if (server == null) {
			server = new SierraServerModel(this, name);
			notifyObservers();
		}
		return server;
	}

	/**
	 * Creates a server with a unique name. The new server is set as the focus
	 * query.
	 * 
	 * @return the new server.
	 */
	public SierraServerModel create() {
		final String name = newUniqueName("server");
		final SierraServerModel query = new SierraServerModel(this, name);
		setFocus(query);
		return query;
	}

	/**
	 * Creates a duplicate of the current focus server with a new unique name.
	 * The new server becomes the focus of this model.
	 */
	public void duplicate() {
		final SierraServerModel server = getFocus();
		if (server != null) {
			final String name = newUniqueName(server.getLabel());
			final SierraServerModel newServer = new SierraServerModel(this,
					name);
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
	 * Creates a unique server name. For example, given a prefix of
	 * <code>"server"</code>, it could return <code>"server (1)"</code>.
	 * 
	 * @param prefix
	 *            non-null prefix for the name.
	 * @return a unique server name.
	 */
	private String newUniqueName(String prefix) {
		// strip off any previous number, e.g., (1)
		if (prefix.endsWith(")")) {
			int index = prefix.lastIndexOf("(");
			if (index != -1 && index > 1) {
				if (prefix.charAt(index - 1) == ' ')
					prefix = prefix.substring(0, index - 1);
			}
		}
		// create a new name
		int id = 1;
		String name;
		do {
			name = prefix + " (" + id++ + ")";
		} while (exists(name));
		return name;
	}

	/**
	 * Gets the ordered list of server names managed by this model. The array
	 * returned is a copy so mutations to the array will not affect this model.
	 * 
	 * @return the ordered list of server names.
	 */
	public String[] getNames() {
		List<String> f_names = new ArrayList<String>(f_nameToServer.keySet());
		Collections.sort(f_names);
		return f_names.toArray(new String[f_names.size()]);
	}

	/**
	 * Defines a server which is the current focus of this model.
	 */
	private SierraServerModel f_focus;

	/**
	 * Sets the passed server as the current focus of this model.
	 * 
	 * @param server
	 *            the non-null server to be the focus of this model.
	 */
	public void setFocus(final SierraServerModel server) {
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
	public SierraServerModel getFocus() {
		return f_focus;
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
		// TODO: persist this model
	}

	public void load(File file) {
		// TODO: load this model.
	}
}
