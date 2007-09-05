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
public final class Projects extends AbstractDatabaseObserver {

	private static final String QUERY = "select PROJECT from PROJECT_OVERVIEW order by PROJECT";

	private static final Projects INSTANCE = new Projects();

	static {
		DatabaseHub.getInstance().addObserver(INSTANCE);
	}

	public static Projects getInstance() {
		return INSTANCE;
	}

	private Projects() {
		// singleton
	}

	private final Set<IProjectsObserver> f_observers = new CopyOnWriteArraySet<IProjectsObserver>();

	public void addObserver(final IProjectsObserver o) {
		f_observers.add(o);
	}

	public void removeObserver(final IProjectsObserver o) {
		f_observers.remove(o);
	}

	private void notifyObservers() {
		for (IProjectsObserver o : f_observers)
			o.notify(this);
	}

	/**
	 * Protected by a lock on <code>this</code>.
	 */
	private final List<String> f_projectNames = new ArrayList<String>();

	/**
	 * Gets the set of project names in the database.
	 * 
	 * @return the set of project names in the database.
	 */
	public synchronized List<String> getProjectNames() {
		return new ArrayList<String>(f_projectNames);
	}

	/**
	 * Checks if a project name exists in the database.
	 * 
	 * @param projectName
	 *            a project name.
	 * @return <code>true</code> if the given project name exists in the
	 *         database, <code>false</code> otherwise.
	 */
	public boolean exists(final String projectName) {
		return f_projectNames.contains(projectName);
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

	/*
	 * Track changes to the database that can mutate the set of projects.
	 */

	@Override
	public void projectDeleted() {
		refresh();
	}

	@Override
	public void scanLoaded() {
		refresh();
	}
}
