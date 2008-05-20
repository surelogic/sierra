package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.jdbc.QB;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;

/**
 * A singleton that tracks the BugLink data in the client database 
 * <p>
 * The class allows observers to changes to the BugLink data
 */
public final class BuglinkData extends DatabaseObservable<IBuglinkDataObserver> {

	private static final BuglinkData INSTANCE = new BuglinkData();

	static {
		DatabaseHub.getInstance().addObserver(INSTANCE);
	}

	public static BuglinkData getInstance() {
		return INSTANCE;
	}

	private BuglinkData() {
		// singleton
	}

	@Override
	protected void notifyObserver(IBuglinkDataObserver o) {
		o.notify(this);		
	}
	
	public void refresh() {
		List<String> projectNames = new ArrayList<String>();
		try {
			final Connection c = Data.readOnlyConnection();
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
			if (false /*equal*/) {
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
			return "[" + BuglinkData.class.getName() + "]";
		}
	}

	/*
	 * Track changes to the database that can mutate the set of projects.
	 */
    /*
	@Override
	public void projectDeleted() {
		refresh();
	}

	@Override
	public void scanLoaded() {
		refresh();
	}
    */
}
