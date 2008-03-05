package com.surelogic.sierra.chart;

/**
 * An abstract base class for all database plots. This class provides
 * implementations of the {@link #getWidth(int)} and {@link #getHeight(int)}
 * methods that simply return the hint.
 */
public abstract class AbstractDatabasePlot implements IDatabasePlot {

	public int getHeight(final int hint) {
		return hint;
	}

	public int getWidth(final int hint) {
		return hint;
	}
}
