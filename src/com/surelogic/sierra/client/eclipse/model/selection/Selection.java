package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public final class Selection {

	protected final Executor f_executor;

	private final Set<ISelectionFilterFactory> f_availableFilters;

	Selection(SelectionManager manager, final Executor executor) {
		assert manager != null;
		f_manager = manager;
		assert executor != null;
		f_executor = executor;
		f_availableFilters = new HashSet<ISelectionFilterFactory>();
		/*
		 * Add in all the filter factories.
		 */
		f_availableFilters.add(FilterImportance.FACTORY);
		f_availableFilters.add(FilterProject.FACTORY);
	}

	private final SelectionManager f_manager;

	public SelectionManager getManager() {
		return f_manager;
	}

	private final LinkedList<Filter> f_filters = new LinkedList<Filter>();

	public void emptyAfter(int filterIndex) {
		int index = 0;
		for (Iterator<Filter> iterator = f_filters.iterator(); iterator
				.hasNext();) {
			if (index > filterIndex) {
				iterator.remove();
			}
			index++;
		}
	}

	public int getFilterCount() {
		return f_filters.size();
	}

	public Filter construct(ISelectionFilterFactory factory) {
		if (factory == null)
			throw new IllegalArgumentException("factory must be non-null");
		if (!f_availableFilters.contains(factory))
			throw new IllegalArgumentException(factory.getFilterLabel()
					+ " already used in selection");
		final Filter previous = f_filters.isEmpty() ? null : f_filters
				.getLast();
		final Filter filter = factory.construct(this, previous, f_executor);
		f_filters.add(filter);
		return filter;
	}

	public List<ISelectionFilterFactory> getAvailableFilters() {
		List<ISelectionFilterFactory> result = new ArrayList<ISelectionFilterFactory>(
				f_availableFilters);
		Collections.sort(result);
		return result;
	}
}
