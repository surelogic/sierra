package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
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
		refresh(); // project names from the database
	}

	private final Set<IProjectsObserver> f_observers = new CopyOnWriteArraySet<IProjectsObserver>();

	public void addObserver(final IProjectsObserver o) {
		if (o == null)
			return;
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
	private final LinkedList<String> f_projectNames = new LinkedList<String>();

	/**
	 * Gets the set of project names in the database.
	 * 
	 * @return the set of project names in the database.
	 */
	public synchronized List<String> getProjectNames() {
		return new ArrayList<String>(f_projectNames);
	}

	/**
	 * Gets the set of project names in the database.
	 * 
	 * @return the set of project names in the database.
	 */
	public synchronized String[] getProjectNamesArray() {
		return f_projectNames.toArray(new String[f_projectNames.size()]);
	}

	/**
	 * Returns <code>true</code> if the database contains no projects.
	 * 
	 * @return <code>true</code> if the database contains no projects.
	 */
	public synchronized boolean isEmpty() {
		return f_projectNames.isEmpty();
	}

	/**
	 * Checks if a project name exists in the database.
	 * 
	 * @param projectName
	 *            a project name.
	 * @return <code>true</code> if the given project name exists in the
	 *         database, <code>false</code> otherwise.
	 */
	public synchronized boolean contains(final String projectName) {
		return f_projectNames.contains(projectName);
	}

	/**
	 * Returns the first project in the database.
	 * 
	 * @return the first project in the database.
	 */
	public synchronized String getFirst() {
		return f_projectNames.getFirst();
	}

	private void refresh() {
		List<String> projectNames = new ArrayList<String>();
		try {
			final Connection c = Data.readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final ResultSet rs = st.executeQuery(QUERY);
					while (rs.next()) {
						projectNames.add(rs.getString(1));
					}
				} catch (SQLException e) {
					SLLogger
							.getLogger()
							.log(
									Level.SEVERE,
									"Unable to read the list of projects in the database",
									e);
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
