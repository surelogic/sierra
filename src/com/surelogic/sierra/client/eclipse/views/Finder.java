package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

/**
 * A scrolled composite specialized to act like the Mac finder. This is
 * <i>mostly</i> a general purpose control. The
 * {@link #addColumn(com.surelogic.sierra.client.eclipse.views.Finder.IColumn)}
 * method allows addition of a new column into the finder, and
 * {@link #emptyAfter(int)} removes columns.
 * <p>
 * When a column is added to this finder and the finder is not wide enough to
 * display it it is animated into view.
 */
public class Finder extends ScrolledComposite {

	private final Composite f_finderContents;

	public Finder(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL);
		f_finderContents = new Composite(this, SWT.NONE);
		// RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		// layout.fill = true;
		// layout.wrap = false;
		// f_finderContents.setLayout(layout);
		setContent(f_finderContents);

		setExpandVertical(true);
		setExpandHorizontal(true);
		setAlwaysShowScrollBars(true);

		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				// MessageBox dialog = new
				// MessageBox(f_finderContents.getShell(), SWT.DEFAULT);
				// dialog.setText("NOW");
				// dialog.open();
				Rectangle rect = getClientArea();
				// System.out.println("resize");
				Point sashSize = f_finderContents.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				// System.out.println("sashSize=" + sashSize);
				// System.out.println("sashSize=" + rect);
				sashSize.y = rect.height;
				setMinSize(sashSize);
				f_finderContents.setSize(sashSize);
				// PlatformUI.getWorkbench().getDisplay().asyncExec(new
				// Runnable() {
				// public void run() {
				rememberColumnViewportOrigins();
				fixupSizeOfColumnViewports();
				// }
				// });
			}
		});
		f_finderContents.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_LIST_SELECTION));
	}

	private final List<ScrolledComposite> f_columns = new ArrayList<ScrolledComposite>();

	static public interface IColumn {
		void createContents(Composite panel, int index);
	}

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
		columnViewport.setMinSize(columnContents.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		columnContents.layout();

		fixupSizeOfColumnViewports();
		final Point sashSize = f_finderContents.computeSize(SWT.DEFAULT,
				SWT.DEFAULT);
		setMinSize(sashSize);
		final Point from = getOrigin();
		final Point to = new Point(sashSize.x - getSize().x, getOrigin().y);
		// f_finderContents.layout();
		// fixupSizeOfColumnViewports();
		if (this.isVisible() && from.x < to.x) {
			final Thread animateTillColumnIsVisible = new Thread(new Animate(
					from, to, this));
			animateTillColumnIsVisible.start();
		}
		return index;
	}

	/**
	 * Empties all columns out of this finder after the passed column index.
	 * 
	 * @param columnIndex
	 *            a column index that is a column of this finder.
	 */
	public void emptyAfter(int columnIndex) {
		rememberColumnViewportOrigins();
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
		// f_finderContents.layout();
		fixupSizeOfColumnViewports();
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

	private static final int BORDER = 10;
	private static final int PADDING = 3;

	private void fixupSizeOfColumnViewports() {
		System.out.println();
		Rectangle finderViewportSize = getClientArea();
		System.out.println("finderViewportSize=" + finderViewportSize);
		int xPos = BORDER;
		for (ScrolledComposite columnViewport : f_columns) {
			final Control columnContents = columnViewport.getContent();
			final Point pColumnContentsSize = columnContents.computeSize(
					SWT.DEFAULT, SWT.DEFAULT);
			columnViewport.setBounds(xPos, BORDER, pColumnContentsSize.x,
					finderViewportSize.height - (2 * BORDER));
			xPos += PADDING + pColumnContentsSize.x;
			// final Point columnViewportSize = columnViewport.getSize();
			// System.out.println("columnViewportSize=" + columnViewportSize);
			// columnViewport.setSize(columnViewportSize.x,
			// finderViewportSize.height - 5);
			final Point origin = f_columnViewportToOrigin.get(columnViewport);
			if (origin != null) {
				columnViewport.setOrigin(origin);
			}
		}
		f_finderContents.setSize(xPos - PADDING + BORDER,
				finderViewportSize.height);
		//Rectangle finderCSize = f_finderContents.getBounds();
		//System.out.println("finderCSize=" + finderCSize);
	}

	/**
	 * An animation to make a new column visible in this finder.
	 */
	static private class Animate implements Runnable {
		public static final int FRAME_RATE_NS = 50000000;
		public static final int PIXELS_PER_FRAME = 20;
		/**
		 * Causes the pixels per frame to double after the specified frame.
		 */
		public static final int SPEED_UP_AFTER_FRAME = 4;

		final Point f_from;
		final Point f_to;
		final ScrolledComposite f_control;

		Animate(Point from, Point to, ScrolledComposite control) {
			f_from = from;
			f_to = to;
			f_control = control;
		}

		public void run() {
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
				f_from.x += PIXELS_PER_FRAME;
				if (frames++ > SPEED_UP_AFTER_FRAME)
					f_from.x += PIXELS_PER_FRAME;
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
