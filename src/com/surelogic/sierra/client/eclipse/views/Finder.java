package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;

/**
 * A scrolled composite specialized to act like the Mac finder. This is
 * <i>mostly</i> a general purpose control.
 * <p>
 * When a column is added to this finder and the finder is not wide enough to
 * display it it is animated into view.
 */
public class Finder extends ScrolledComposite {

	private final Composite f_finderContents;

	public Finder(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL);
		f_finderContents = new Composite(this, SWT.NONE);
		setContent(f_finderContents);

		setExpandVertical(true);
		setExpandHorizontal(true);
		setAlwaysShowScrollBars(false);

		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				rememberColumnViewportOrigins();
				fixupSizeOfFinderContents();
			}
		});
		f_finderContents.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_LIST_SELECTION));
	}

	/**
	 * The ordered list of columns currently displayed by this finder.
	 */
	private final List<ScrolledComposite> f_columns = new ArrayList<ScrolledComposite>();

	/**
	 * Implemented by objects that contribute a column to this finder.
	 * 
	 * @see Finder#addColumn(IColumn);
	 * @see Finder#addColumnAfter(IColumn, int)
	 */
	static public interface IColumn {
		void createContents(Composite panel, int index);
	}

	/**
	 * Adds a column to this finder.
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @return the index of the new column within this finder.
	 */
	public int addColumn(IColumn column) {
		rememberColumnViewportOrigins();
		final ScrolledComposite columnViewport = new ScrolledComposite(
				f_finderContents, SWT.V_SCROLL);
		final Composite columnContents = new Composite(columnViewport, SWT.NONE);
		columnContents.setLayout(new FillLayout());
		f_columns.add(columnViewport);
		final int index = f_columns.size() - 1;
		column.createContents(columnContents, index);
		columnViewport.setContent(columnContents);
		columnViewport.setExpandVertical(true);
		columnViewport.setExpandHorizontal(true);
		columnViewport.setAlwaysShowScrollBars(false);
		columnViewport.setMinHeight(columnContents.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).y);
		columnContents.layout();

		fixupSizeOfFinderContents();

		final Point finderContentsSize = f_finderContents.getSize();
		final Point from = getOrigin();
		final Point to = new Point(
				finderContentsSize.x - getClientArea().width, from.y);
		if (this.isVisible() && from.x < to.x) {
			final Thread animateTillColumnIsVisible = new Thread(new Animate(
					from, to, this));
			animateTillColumnIsVisible.start();
		}
		return index;
	}

	/**
	 * 
	 * Adds a column to this finder after the specified column index. Any and
	 * all existing columns with an index after the specified column index are
	 * removed from this finder before the new column is added.
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @param columnIndex
	 *            the index after which the new column should be placed.
	 * @return the index of the new column within this finder.
	 */
	public int addColumnAfter(IColumn column, int columnIndex) {
		emptyAfterHelper(columnIndex);
		return addColumn(column);
	}

	/**
	 * Removes all existing columns from this finder with an index after the
	 * specified column index.
	 * <p>
	 * Note: Clients should not invoke this method followed by
	 * {@link #addColumn(Finder.IColumn)} as this could cause a strange
	 * animation in several cases. Instead invoke
	 * {@link #addColumnAfter(Finder.IColumn, int)} which combines the two
	 * operations and produces the correct animation.
	 * 
	 * @param columnIndex
	 *            a column index that is a column of this finder.
	 */
	public void emptyAfter(int columnIndex) {
		rememberColumnViewportOrigins();
		emptyAfterHelper(columnIndex);
		fixupSizeOfFinderContents();
	}

	/**
	 * A helper method to dump columns in the finder after the passed column
	 * index.
	 * 
	 * @param columnIndex
	 *            a column index that is a column of this finder.
	 * 
	 * @see #addColumnAfter(Finder.IColumn, int)
	 * @see #emptyAfter(int)
	 */
	private void emptyAfterHelper(int columnIndex) {
		int index = 0;
		for (Iterator<ScrolledComposite> iterator = f_columns.iterator(); iterator
				.hasNext();) {
			ScrolledComposite columnViewport = iterator.next();
			if (index > columnIndex) {
				iterator.remove();
				forgetColumnViewportOriginFor(columnViewport);
				columnViewport.dispose();
			}
			index++;
		}
	}

	/**
	 * Gets returns the column index that the passed control exists within.
	 * 
	 * @param c
	 *            a control.
	 * @return the index of the column that <code>c</code> exists within, or
	 *         -1 if <code>c</code> is not within a column of this finder.
	 */
	public int getColumnIndex(Control c) {
		if (c instanceof ScrolledComposite) {
			ScrolledComposite sc = (ScrolledComposite) c;
			final int index = f_columns.indexOf(sc);
			/*
			 * Is this a scrolled composite that this finder created?
			 */
			if (index != -1)
				return index;
		} else {
			/*
			 * Have we gone all the way up and failed to find a column?
			 */
			if (c == null)
				return -1;
		}
		return getColumnIndex(c.getParent());
	}

	/**
	 * This map remembers the vertical scroll bar positions for all of the
	 * columns in this viewer. It ensures that all the vertical scrollbars don't
	 * jump to the top each time the finer is manipulated.
	 */
	private final Map<ScrolledComposite, Point> f_columnViewportToOrigin = new HashMap<ScrolledComposite, Point>();

	private void rememberColumnViewportOrigins() {
		f_columnViewportToOrigin.clear();
		for (ScrolledComposite columnViewport : f_columns) {
			final Point origin = columnViewport.getOrigin();
			f_columnViewportToOrigin.put(columnViewport, origin);
		}
	}

	private void forgetColumnViewportOriginFor(ScrolledComposite columnViewport) {
		f_columnViewportToOrigin.remove(columnViewport);
	}

	/**
	 * The width in pixels around the finder contents.
	 */
	private static final int BORDER = 3;

	/**
	 * The vertical space between columns.
	 */
	private static final int PADDING = 3;

	/**
	 * We directly manage the layout of the contents of the finder. This method
	 * does the bulk of the work.
	 */
	private void fixupSizeOfFinderContents() {
		Rectangle finderViewportSize = getClientArea();
		int xPos = BORDER;
		for (ScrolledComposite columnViewport : f_columns) {
			final Control columnContents = columnViewport.getContent();
			final Point pColumnContentsSize = columnContents.computeSize(
					SWT.DEFAULT, SWT.DEFAULT);
			final int columnViewportHeight = finderViewportSize.height
					- (2 * BORDER);
			int scrollBarWidth = 0;
			if (pColumnContentsSize.y > columnViewportHeight) {
				/*
				 * The scroll bar will be showing to the right of this column,
				 * so we need to make room for it.
				 */

				ScrollBar bar = columnViewport.getVerticalBar();
				if (bar != null) {
					scrollBarWidth = bar.getSize().x;
				} else {
					SLLogger
							.getLogger()
							.log(Level.WARNING,
									"null vertical scroll bar for a column in the finder.");
					/*
					 * Just guess the scroll bar width.
					 */
					scrollBarWidth = 15;
				}
			}
			final int columnViewportWidth = pColumnContentsSize.x
					+ scrollBarWidth;
			columnViewport.setBounds(xPos, BORDER, columnViewportWidth,
					columnViewportHeight);
			xPos += PADDING + columnViewportWidth;
			final Point origin = f_columnViewportToOrigin.get(columnViewport);
			if (origin != null) {
				columnViewport.setOrigin(origin);
			}
		}
		final int finderContentsWidth = xPos - PADDING + BORDER;
		f_finderContents
				.setSize(finderContentsWidth, finderViewportSize.height);
		setMinWidth(finderContentsWidth);
	}

	/**
	 * An animation to make a new column visible in this finder.
	 */
	static private class Animate implements Runnable {
		public static final int FRAME_RATE_NS = 40000000;
		public static final int PIXELS_PER_FRAME = 15;

		/**
		 * Causes the pixels per frame to double after the specified frame.
		 */
		public static final int SPEED_UP_AFTER_FRAME = 4;
		public static final int SPEED_UP_PIXELS_PER_FRAME = 40;

		final Point f_from;
		final Point f_to;
		final ScrolledComposite f_control;

		Animate(Point from, Point to, ScrolledComposite control) {
			f_from = from;
			f_to = to;
			f_control = control;
		}

		public void run() {
			Thread.yield();
			boolean animating = true;
			int frames = 0;
			long lastFrameTimeNS = System.nanoTime();
			while (animating) {
				boolean waitingNextFrame = true;
				while (waitingNextFrame) {
					final long now = System.nanoTime();
					if (now < lastFrameTimeNS + FRAME_RATE_NS) {
						Thread.yield();
					} else {
						waitingNextFrame = false;
						lastFrameTimeNS = now;
					}
				}
				if (frames++ > SPEED_UP_AFTER_FRAME) {
					// speed up rate of movement
					f_from.x += SPEED_UP_PIXELS_PER_FRAME;
				} else {
					// normal rate of movement
					f_from.x += PIXELS_PER_FRAME;
				}
				if (f_from.x >= f_to.x) {
					f_from.x = f_to.x;
					animating = false;
				}
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						f_control.setOrigin(f_from);
					}
				});
			}
		}
	}
}
