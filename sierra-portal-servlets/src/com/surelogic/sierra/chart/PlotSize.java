package com.surelogic.sierra.chart;

import com.surelogic.common.i18n.I18N;

/**
 * A mutable class for storing the size of a plot. The size is a width and
 * height in pixels.
 */
public final class PlotSize {

	/**
	 * Constructs a new plot size.
	 * 
	 * @param width
	 *            the width in pixels.
	 * @param height
	 *            the height in pixels.
	 */
	public PlotSize(int width, int height) {
		if (width <= 0)
			throw new IllegalArgumentException(I18N.err(80, "width"));
		f_width = width;
		if (height <= 0)
			throw new IllegalArgumentException(I18N.err(80, "height"));
		f_height = height;
	}

	private int f_width;

	/**
	 * Gets the width in pixels.
	 * 
	 * @return the width in pixels.
	 */
	public int getWidth() {
		return f_width;
	}

	/**
	 * Sets the width in pixels.
	 * 
	 * @param pixels
	 *            the new width in pixels.
	 */
	public void setWidth(int pixels) {
		if (pixels <= 0)
			throw new IllegalArgumentException(I18N.err(80, "width"));
		f_width = pixels;
	}

	private int f_height;

	/**
	 * Gets the height in pixels.
	 * 
	 * @return the height in pixels.
	 */
	public int getHeight() {
		return f_height;
	}

	/**
	 * Sets the height in pixels.
	 * 
	 * @param pixels
	 *            the new height in pixels.
	 */
	public void setHeight(int pixels) {
		if (pixels <= 0)
			throw new IllegalArgumentException(I18N.err(80, "height"));
		f_height = pixels;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + f_height;
		result = prime * result + f_width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlotSize other = (PlotSize) obj;
		if (f_height != other.f_height)
			return false;
		if (f_width != other.f_width)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(width=" + f_width + " height=" + f_height + ")";
	}
}
