package com.surelogic.sierra.client.eclipse.views;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.StringUtility;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IPorousObserver;

public final class FilterSelectionReport implements IPorousObserver {

	private final CascadingList f_finder;
	private final int f_column;
	private final Filter f_filter;

	private FilterSelectionMenu f_menu = null;

	private Label f_porousCount = null;

	public boolean hasAMenu() {
		return f_menu != null;
	}

	public FilterSelectionMenu getMenu() {
		return f_menu;
	}

	private final FindingsFinderMediator f_mediator;

	FilterSelectionReport(CascadingList finder, int column, Filter filter,
			FindingsFinderMediator mediator) {
		assert finder != null;
		f_finder = finder;
		f_column = column;
		assert filter != null;
		f_filter = filter;
		assert mediator != null;
		f_mediator = mediator;

		constructFilterReport();
	}

	private void constructFilterReport() {
		CascadingList.IColumn c = new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				boolean showAMenu = f_filter.getSelection()
						.getAvailableFilters().size() > 0;
				GridLayout gridLayout = new GridLayout();
				if (showAMenu)
					gridLayout.numColumns = 2;
				panel.setLayout(gridLayout);
				Group group = new Group(panel, SWT.NONE);
				group.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false,
						false));
				group.setText(f_filter.getFactory().getFilterLabel());
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.fill = true;
				rowLayout.wrap = false;
				group.setLayout(rowLayout);
				final int total = f_filter.getFindingCountTotal();
				Label lt = new Label(group, SWT.RIGHT);
				lt.setText(StringUtility.toCommaSepString(total) + " Findings");
				for (Map.Entry<String, Integer> count : f_filter
						.getSummaryCounts().entrySet()) {
					newReport(group, count.getKey(), null, count.getValue(),
							total);
				}
				f_porousCount = new Label(group, SWT.RIGHT);

				final Link li = new Link(group, SWT.WRAP);
				li
						.setText("<A HREF=\"select\">Graph</A> <A HREF=\"deselect\">Show</A>");
				group.pack();

				if (showAMenu) {
					Composite rhs = new Composite(panel, SWT.NONE);
					rhs.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false,
							false));
					f_menu = new FilterSelectionMenu(f_filter.getSelection()
							.getAvailableFilters(), rhs);
					f_menu.addObserver(f_mediator);
				}
			}

		};
		f_finder.addColumnAfter(c, f_column);
		setFindingCountPorous();
		f_filter.addObserver(this);
	}

	private void newReport(Composite parent, String text, Image image,
			final int value, final int total) {
		final Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		result.setLayout(layout);

		final Button b = new Button(result, SWT.CHECK);
		b.setText(text);
		b.setImage(image);
		final Point bSize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		b.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final String value = b.getText();
				final boolean isChecked = b.getSelection();
				final boolean isPorous = f_filter.isPorous(value);
				final boolean changed = isChecked != isPorous;
				if (changed) {
					final int column = f_finder.getColumnIndexOf(b);
					f_mediator.emptyAfter(column);
					f_filter.setPorous(b.getText(), isChecked);
				}
			}
		});

		final Canvas c = new Canvas(result, SWT.NONE) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(75, bSize.y);
			}
		};
		c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		c.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				final Display display = result.getDisplay();
				Point cSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				GC gc = e.gc;
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
				gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
				int percent = (int) (((double) value / (double) total) * 100);
				int width = (cSize.x - 1) * percent / 100;
				if (width < 2 && value > 0)
					width = 2;
				gc.fillGradientRectangle(0, 0, width, cSize.y, true);
				Rectangle rect2 = new Rectangle(0, 0, cSize.x - 1, cSize.y - 1);
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.drawRectangle(rect2);
				if (percent > 25) {
					int p = (cSize.x - 1) * 25 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 50) {
					int p = (cSize.x - 1) * 50 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 75) {
					int p = (cSize.x - 1) * 75 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				String text = StringUtility.toCommaSepString(value);
				Point size = e.gc.textExtent(text);
				int offset = Math.max(0, (cSize.y - size.y) / 2);
				int rightJ = cSize.x - 2 - size.x;
				gc.drawText(text, rightJ, 0 + offset, true);
				gc.setForeground(background);
				gc.setBackground(foreground);
			}
		});
	}

	public void porous(Filter filter) {
		setFindingCountPorous();
	}

	private void setFindingCountPorous() {
		final int porousCount = f_filter.getFindingCountPorous();
		f_porousCount.setText(StringUtility.toCommaSepString(porousCount));
		if (hasAMenu())
			getMenu().setEnabled(porousCount > 0);
	}
}
