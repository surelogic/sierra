package com.surelogic.sierra.client.eclipse.model.selection;

/**
 * An interface for objects that want to observer a findings selection filter.
 */
public interface IFilterObserver {

	/**
	 * Indicates a change to the number of findings that a filter is allowing
	 * through itself.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void porous(Filter filter);

	/**
	 * Indicates that the set of findings that enters this filter changed. In
	 * particular the database was required due to some change in the database
	 * or a prior filter.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void contentsChanged(Filter filter);

	/**
	 * Indicates that the set of findings that enters this filter is the empty
	 * set. This could occur if the database is empty or a prior filter is
	 * non-porous.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void contentsEmpty(Filter filter);

	/**
	 * Indicates that the filter is in the process of being disposed by its
	 * owing selection.
	 * 
	 * @param filter
	 *            a filter.
	 */
	void dispose(Filter filter);

	/**
	 * Indicates that the query of findings that enter this filter failed due to
	 * a database problem.
	 * 
	 * @param filter
	 *            a filter.
	 * @param e
	 *            details about what went wrong.
	 */
	void queryFailure(Filter filter, Exception e);
}
