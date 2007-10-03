package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public final class Selection extends AbstractDatabaseObserver {

	private final Executor f_executor;

	/**
	 * Setup in the constructor and immutable after that point.
	 */
	private final Set<ISelectionFilterFactory> f_allFilters;

	Selection(SelectionManager manager, final Executor executor) {
		assert manager != null;
		f_manager = manager;
		assert executor != null;
		f_executor = executor;
		f_allFilters = new HashSet<ISelectionFilterFactory>();
		/*
		 * Add in all the filter factories.
		 */
		f_allFilters.add(FilterArtifactCount.FACTORY);
		f_allFilters.add(FilterCommentCount.FACTORY);
		f_allFilters.add(FilterExamined.FACTORY);
		f_allFilters.add(FilterFindingType.FACTORY);
		f_allFilters.add(FilterImportance.FACTORY);
		f_allFilters.add(FilterPackage.FACTORY);
		f_allFilters.add(FilterProject.FACTORY);
		f_allFilters.add(FilterSelection.FACTORY);
		f_allFilters.add(FilterType.FACTORY);
	}

	void init() {
		DatabaseHub.getInstance().addObserver(this);
	}

	void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
		for (Filter f : f_filters)
			f.dispose();
		f_observers.clear();
	}

	private final SelectionManager f_manager;

	public SelectionManager getManager() {
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
		return new LinkedList<Filter>(f_filters);
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
		waitUntilRefreshIsCompleted();
		boolean changed = false;
		int index = 0;
		for (Iterator<Filter> iterator = f_filters.iterator(); iterator
				.hasNext();) {
			Filter filter = iterator.next();
			if (index > filterIndex) {
				filter.dispose();
				iterator.remove();
				changed = true;
			}
			index++;
		}
		if (changed)
			notifyObservers();
	}

	/**
	 * Gets the number of filters used by this selection.
	 * 
	 * @return the number of filters used by this selection.
	 */
	public int getFilterCount() {
		return f_filters.size();
	}

	/**
	 * Constructs a filter at the end of this selections chain of filters. Adds
	 * an optional observer to that filter. Finally, initiates the query to
	 * populate the filter.
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
		if (!getAvailableFilters().contains(factory))
			throw new IllegalArgumentException(factory.getFilterLabel()
					+ " already used in selection");
		final Filter previous = f_filters.isEmpty() ? null : f_filters
				.getLast();
		if (previous != null && !previous.isPorous()) {
			throw new IllegalStateException("unable to construct filter '"
					+ factory.getFilterLabel() + "' over non-porous filter '"
					+ previous.getFactory().getFilterLabel() + "' (bug)");
		}
		final Filter filter = factory.construct(this, previous);
		f_filters.add(filter);
		filter.addObserver(observer);
		// TODO filter.queryAsync();
		notifyObservers();
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
		for (Filter filter : f_filters) {
			result.remove(filter.getFactory());
		}
		Collections.sort(result);
		return result;
	}

	private final Set<ISelectionObserver> f_observers = new CopyOnWriteArraySet<ISelectionObserver>();

	public void addObserver(ISelectionObserver o) {
		f_observers.add(o);
	}

	public void removeObserver(ISelectionObserver o) {
		f_observers.remove(o);
	}

	private void notifyObservers() {
		for (ISelectionObserver o : f_observers)
			o.selectionStructureChanged(this);
	}

	@Override
	public void changed() {
		/*
		 * The database has changed. Refresh this selection if it has any
		 * filters.
		 */
		refresh();
	}

	private final AtomicBoolean f_refreshing = new AtomicBoolean(false);

	private void waitUntilRefreshIsCompleted() {
		while (f_refreshing.get()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public void refresh() {
		if (f_refreshing.compareAndSet(false, true)) {
			if (!f_filters.isEmpty()) {
				f_executor.execute(new RefreshFilter(f_filters));
			}
		}
	}

	private class RefreshFilter implements Runnable, IFilterObserver {

		private final Filter f_filter;
		private final LinkedList<Filter> f_workList;

		public RefreshFilter(List<Filter> workList) {
			if (workList == null || workList.isEmpty())
				throw new IllegalArgumentException(
						"refresh work list must be non-null and not empty");
			f_workList = new LinkedList<Filter>(workList);
			f_filter = f_workList.removeFirst();
		}

		private void doNext() {
			if (!f_workList.isEmpty())
				f_executor.execute(new RefreshFilter(f_workList));
			else
				f_refreshing.set(false);
		}

		public void run() {
			f_filter.addObserver(this);
			// TODO f_filter.queryAsync();
		}

		public void contentsChanged(Filter filter) {
			f_filter.removeObserver(this);
			doNext();
		}

		public void contentsEmpty(Filter filter) {
			f_filter.removeObserver(this);
			doNext();
		}

		public void dispose(Filter filter) {
			/*
			 * This is a bug; it should never happen.
			 */
			SLLogger.getLogger().log(Level.SEVERE,
					filter + " disposed during a selection refresh (bug)",
					new IllegalStateException());
		}

		public void porous(Filter filter) {
			f_filter.removeObserver(this);
		}

		public void queryFailure(Filter filter, Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Query failure during selection refresh " + filter, e);
			f_filter.removeObserver(this);
			f_refreshing.set(false);
		}
	}

	private class FilterPorousChange extends AbstractFilterObserver {

		@Override
		public void porous(Filter filter) {
			if (f_refreshing.compareAndSet(false, true)) {
				/*
				 * Create a work list of all the filters in this selection after
				 * the one that just changed.
				 */
				LinkedList<Filter> workList = new LinkedList<Filter>();
				boolean add = false;
				for (Filter f : f_filters) {
					if (add) {
						workList.addLast(f);
					} else {
						if (f == filter)
							add = true;
					}
				}
				/*
				 * Do an update if the work list is not empty.
				 */
				if (!workList.isEmpty()) {
					f_executor.execute(new RefreshFilter(workList));
				}
			}
		}
	}
}
