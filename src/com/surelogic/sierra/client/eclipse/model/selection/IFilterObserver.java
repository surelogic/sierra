package com.surelogic.sierra.client.eclipse.model.selection;

/**
 * An interface for objects that want to observer a findings selection filter.
 */
public interface IFilterObserver {

	/**
	 * Indicates that something about the passed filter has changed.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void filterChanged(Filter filter);

	/**
	 * Indicates that the passed filter is in the process of being disposed by
	 * its owing selection.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void filterDisposed(Filter filter);

	/**
	 * Indicates that the query of findings that enter the passed filter failed
	 * due to a database problem.
	 * 
	 * @param filter
	 *            a filter.
	 * @param e
	 *            details about what went wrong.
	 */
	void filterQueryFailure(Filter filter, Exception e);
}
