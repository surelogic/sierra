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

	/**
	 * Counts broken down by all previous filter values. Should be considered
	 * immutable after {@link #initAsync(Runnable)} runs.
	 */
	protected final Map<LinkedList<String>, Integer> f_counts = new HashMap<LinkedList<String>, Integer>();

	/**
	 * Summary counts for just this filter. Should be considered immutable after
	 * {@link #initAsync(Runnable)} runs.
	 */
	protected final Map<String, Integer> f_summaryCounts = new HashMap<String, Integer>();

	public Map<String, Integer> getSummaryCounts() {
		return new HashMap<String, Integer>(f_summaryCounts);
	}

	/**
	 * Set by {@link #deriveSummaryCounts()}. Should be considered immutable
	 * after that.
	 */
	protected int f_countTotal = 0;

	/**
	 * The set of values in alphabetical order. Should be considered immutable
	 * after {@link #initAsync(Runnable)} runs.
	 */
	protected final LinkedList<String> f_allValues = new LinkedList<String>();

	public List<String> getAllValues() {
		return new LinkedList<String>(f_allValues);
	}

	public interface CompletedAction {
		/**
		 * The initialization was successful.
		 */
		void success();

		/**
		 * The initialization failed.
		 * 
		 * @param e
		 *            the cause, may be <code>null</code>.
		 */
		void failure(Exception e);
	}

	/**
	 * Starts a task in the background to query the necessary information about
	 * this filter. This method doesn't wait for the task to complete, it
	 * returns immediately.
	 * <p>
	 * The passed object is invoked when the task completes.
	 * 
	 * @param completedAction
	 *            to be invoked when the initialization of this filter is
	 *            complete.
	 */
	public void initAsync(final CompletedAction completedAction) {
		final Runnable task = new Runnable() {
			public void run() {
				try {
					queryCounts();
					deriveSummaryCounts();
					deriveAllValues();
				} catch (Exception e) {
					completedAction.failure(e);
					return;
				}
				completedAction.success();
			}
		};
		f_exector.execute(task);
	}

	private void queryCounts() throws SQLException {
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
		for (Iterator<Map.Entry<LinkedList<String>, Integer>> i = f_counts
				.entrySet().iterator(); i.hasNext();) {
			final Map.Entry<LinkedList<String>, Integer> entry = i.next();
			final String key = entry.getKey().getLast();
			int count = entry.getValue();
			f_countTotal += count;
			Integer summaryCount = f_summaryCounts.get(key);
			if (summaryCount != null) {
				count += summaryCount;
			}
			f_summaryCounts.put(key, count);
		}
	}

	/**
	 * May need to be overidden if the set of values includes values not able to
	 * be determined from the filter context.
	 */
	protected void deriveAllValues() {
		f_allValues.addAll(f_summaryCounts.keySet());
		Collections.sort(f_allValues);
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
	 * {@link #setPorous(String)} then it is required to invoke
	 * {@link #notifyObservers()}.
	 */
	protected final Set<String> f_porousValues = new HashSet<String>();

	protected final Set<IPorousObserver> f_porousObservers = new CopyOnWriteArraySet<IPorousObserver>();

	public final void addObserver(IPorousObserver o) {
		f_porousObservers.add(o);
	}

	public final void removeObserver(IPorousObserver o) {
		f_porousObservers.remove(o);
	}

	protected void notifyObservers() {
		for (IPorousObserver o : f_porousObservers)
			o.porous(this);
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
		notifyObservers();
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
		 * porous.
		 */
		return !f_porousValues.containsAll(f_allValues);
	}

	protected void addCountsWhereClauseTo(StringBuilder b) {
		boolean stateFilterNotUsed = true;
		boolean first = true;
		Filter filter = this.f_previous;
		while (filter != null) {
			// TODO: fragile base class :-)
			if (filter instanceof FilterState)
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
		if (stateFilterNotUsed && !(this instanceof FilterState)) {
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
			FilterState.addWhereClauseToFilterOutFixed(b);
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
		return "[Filter: " + getColumnName() + "]";
	}
}
