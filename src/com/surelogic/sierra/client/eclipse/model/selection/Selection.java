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

public final class Selection {

	protected final Executor f_executor;

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
		f_allFilters.add(FilterState.FACTORY);
		f_allFilters.add(FilterType.FACTORY);
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
		boolean changed = false;
		int index = 0;
		for (Iterator<Filter> iterator = f_filters.iterator(); iterator
				.hasNext();) {
			iterator.next();
			if (index > filterIndex) {
				iterator.remove();
				changed = true;
			}
			index++;
		}
		if (changed)
			notifyObservers();
	}

	public int getFilterCount() {
		return f_filters.size();
	}

	public Filter construct(ISelectionFilterFactory factory) {
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
		final Filter filter = factory.construct(this, previous, f_executor);
		f_filters.add(filter);
		notifyObservers();
		return filter;
	}

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
}
