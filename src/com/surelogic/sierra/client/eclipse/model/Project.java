package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;

/**
 * A singleton that tracks the set of projects in the client database that have
 * analysis results about them.
 * <p>
 * This class relies upon the <code>PROJECT_OVERVIEW</code> view within the
 * client database to obtain the list of projects.
 * <p>
 * The class allows observers to changes to this list of projects
 */
public final class Project {

	private static final String QUERY = "select PROJECT from PROJECT_OVERVIEW order by PROJECT";

	private static final Project INSTANCE = new Project();

	public static Project getInstance() {
		return INSTANCE;
	}

	private Project() {
		// singleton
	}

	private final Set<IProjectObserver> f_observers = new CopyOnWriteArraySet<IProjectObserver>();

	public void addObserver(final IProjectObserver o) {
		f_observers.add(o);
	}

	public void removeObserver(final IProjectObserver o) {
		f_observers.remove(o);
	}

	private void notifyObservers() {
		for (IProjectObserver o : f_observers)
			o.notify(this);
	}

	/**
	 * Protected by a lock on <code>this</code>.
	 */
	private final List<String> f_projectNames = new ArrayList<String>();

	public synchronized List<String> getProjectNames() {
		return new ArrayList<String>(f_projectNames);

	}

	public void refresh() {
		List<String> projectNames = new ArrayList<String>();
		try {
			final Connection c = Data.getConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final ResultSet rs = st.executeQuery(QUERY);
					while (rs.next()) {
						projectNames.add(rs.getString(1));
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Unable to read the list of projects in the database", e);
		}
		boolean notify = false;
		synchronized (this) {
			if (!f_projectNames.equals(projectNames)) {
				f_projectNames.clear();
				f_projectNames.addAll(projectNames);
				notify = true;
			}
		}
		if (notify)
			notifyObservers();
	}

	@Override
	public String toString() {
		/*
		 * Show the list of projects that we read from the database.
		 */
		synchronized (this) {
			return f_projectNames.toString();
		}
	}
}
