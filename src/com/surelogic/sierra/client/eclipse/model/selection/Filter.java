package com.surelogic.sierra.client.eclipse.model.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;

/**
 * Abstract base class for all findings filters. Intended to be subclassed.
 * <p>
 * This class depends upon the form of the <code>FINDINGS_OVERVIEW</code>
 * table in the database.
 * <p>
 * This class is thread-safe.
 */
public abstract class Filter {

	/**
	 * Gets the factory for this filter.
	 * 
	 * @return a filter factory object.
	 */
	public abstract ISelectionFilterFactory getFactory();

	private final Selection f_selection;

	/**
	 * Gets the selection this filter exists within.
	 * 
	 * @return a selection.
	 */
	public Selection getSelection() {
		/*
		 * Not mutable so we don't need to hold a lock on this.
		 */
		return f_selection;
	}

	/**
	 * Indicates if this is the first filter of its selection.
	 * 
	 * @return <code>true</code> if this filter is the first filter of its
	 *         selection, <code>false</code> otherwise.
	 */
	public final boolean isFirstFilter() {
		return f_selection.isFirstFilter(this);
	}

	/**
	 * Indicates if this is the last filter of its selection.
	 * 
	 * @return <code>true</code> if this filter is the last filter of its
	 *         selection, <code>false</code> otherwise.
	 */
	public final boolean isLastFilter() {
		return f_selection.isLastFilter(this);
	}

	/**
	 * Link to the previous filter, may be <code>null</code>.
	 */
	protected final Filter f_previous;

	Filter(final Selection selection, final Filter previous) {
		assert selection != null;
		f_selection = selection;
		f_previous = previous;
	}

	void dispose() {
		notifyDispose();
		synchronized (this) {
			f_observers.clear();
		}
	}

	/**
	 * Clones this filter without its data. Only the set of porous values is
	 * remembered. {@link #refresh()} must be invoked on the clone before it can
	 * be used.
	 * 
	 * @param selection
	 *            the selection the new filter should be within.
	 * @param previous
	 *            the (new) filter before the new filter.
	 * @return a clone of this filter without its data.
	 */
	Filter copyNoQuery(Selection selection, Filter previous) {
		// construct a filter of the right type
		final Filter result = getFactory().construct(selection, previous);
		result.f_porousValues.addAll(f_porousValues);
		return result;
	}

	/**
	 * Counts for just this filter. Only mutated by {@link #refresh()}.
	 */
	protected final Map<String, Integer> f_counts = new HashMap<String, Integer>();

	/*
	private Map<String, Integer> getSummaryCounts() {
		synchronized (this) {
			return new HashMap<String, Integer>(f_counts);
		}
	}
    */
	
	public int getSummaryCountFor(String value) {
		Integer result = f_counts.get(value);
		return result == null ? 0 : result.intValue();
	}

	/**
	 * Set by {@link #queryCounts()}. Only mutated by
	 * {@link #refresh()}.
	 */
	protected int f_countTotal = 0;

	/**
	 * The set of values in alphabetical order. Only mutated by
	 * {@link #refresh()}.
	 */
	protected final LinkedList<String> f_allValues = new LinkedList<String>();

	/**
	 * Gets the current list of all possible values for this filter. This set of
	 * values defines a set of bins that partition the set of findings entering
	 * this filter.
	 * 
	 * @return all possible values for this filter.
	 */
	public List<String> getAllValues() {
		synchronized (this) {
			return new LinkedList<String>(f_allValues);
		}
	}

	/**
	 * This method is intended to be called to refresh the set of findings that
	 * enter this filter. It could also be called if a prior filter changed the
	 * set of findings it allows to enter this filter or if something changed in
	 * the database.
	 * <p>
	 * Observers are notified via a call to
	 * {@link IFilterObserver#filterChanged(Filter)} if the query was
	 * successful. In the worst case,
	 * {@link IFilterObserver#filterQueryFailure(Filter, Exception)} is called
	 * if the query failed (a bug).
	 */
	void refresh() {
		try {
			synchronized (this) {
				queryCounts();
				deriveAllValues();
				fixupPorousValues();
			}
		} catch (Exception e) {
			notifyFilterQueryFailure(e);
			return;
		}
		notifyFilterChanged();
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private void queryCounts() throws SQLException {
		f_counts.clear();
		int countTotal = 0;
		final Connection c = Data.readOnlyConnection();
		try {
			final Statement st = c.createStatement();
			try {
				final String query = getCountsQuery().toString();
				if (SLLogger.getLogger().isLoggable(Level.FINE)) {
					SLLogger.getLogger().fine(
							getFactory().getFilterLabel()
									+ " filter counts query: " + query);
				}
				final ResultSet rs = st.executeQuery(query);
				try {
					while (rs.next()) {
						final String value = rs.getString(1);
						int count = rs.getInt(2);
						f_counts.put(value, count);
						countTotal += count;
					}
				} finally {
					rs.close();
				}
			} finally {
				st.close();
			}
		} finally {
			c.close();
		}
		f_countTotal = countTotal;
	}

	/**
	 * May need to be overidden if the set of values includes values not able to
	 * be determined from the filter context.
	 * <p>
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	protected void deriveAllValues() throws Exception {
		f_allValues.clear();
		f_allValues.addAll(f_counts.keySet());
		Collections.sort(f_allValues);
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private void fixupPorousValues() {
		/*
		 * We don't want to delete values that are no longer in this filter
		 * because they may be in the future.
		 */
		/*
		 * If only one choice exists, go ahead and select it. Bill Scherlis had
		 * this idea for making the filter easier to use.
		 */
		if (f_allValues.size() == 1)
			f_porousValues.addAll(f_allValues);
		/*
		 * Don't call notifyPorous() here, the caller of this method will do it
		 * in a manner where we are not suspectable to deadlock.
		 */
	}

	/**
	 * Gets the column name from <code>FINDINGS_OVERVIEW</code> for this
	 * filter.
	 * 
	 * @return the column name from <code>FINDINGS_OVERVIEW</code> for this
	 *         filter.
	 */
	protected abstract String getColumnName();

	/**
	 * Gets the complete set of all values for this filter. This set is is
	 * filtered by previous filters.
	 * 
	 * @return a list of all values for this filter.
	 */
	public List<String> getValues() {
		synchronized (this) {
			return new LinkedList<String>(f_allValues);
		}
	}

	/**
	 * Indicates if this filter has any values. Its return value is equal to,
	 * but more efficient than, using the following expression:
	 * 
	 * <pre>
	 * !(getValues().isEmpty())
	 * </pre>
	 * 
	 * @return <code>true</code> if this filter has any values,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasValues() {
		synchronized (this) {
			return !f_allValues.isEmpty();
		}
	}

	/**
	 * Returns the list of all values for this filter ordered, from highest to
	 * lowest, by the summary count for that value.
	 * 
	 * @return a list of all values ordered by summary count.
	 */
	public List<String> getValuesOrderedBySummaryCount() {
		final List<String> values = getValues();
		final LinkedList<String> result = new LinkedList<String>();
		int count = 0;
		while (!values.isEmpty()) {
			for (Iterator<String> i = values.iterator(); i.hasNext();) {
				String value = i.next();
				if (getSummaryCountFor(value) < count) {
					result.add(value);
					i.remove();
				}
			}
			count++;
		}
		Collections.reverse(result);
		return result;
	}

	/**
	 * Records the set of values allowed through this filter. They would be
	 * "checked" in the user interface. It should be an invariant that for all
	 * elements <code>e</code> of this set
	 * <code>f_summaryCounts.containsKey(e)</code> is true.
	 * <p>
	 * If this set is mutated other than via a call to
	 * {@link #setPorous(String)} then it is important to remember to invoke
	 * {@link #notifyPorous()} to let observers know about this mutation.
	 */
	protected final Set<String> f_porousValues = new HashSet<String>();

	protected final Set<IFilterObserver> f_observers = new CopyOnWriteArraySet<IFilterObserver>();

	public final void addObserver(IFilterObserver o) {
		if (o == null)
			return;
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.add(o);
	}

	public final void removeObserver(IFilterObserver o) {
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.remove(o);
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	protected void notifyFilterChanged() {
		for (IFilterObserver o : f_observers) {
			o.filterChanged(this);
		}
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	protected void notifyDispose() {
		for (IFilterObserver o : f_observers) {
			o.filterDisposed(this);
		}
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	protected void notifyFilterQueryFailure(final Exception e) {
		for (IFilterObserver o : f_observers) {
			o.filterQueryFailure(this, e);
		}
	}

	/**
	 * Checks if the passed value is porous. It would be "checked" in the user
	 * interface.
	 * 
	 * @param value
	 *            a value managed by this filter.
	 * @return <code>true</code> if the value is porous, <code>false</code>
	 *         otherwise.
	 * @throws IllegalArgumentException
	 *             if <code>getValues().contains(value)</code> is not true.
	 */
	public boolean isPorous(String value) {
		synchronized (this) {
			if (!f_allValues.contains(value))
				throw new IllegalArgumentException("value not filtered by "
						+ this);
			return f_porousValues.contains(value);
		}
	}

	/**
	 * Sets the passed value to be porous within this filter.
	 * 
	 * @param value
	 *            a value managed by this filter.
	 * @param porous
	 *            <code>true</code> sets the value to be porous,
	 *            <code>false</code> makes it non-porous.
	 * @throws IllegalArgumentException
	 *             if <code>getValues().contains(value)</code> is not true.
	 */
	public void setPorous(String value, boolean porous) {
		synchronized (this) {
			if (!f_allValues.contains(value))
				throw new IllegalArgumentException("value not filtered by "
						+ this);
			if (porous == isPorous(value))
				return; // not a change
			if (porous)
				f_porousValues.add(value);
			else
				f_porousValues.remove(value);
		}
		notifyFilterChanged();
		/*
		 * Tell my enclosing selection to update filters after me because I
		 * changed the set of findings I let through.
		 */
		f_selection.filterChanged(this);
	}

	void setPorousOnLoad(String value, boolean porous) {
		synchronized (this) {
			if (porous)
				f_porousValues.add(value);
			else
				f_porousValues.remove(value);
		}
	}

	/**
	 * Makes all values in this filter porous.
	 * <p>
	 * Note that this method removes any values that might exist as porous but
	 * are not currently part of this filter. This situation can happen if a
	 * previous filter became less porous and a value selected as porous in this
	 * filter no longer exists.
	 */
	public void setPorousAll() {
		synchronized (this) {
			if (f_porousValues.containsAll(f_allValues)) {
				return;
			}

			f_porousValues.clear();
			f_porousValues.addAll(f_allValues);
		}
		notifyFilterChanged();
		/*
		 * Tell my enclosing selection to update filters after me because I
		 * changed the set of findings I let through.
		 */
		f_selection.filterChanged(this);
	}

	/**
	 * Makes no values in this filter porous.
	 * <p>
	 * Note that this method removes any values that might exist as porous but
	 * are not currently part of this filter. This situation can happen if a
	 * previous filter became less porous and a value selected as porous in this
	 * filter no longer exists.
	 */
	public void setPorousNone() {
		synchronized (this) {
			if (f_porousValues.isEmpty())
				return;
			f_porousValues.clear();
		}
		notifyFilterChanged();
		/*
		 * Tell my enclosing selection to update filters after me because I
		 * changed the set of findings I let through.
		 */
		f_selection.filterChanged(this);
	}

	/**
	 * Returns a copy of the set of porous values for this filter.
	 * 
	 * @return a copy of the set of porous values for this filter.
	 */
	public Set<String> getPorousValues() {
		return new HashSet<String>(f_porousValues);
	}

	/**
	 * Subclasses may need to set this to <code>false</code> if they don't
	 * want values quoted in the SQL query. For example, if the values are
	 * integers.
	 */
	protected volatile boolean f_quote = true;

	/**
	 * The total count of findings that this filter may filter. This is the
	 * count of findings that the previous filter let through.
	 * 
	 * @return a count of findings.
	 */
	public int getFindingCountTotal() {
		synchronized (this) {
			return f_countTotal;
		}
	}

	/**
	 * The count of findings that this filter, based upon what is set to be
	 * porous, will allow through.
	 * <p>
	 * it is an invariant that <code>getFindingCountPorous() <=
	 * getFindingCountTotal()</code>.
	 * 
	 * @return count of findings that this filter, based upon what is set to be
	 *         porous, will allow through.
	 */
	public int getFindingCountPorous() {
		int result = 0;
		synchronized (this) {
			for (String value : getMappedPorousValues()) {
				Integer count = f_counts.get(value);
				if (count != null)
					result += count;
			}
		}
		return result;
	}

	/**
	 * Indicates if this filter allows any possible findings through it.
	 * 
	 * @return <code>true</code> if the filter allows findings through it,
	 *         <code>false</code> otherwise.
	 */
	public boolean isPorous() {
		return getFindingCountPorous() > 0;
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private StringBuilder getCountsQuery() {
		final StringBuilder b = new StringBuilder();
		b.append(QB.get(6, getColumnName(), getWhereClause(false),
				getColumnName()));
		return b;
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private boolean hasWhereClausePart() {
		/*
		 * We don't need a where clause if everything is checked as being
		 * porous. This should make the query faster than listing everything
		 * explicitly.
		 */
		return !f_porousValues.containsAll(f_allValues);
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	String getWhereClause(boolean includeThis) {
		final StringBuilder b = new StringBuilder();
		final Set<ISelectionFilterFactory> unused = 
			new HashSet<ISelectionFilterFactory>(Selection.getAllFilters());
		boolean first = true;
		/*
		 * For counts we don't include this, for queries on the whole selection
		 * we do.
		 */
		Filter filter = includeThis ? this : this.f_previous;
		while (filter != null) {
			// TODO: fragile base class :-)
			unused.remove(filter.getFactory());
			
			if (filter.hasWhereClausePart()) {
				first = addClausePrefix(b, first);
				b.append(filter.getWhereClausePart());
			}
			filter = filter.f_previous;
		}
		unused.remove(this.getFactory());
		
		for (ISelectionFilterFactory unusedFilter : unused) {
			first = unusedFilter.addWhereClauseIfUnusedFilter(b, first);
		}
		return b.toString();
	}

  protected static boolean addClausePrefix(final StringBuilder b, boolean first) {
    if (first) {
    	b.append("where ");
    	first = false;
    } else {
    	b.append(" and ");
    }
    return first;
  }

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private String getWhereClausePart() {
		final StringBuilder b = new StringBuilder();
		if (!hasWhereClausePart())
			throw new IllegalStateException(this + " has no where clause");
		b.append(getColumnName()).append(" in (");
		boolean first = true;
		for (String value : getMappedPorousValues()) {
			if (first) {
				first = false;
			} else {
				b.append(",");
			}
			addValueTo(b, value);
		}
		if (first) {
			/*
			 * Hack to avoid problems with empty query
			 */
			if (f_quote)
				b.append("'xyzzy'");
			else
				b.append("-456");
		}
		b.append(")");
		return b.toString();
	}

	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	protected Iterable<String> getMappedPorousValues() {
		return f_porousValues;
	}
	
	/**
	 * Any caller must be holding a lock on <code>this</code>.
	 */
	private void addValueTo(StringBuilder b, String dbValue) {
		if (f_quote) {
			b.append("'").append(JDBCUtils.escapeString(dbValue)).append("'");
		} else {
			b.append(dbValue);
		}
	}

	public String getLabel(String value) {
		return value;
	}
	
	public Image getImageFor(String value) {
		return null;
	}

	@Override
	public String toString() {
		return "[Filter on " + getColumnName() + "]";
	}
}
