package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.views.selection.MListOfFindingsColumn;

/**
 * Defines a selection of findings using a series of filters.
 * <p>
 * This class is thread-safe.
 */
public final class Selection extends AbstractDatabaseObserver {

	/**
	 * Immutable set of all possible filters.
	 */
	private static final Set<ISelectionFilterFactory> f_allFilters;
	static {
		Set<ISelectionFilterFactory> allFilters = new HashSet<ISelectionFilterFactory>();
		/*
		 * Add in all the filter factories.
		 */
		allFilters.add(FilterArtifactCount.FACTORY);
		allFilters.add(FilterAuditCount.FACTORY);
		allFilters.add(FilterAudited.FACTORY);
		allFilters.add(FilterAdHocFindingCategory.FACTORY);
		//allFilters.add(FilterFindingCategory.FACTORY);
		allFilters.add(FilterFindingType.FACTORY);
		allFilters.add(FilterImportance.FACTORY);
		allFilters.add(FilterJavaClass.FACTORY);
		allFilters.add(FilterJavaPackage.FACTORY);
		allFilters.add(FilterProject.FACTORY);
		allFilters.add(FilterSelection.FACTORY);
		allFilters.add(FilterTool.FACTORY);		
		allFilters.add(FilterVerificationStatus.FACTORY);
		allFilters.add(FilterResultType.FACTORY);
		allFilters.add(FilterModels.FACTORY);	
		f_allFilters = Collections.unmodifiableSet(allFilters);
	}

	/**
	 * Gets the immutable set of all possible filters.
	 * 
	 * @return the immutable set of all possible filters.
	 */
	public static Set<ISelectionFilterFactory> getAllFilters() {
		return f_allFilters;
	}

	Selection(SelectionManager manager) {
		assert manager != null;
		f_manager = manager;
	}

	public Selection(Selection source) {
		synchronized (source) {
			f_manager = source.f_manager;
			f_showingFindings = source.f_showingFindings;

			Filter prev = null;
			for (Filter f : source.f_filters) {
				Filter clone = f.copyNoQuery(this, prev);
				prev = clone;
				f_filters.add(clone);
			}
			for (Map.Entry<String, Column> e : source.f_columns.entrySet()) {
				Column c = getColumn(e.getKey());
				Column sc = e.getValue();
				c.configure(sc);
			}
		}
	}

	/**
	 * This just connects this filter to the database. Making it reflect changes
	 * to the database. Ensure that {@link #dispose()} is called to disconnect
	 * this selection from the database when the selection is no longer used.
	 */
	public void initAndSyncToDatabase() {
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
		synchronized (this) {
			for (Filter f : f_filters)
				f.dispose();
			f_observers.clear();
		}
	}

	private final SelectionManager f_manager;

	public SelectionManager getManager() {
		/*
		 * Not mutable so we don't need to hold a lock on this.
		 */
		return f_manager;
	}

	/**
	 * The ordered list of filters within this selection.
	 */
	private final LinkedList<Filter> f_filters = new LinkedList<Filter>();

	/**
	 * Gets the ordered list of filters managed by this Selection;
	 * 
	 * @return
	 */
	public final List<Filter> getFilters() {
		synchronized (this) {
			return new LinkedList<Filter>(f_filters);
		}
	}

	/**
	 * Indicates if the passed filter is the first filter of this selection.
	 * 
	 * @param filter
	 *            a filter within this selection.
	 * @return <code>true</code> if the passed filter is the first filter of
	 *         this selection, <code>false</code> otherwise.
	 */
	public boolean isFirstFilter(Filter filter) {
		return f_filters.getFirst() == filter;
	}

	/**
	 * Indicates if the passed filter is the last filter of this selection.
	 * 
	 * @param filter
	 *            a filter within this selection.
	 * @return <code>true</code> if the passed filter is the last filter of
	 *         this selection, <code>false</code> otherwise.
	 */
	public boolean isLastFilter(Filter filter) {
		return f_filters.getLast() == filter;
	}

	/**
	 * Removes all the passed filter and all subsequent filters from this
	 * selection.
	 * 
	 * @param filter
	 *            a filter within this selection, may be <code>null</code> in
	 *            which case the selection is not modified.
	 */
	public void emptyAfter(Filter filter) {
		if (filter == null)
			return;
		final List<Filter> disposeList = new ArrayList<Filter>();
		boolean found = false;
		synchronized (this) {
			for (Iterator<Filter> iterator = f_filters.iterator(); iterator
					.hasNext();) {
				final Filter next = iterator.next();
				if (filter == next)
					found = true;
				if (found) {
					disposeList.add(filter);
					iterator.remove();
				}
			}
		}
		if (found) {
			for (Filter f : disposeList) {
				f.dispose();
			}
			notifySelectionChanged();
		}
	}

	/**
	 * Removes all existing filters from this selection with an index after the
	 * specified index.
	 * 
	 * @param filterIndex
	 *            the index of a filter used by this selection. A value of -1
	 *            will clear out all filters.
	 */
	public void emptyAfter(int filterIndex) {
		final List<Filter> disposeList = new ArrayList<Filter>();
		boolean changed = false;
		int index = 0;
		synchronized (this) {
			for (Iterator<Filter> iterator = f_filters.iterator(); iterator
					.hasNext();) {
				Filter filter = iterator.next();
				if (index > filterIndex) {
					disposeList.add(filter);
					iterator.remove();
					changed = true;
				}
				index++;
			}
		}
		if (changed) {
			for (Filter f : disposeList) {
				f.dispose();
			}
			notifySelectionChanged();
		}
	}

	/**
	 * Gets the number of filters used by this selection.
	 * 
	 * @return the number of filters used by this selection.
	 */
	public int getFilterCount() {
		synchronized (this) {
			return f_filters.size();
		}
	}

	/**
	 * Indicates if this selection should show the list of findings selected.
	 */
	private boolean f_showingFindings = false;

	/**
	 * Indicates if this selection shows the list of findings in the UI.
	 * 
	 * @return <code>true</code> if this selection should show the list of
	 *         findings, <code>false<code> if it should not.
	 */
	public boolean isShowingFindings() {
		synchronized (this) {
			return f_showingFindings;
		}
	}

	/**
	 * Sets the status of this selection with regard to showing the list of
	 * findings.
	 * 
	 * @param value
	 *            <code>true</code> if this selection should show the list of
	 *            findings, <code>false<code> if it should not.
	 */
	public void setShowingFindings(boolean value) {
		synchronized (this) {
			f_showingFindings = value;
		}
	}

	/**
	 * Constructs a filter at the end of this selections chain of filters. Adds
	 * an optional observer to that filter. This method does <i>not</i>
	 * initiate the query to populate the filter.
	 * 
	 * @param factory
	 *            a filter factory used to select the filter to be constructed.
	 * @param observer
	 *            an observer for the new filter, may be <code>null</code> if
	 *            no observer is desired.
	 * @return the new filter.
	 */
	public Filter construct(ISelectionFilterFactory factory,
			IFilterObserver observer) {
		if (factory == null)
			throw new IllegalArgumentException("factory must be non-null");
		final Filter filter;
		synchronized (this) {
			if (!getAvailableFilters().contains(factory))
				throw new IllegalArgumentException(factory.getFilterLabel()
						+ " already used in selection");
			final Filter previous = f_filters.isEmpty() ? null : f_filters
					.getLast();
			filter = factory.construct(this, previous);
			f_filters.add(filter);
		}
		filter.addObserver(observer);
		return filter;
	}

	/**
	 * Gets the list of filters that are not yet being used as part of this
	 * selection. Any member of this result could be used to in a call to
	 * {@link #construct(ISelectionFilterFactory)}.
	 * 
	 * @return factories for unused filters.
	 */
	public List<ISelectionFilterFactory> getAvailableFilters() {
		List<ISelectionFilterFactory> result = new ArrayList<ISelectionFilterFactory>(
				f_allFilters);
		synchronized (this) {
			for (Filter filter : f_filters) {
				result.remove(filter.getFactory());
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * The count of findings that this selection, based upon what its filters
	 * have set to be porous, will allow through.
	 * 
	 * @return count of findings that this selection, based upon what its
	 *         filters have set to be porous, will allow through.
	 */
	public int getFindingCountPorous() {
		synchronized (this) {
			if (!f_filters.isEmpty()) {
				return f_filters.getLast().getFindingCountPorous();
			} else {
				return 0;
			}
		}
	}

	/**
	 * Adds the correct <code>from</code> and <code>where</code> clause to
	 * make a query get the set of findings defined by this selection from the
	 * <code>FINDINGS_OVERVIEW</code> table.
	 * 
	 * @param b
	 *            the string to mutate.
	 */
	public String getWhereClause() {
		final StringBuilder b = new StringBuilder();
		synchronized (this) {
			if (!f_filters.isEmpty()) {
				final Filter last = f_filters.getLast();
				synchronized (last) {
					b.append(last.getWhereClause(true));
				}
			}
		}
		return b.toString();
	}

	public boolean usesJoin() {
		synchronized (this) {
			if (!f_filters.isEmpty()) {
				final Filter last = f_filters.getLast();
				synchronized (last) {
					return last.usesJoin();
				}
			}
		}
		return false;
	}
	
	/**
	 * Indicates if this selection allows any possible findings through it.
	 * 
	 * @return <code>true</code> if the selection allows findings through it,
	 *         <code>false</code> otherwise.
	 */
	public boolean isPorous() {
		return getFindingCountPorous() > 0;
	}

	private final Set<ISelectionObserver> f_observers = new CopyOnWriteArraySet<ISelectionObserver>();

	public void addObserver(ISelectionObserver o) {
		if (o == null)
			return;
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.add(o);
	}

	public void removeObserver(ISelectionObserver o) {
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.remove(o);
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	private void notifySelectionChanged() {
		for (ISelectionObserver o : f_observers)
			o.selectionChanged(this);
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	private void notifyColumnsChanged(Column c) {
		for (ISelectionObserver o : f_observers)
			o.columnsChanged(this, c);
	}

	@Override
	public void changed() {
		/*
		 * The database has changed. Refresh this selection if it has any
		 * filters.
		 */
		final Job job = new DatabaseJob("Refresh selection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					refreshFiltersDatabaseJob();
					notifySelectionChanged();
				} catch (Exception e) {
					final int errNo = 53;
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * Refreshes the data within all the filters that comprise this selection.
	 * <p>
	 * Queries the database.
	 * <p>
	 * Blocks until all the queries are completed.
	 */
	public void refreshFilters() {
		final Job job = new DatabaseJob("Refresh selection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					refreshFiltersDatabaseJob();
				} catch (Exception e) {
					final int errNo = 53;
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		boolean joined = false;
		while (!joined) {
			try {
				job.join();
				joined = true;
			} catch (InterruptedException e) {
				// ignore, as we'll try again if necessary.
			}
		}
	}

	private void refreshFiltersDatabaseJob() {
		synchronized (this) {
			for (Filter filter : f_filters) {
				filter.refresh();
			}
		}
	}

	/**
	 * Invoked by a filter when the amount of findings allowed through the
	 * filter changed. This would be a change that occurred in the user
	 * interface.
	 * <p>
	 * This method must never be called during a refresh or an infinite loop of
	 * refreshes could occur.
	 * 
	 * @param changedFilter
	 *            a filter that is part of this selection.
	 */
	void filterChanged(final Filter changedFilter) {
		final Job job = new DatabaseJob("Refresh filter") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					refreshFiltersAfter(changedFilter);
					notifySelectionChanged();
				} catch (Exception e) {
					final int errNo = 53;
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * Refreshes the data within all the filters after the passed filter.
	 * <p>
	 * Queries the database.
	 * <p>
	 * Blocks until all the queries are completed.
	 */
	private void refreshFiltersAfter(Filter changedFilter) {
		/*
		 * Create a work list of all the filters in this selection after the one
		 * that just changed.
		 */
		LinkedList<Filter> workList = new LinkedList<Filter>();
		boolean add = false;
		synchronized (this) {
			for (Filter filter : f_filters) {
				if (add) {
					workList.addLast(filter);
				} else {
					if (filter == changedFilter)
						add = true;
				}
			}
			/*
			 * Do an update if the work list is not empty.
			 */
			for (Filter filter : workList) {
				filter.refresh();
			}
		}
	}

	private final Map<String, Column> f_columns = MListOfFindingsColumn
			.createColumns();

	public Column getColumn(String name) {
		return f_columns.get(name);
	}

	public Iterable<Column> getColumns() {
		return f_columns.values();
	}

	public int getNumColumns() {
		return f_columns.size();
	}

	public void setColumnVisible(String name, boolean newVisible) {
		Column c = getColumn(name);
		if (c.isVisible() == newVisible) {
			return; // Nothing changed
		}
		c.setVisible(newVisible);
		notifyColumnsChanged(c);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("[Selection filters={");
		boolean first = true;
		for (Filter f : f_filters) {
			if (first)
				first = false;
			else
				b.append(",");
			b.append(f.toString());
		}
		b.append("}]");
		return b.toString();
	}
}
