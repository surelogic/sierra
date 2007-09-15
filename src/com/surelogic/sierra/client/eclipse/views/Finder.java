package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

public class Finder extends ScrolledComposite {

	private final Composite f_finderContents;

	public Finder(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL);
		f_finderContents = new Composite(this, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.fill = true;
		layout.wrap = false;
		f_finderContents.setLayout(layout);
		setContent(f_finderContents);

		setExpandVertical(true);
		setExpandHorizontal(true);
		setAlwaysShowScrollBars(true);

		Point sashSize = f_finderContents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		setMinSize(sashSize);

		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				fixupSizeOfColumnViewports();
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
		columnViewport.setMinSize(columnContents.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		columnContents.layout();

		Point sashSize = f_finderContents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		setMinSize(sashSize);
		Point from = getOrigin();
		Point to = new Point(sashSize.x - getSize().x, getOrigin().y);
		if (from.x < to.x) {
			Thread t = new Thread(new Animate(from, to, this));
			t.start();
		}
		f_finderContents.layout();
		fixupSizeOfColumnViewports();
		return index;
	}

	public void emptyAfter(int columnIndex) {
		int index = 0;
		for (Iterator<ScrolledComposite> iterator = f_columns.iterator(); iterator
				.hasNext();) {
			ScrolledComposite columnViewport = iterator.next();
			if (index > columnIndex) {
				iterator.remove();
				columnViewport.dispose();
			}
			index++;
		}
		f_finderContents.layout();
		fixupSizeOfColumnViewports();
	}

	private void fixupSizeOfColumnViewports() {
		Rectangle finderViewportSize = getClientArea();
		for (ScrolledComposite columnViewport : f_columns) {
			Point columnViewportSize = columnViewport.getSize();
			columnViewport.setSize(columnViewportSize.x,
					finderViewportSize.height - 3);
		}
	}

	static class Animate implements Runnable {
		public static final int SLEEP_DURATION = 10;
		public static final int PIXELS_PER_FRAME = 6;

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
			while (animating) {
				try {
					Thread.sleep(SLEEP_DURATION);
				} catch (InterruptedException e) {
					// ignore
				}
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
