package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
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
     * This map holds all the scanned projects in the database. The key is the
     * project name. The value is an immutable container of information about
     * the project
     */
    private final ConcurrentHashMap<String, ScannedProject> f_nameToScannedProject = new ConcurrentHashMap<String, ScannedProject>();

    /**
     * The value counts the number of consecutive server connection failures for
     * the project used as the map key, if any. This is a sparse map, nothing
     * defined means no prior server failures.
     */
    private final ConcurrentHashMap<String, AtomicInteger> f_nameToConsecutiveConnectFailures = new ConcurrentHashMap<String, AtomicInteger>();

    // TODO NOBODY CALLS THIS????
    public void markAsConnectedSuccessfully(final String projectName) {
        f_nameToConsecutiveConnectFailures.remove(projectName);
    }

    // TODO NOBODY CALLS THIS????
    public int incrementConsecutiveConnectFailuresFor(final String projectName) {
        int result = 0;
        final AtomicInteger count = f_nameToConsecutiveConnectFailures
                .get(projectName);
        if (count != null) {
            result = count.incrementAndGet();
        } else {
            result = 1;
            f_nameToConsecutiveConnectFailures.put(projectName,
                    new AtomicInteger(result));
        }
        notifyObservers();
        return result;
    }

    // TODO YET THIS GETS CALLED (probably returning a constant 0)
    public int getConsecutiveConnectFailuresFor(final String projectName) {
        final AtomicInteger count = f_nameToConsecutiveConnectFailures
                .get(projectName);
        return count == null ? 0 : count.get();
    }

    @Nullable
    public ScannedProject getScannedProjectFor(final String projectName) {
        return f_nameToScannedProject.get(projectName);
    }

    /**
     * Gets the set of project names in the database.
     *
     * @return the set of project names in the database.
     */
    @NonNull
    public ArrayList<String> getProjectNames() {
        return new ArrayList<String>(f_nameToScannedProject.keySet());
    }

    /**
     * Gets the set of scanned projects in the database.
     *
     * @return the set of scanned projects in the database.
     */
    @NonNull
    public ArrayList<ScannedProject> getScannedProjects() {
        return new ArrayList<ScannedProject>(f_nameToScannedProject.values());
    }

    /**
     * Gets the set of project names in the database.
     *
     * @return the set of project names in the database.
     */
    @NonNull
    public String[] getProjectNamesArray() {
        final List<String> projectNames = getProjectNames();
        return projectNames.toArray(new String[projectNames.size()]);
    }

    /**
     * Returns <code>true</code> if the database contains no projects.
     *
     * @return <code>true</code> if the database contains no projects.
     */
    public boolean isEmpty() {
        return f_nameToScannedProject.isEmpty();
    }

    /**
     * Checks if a project name exists in the database.
     *
     * @param projectName
     *            a project name.
     * @return <code>true</code> if the given project name exists in the
     *         database, <code>false</code> otherwise.
     */
    public boolean contains(final String projectName) {
        return f_nameToScannedProject.containsKey(projectName);
    }

    public void refresh() {
        final Map<String, ScannedProject> nameToScannedProject = new HashMap<String, ScannedProject>();
        try {
            final Connection c = Data.getInstance().readOnlyConnection();
            try {
                final Statement st = c.createStatement();
                try {
                    final ResultSet rs = st.executeQuery(QB.get(1));
                    try {
                        while (rs.next()) {
                            @NonNull
                            final String name = rs.getString(1);
                            @NonNull
                            final Date whenScanned = rs.getTimestamp(2);
                            @Nullable
                            final Date whenScannedPreviously = rs
                                    .getTimestamp(3);
                            @Nullable
                            final String exclusionFilter = rs.getString(4);
                            final ScannedProject info = new ScannedProject(
                                    name, whenScanned, whenScannedPreviously,
                                    exclusionFilter);
                            nameToScannedProject.put(name, info);
                        }
                    } finally {
                        rs.close();
                    }
                } catch (final SQLException e) {
                    SLLogger.getLogger()
                            .log(Level.SEVERE,
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
        /*
         * There is a very small chance that something could see into this group
         * of operations.
         */
        if (!f_nameToScannedProject.equals(nameToScannedProject)) {
            f_nameToScannedProject.clear();
            f_nameToScannedProject.putAll(nameToScannedProject);
            notify = true;
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
        return "[" + Projects.class.getName() + ": " + getProjectNames() + "]";
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
