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
import java.util.concurrent.Executor;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.Data;

/**
 * Abstract base class for all findings filters. Intended to be subclassed.
 * <p>
 * This class depends upon the form of the <code>FINDINGS_OVERVIEW</code>
 * table in the database.
 * <p>
 * This may not be thread-safe depending upon what the {@link Executor} does
 * w.r.t. thread confinement. So races may lurk here.
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
		return f_selection;
	}

	/**
	 * Link to the previous filter, may be <code>null</code>.
	 */
	protected final Filter f_previous;

	protected final Executor f_exector;

	Filter(final Selection selection, final Filter previous,
			final Executor executor) {
		assert selection != null;
		f_selection = selection;
		f_previous = previous;
		assert executor != null;
		f_exector = executor;
	}

	void dispose() {
		notifyDispose();
	}

	/**
	 * Counts broken down by all previous filter values. Only mutated by
	 * {@link #queryAsync()}.
	 */
	protected final Map<LinkedList<String>, Integer> f_counts = new HashMap<LinkedList<String>, Integer>();

	/**
	 * Summary counts for just this filter. Only mutated by
	 * {@link #queryAsync()}.
	 */
	protected final Map<String, Integer> f_summaryCounts = new HashMap<String, Integer>();

	public Map<String, Integer> getSummaryCounts() {
		return new HashMap<String, Integer>(f_summaryCounts);
	}

	/**
	 * Set by {@link #deriveSummaryCounts()}. Only mutated by
	 * {@link #queryAsync()}.
	 */
	protected int f_countTotal = 0;

	/**
	 * The set of values in alphabetical order. Only mutated by
	 * {@link #queryAsync()}.
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
		return new LinkedList<String>(f_allValues);
	}

	/**
	 * Starts a task in the background to query the necessary information about
	 * this filter. This method doesn't wait for the task to complete, it
	 * returns immediately.
	 * <p>
	 * This method is intended to be called upon an update to the database to
	 * refresh the model within this class. It could also be called if a prior
	 * filter changed the set of findings it allows to enter this filter.
	 * <p>
	 * Observers are notified (later on) via a call to
	 * {@link IFilterObserver#contentsChanged(Filter)} followed by a call to
	 * {@link IFilterObserver#porous(Filter)} if the query was successful and at
	 * least one finding enters this filter. If no findings enter this filter
	 * then a call to {@link IFilterObserver#contentsEmpty(Filter)} is made. In
	 * the worst case, {@link IFilterObserver#queryFailure(Exception)} is called
	 * if the query failed (a bug).
	 */
	public void queryAsync() {
		final Runnable task = new Runnable() {
			public void run() {
				try {
					queryCounts();
					deriveSummaryCounts();
					deriveAllValues();
					fixupPorousValues();
				} catch (Exception e) {
					notifyQueryFailure(e);
					return;
				}
				if (f_countTotal == 0) {
					notifyContentsEmpty();
					return;
				}
				notifyContentsChanged();
				notifyPorous();
			}
		};
		f_exector.execute(task);
	}

	private void queryCounts() throws SQLException {
		f_counts.clear();
		final Connection c = Data.getConnection();
		try {
			final Statement st = c.createStatement();
			try {
				System.out.println(getCountsQuery().toString());
				final ResultSet rs = st.executeQuery(getCountsQuery()
						.toString());
				final int columnCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					final LinkedList<String> valueList = new LinkedList<String>();
					for (int i = 1; i < columnCount; i++) {
						final String value = rs.getString(i);
						valueList.add(value);
					}
					int count = rs.getInt(columnCount);
					f_counts.put(valueList, count);
				}
			} finally {
				st.close();
			}
		} finally {
			c.close();
		}
	}

	private void deriveSummaryCounts() {
		f_summaryCounts.clear();
		int countTotal = 0;
		for (Iterator<Map.Entry<LinkedList<String>, Integer>> i = f_counts
				.entrySet().iterator(); i.hasNext();) {
			final Map.Entry<LinkedList<String>, Integer> entry = i.next();
			final String key = entry.getKey().getLast();
			int count = entry.getValue();
			countTotal += count;
			Integer summaryCount = f_summaryCounts.get(key);
			if (summaryCount != null) {
				count += summaryCount;
			}
			f_summaryCounts.put(key, count);
		}
		f_countTotal = countTotal;
	}

	/**
	 * May need to be overidden if the set of values includes values not able to
	 * be determined from the filter context.
	 */
	protected void deriveAllValues() {
		f_allValues.clear();
		f_allValues.addAll(f_summaryCounts.keySet());
		Collections.sort(f_allValues);
	}

	private void fixupPorousValues() {
		/*
		 * Keep only those "checked" values that still exist in this filter.
		 */
		f_porousValues.retainAll(f_allValues);
		/*
		 * If only one choice exists, go ahead and select it. Bill Scherlis had
		 * this idea for making the filter easier to use.
		 */
		if (f_allValues.size() == 1)
			f_porousValues.addAll(f_allValues);
		/*
		 * Don't call notifyPorous() here, the caller of this method will do it.
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
	 * @return
	 */
	public List<String> getValues() {
		return new LinkedList<String>(f_allValues);
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
		f_observers.add(o);
	}

	public final void removeObserver(IFilterObserver o) {
		f_observers.remove(o);
	}

	protected void notifyPorous() {
		for (IFilterObserver o : f_observers) {
			o.porous(this);
		}
	}

	protected void notifyContentsChanged() {
		for (IFilterObserver o : f_observers) {
			o.contentsChanged(this);
		}
	}

	protected void notifyContentsEmpty() {
		for (IFilterObserver o : f_observers) {
			o.contentsEmpty(this);
		}
	}

	protected void notifyDispose() {
		for (IFilterObserver o : f_observers) {
			o.dispose(this);
		}
	}

	protected void notifyQueryFailure(final Exception e) {
		for (IFilterObserver o : f_observers) {
			o.queryFailure(this, e);
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
		if (!f_allValues.contains(value))
			throw new IllegalArgumentException("value not filtered by " + this);
		return f_porousValues.contains(value);
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
		if (!f_allValues.contains(value))
			throw new IllegalArgumentException("value not filtered by " + this);
		if (porous == isPorous(value))
			return;
		if (porous)
			f_porousValues.add(value);
		else
			f_porousValues.remove(value);
		notifyPorous();
	}

	/**
	 * Subclasses may need to set this to <code>false</code> if they don't
	 * want values quoted in the SQL query. For example, if the values are
	 * integers.
	 */
	protected boolean f_quote = true;

	/**
	 * The total count of findings that this filter may filter. This is the
	 * count of findings that the previous filter let through.
	 * 
	 * @return a count of findings.
	 */
	public int getFindingCountTotal() {
		return f_countTotal;
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
		for (String value : f_porousValues) {
			Integer count = f_summaryCounts.get(value);
			if (count != null)
				result += count;
		}
		return result;
	}

	/**
	 * Indicates if this filter allows any possible findings through it.
	 * 
	 * @return <code>true</code> if the filter allows rows through it,
	 *         <code>false</code> otherwise.
	 */
	public boolean isPorous() {
		return getFindingCountPorous() > 0;
	}

	protected StringBuilder getCountsQuery() {
		final StringBuilder b = new StringBuilder();
		b.append("select ");
		addColumnsTo(b);
		b.append(",count(*) from FINDINGS_OVERVIEW ");
		addCountsWhereClauseTo(b);
		b.append("group by ");
		addColumnsTo(b);
		return b;
	}

	protected void addColumnsTo(StringBuilder b) {
		Filter filter = this;
		final LinkedList<String> columnNames = new LinkedList<String>();
		do {
			columnNames.addFirst(filter.getColumnName());
			filter = filter.f_previous;
		} while (filter != null);

		boolean first = true;
		for (String columnName : columnNames) {
			if (first) {
				first = false;
			} else {
				b.append(",");
			}
			b.append(columnName);
		}
	}

	protected boolean hasWhereClausePart() {
		/*
		 * We don't need a where clause if everything is checked as being
		 * porous. This should make the query faster than listing everything
		 * explicitly.
		 */
		return !f_porousValues.containsAll(f_allValues);
	}

	protected void addCountsWhereClauseTo(StringBuilder b) {
		boolean stateFilterNotUsed = true;
		boolean first = true;
		Filter filter = this.f_previous;
		while (filter != null) {
			// TODO: fragile base class :-)
			if (filter instanceof FilterSelection)
				stateFilterNotUsed = false;
			if (filter.hasWhereClausePart()) {
				if (first) {
					b.append("where ");
					first = false;
				} else {
					b.append("and ");
				}
				filter.addWhereClausePartTo(b);
			}
			filter = filter.f_previous;
		}
		if (stateFilterNotUsed && !(this instanceof FilterSelection)) {
			/*
			 * In this case we need to add to the where clause to filter out all
			 * the findings that have been fixed.
			 */
			if (first) {
				b.append("where ");
				first = false;
			} else {
				b.append("and ");
			}
			FilterSelection.addWhereClauseToFilterOutFixed(b);
		}
	}

	protected void addWhereClausePartTo(StringBuilder b) {
		if (!hasWhereClausePart())
			throw new IllegalStateException(this + " has no where clause");
		b.append(getColumnName()).append(" in (");
		boolean first = true;
		for (String value : f_porousValues) {
			if (first) {
				first = false;
			} else {
				b.append(",");
			}
			addValueTo(b, value);
		}
		b.append(") ");
	}

	protected void addValueTo(StringBuilder b, String dbValue) {
		if (f_quote)
			b.append("'");
		/*
		 * TODO: We should turn all ' in this string to ''
		 */
		b.append(dbValue);
		if (f_quote)
			b.append("'");
	}

	public Image getImageFor(String value) {
		return null;
	}

	@Override
	public String toString() {
		return "[Filter on " + getColumnName() + "]";
	}
}
