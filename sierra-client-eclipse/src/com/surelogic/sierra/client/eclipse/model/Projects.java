package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.surelogic.common.ILifecycle;
import com.surelogic.common.jdbc.QB;
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
public final class Projects extends DatabaseObservable<IProjectsObserver>
		implements ILifecycle {

	private static final Projects INSTANCE = new Projects();

	public static Projects getInstance() {
		return INSTANCE;
	}

	private Projects() {
		// singleton
	}

	@Override
  public void init() {
		DatabaseHub.getInstance().addObserver(this);
		refresh();
	}

	@Override
  public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
	}

	@Override
	protected void notifyThisObserver(final IProjectsObserver o) {
		o.notify(this);
	}

	/**
	 * Protected by a lock on <code>this</code>.
	 */
	private final LinkedList<String> f_projectNames = new LinkedList<String>();

	/**
	 * Counts of consecutive server project failures
	 */
	private final Map<String, Integer> projectProblems = new HashMap<String, Integer>();

	private <T> int incrProblem(final Map<T, Integer> map, final T key) {
		Integer count = map.get(key);
		count = (count == null) ? 1 : count + 1;
		map.put(key, count);
		return count;
	}

	public synchronized void markAsConnected(final String name) {
		projectProblems.remove(name);
	}

	public synchronized int encounteredProblem(final String name) {
		try {
			return incrProblem(projectProblems, name);
		} finally {
			notifyObservers();
		}
	}

	public synchronized int getProblemCount(final String name) {
		final Integer count = projectProblems.get(name);
		return count == null ? 0 : count;
	}

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

	public void refresh() {
		final List<String> projectNames = new ArrayList<String>();
		try {
			final Connection c = Data.getInstance().readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final ResultSet rs = st.executeQuery(QB.get(1));
					try {
						while (rs.next()) {
							projectNames.add(rs.getString(1));
						}
					} finally {
						rs.close();
					}
				} catch (final SQLException e) {
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
		} catch (final SQLException e) {
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
		if (notify) {
			notifyObservers();
		}
	}

	@Override
	public String toString() {
		/*
		 * Show the list of projects that we read from the database.
		 */
		synchronized (this) {
			return "[" + Projects.class.getName() + ": "
					+ f_projectNames.toString() + "]";
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
	public void databaseDeleted() {
		refresh();
	}

	@Override
	public void scanLoaded() {
		refresh();
	}
}
