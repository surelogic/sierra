package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class FindingsView2 extends ViewPart {

	private static final String NO_FINDINGS = "No findings ... please run Sierra analysis on a project to generate a set of findings";

	private FindingsMediator f_mediator = null;

	Finder f_finder;

	Composite getLabel(String text, Image image, Composite parent,
			final Listener onClick) {
		final Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		// layout.horizontalSpacing=0;
		// layout.verticalSpacing=0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		result.setLayout(layout);

		final Label iL = new Label(result, SWT.NONE);
		iL.setImage(image);
		iL.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
		final Label tL = new Label(result, SWT.LEFT);
		tL.setText(text);
		tL.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		final Label aL = new Label(result, SWT.NONE);
		aL.setImage(SLImages.getImage(SLImages.IMG_RIGHT_ARROW_SMALL));
		aL.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
		tL.addListener(SWT.MouseDown, new Listener() {
			boolean toggle = true;

			public void handleEvent(Event event) {
				if (toggle) {
					f_finder.emptyAfter(f_finder.getColumnIndex(iL));
					result.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					iL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					tL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					aL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					onClick.handleEvent(event);
				} else {
					result.setBackground(null);
					iL.setBackground(null);
					tL.setBackground(null);
					aL.setBackground(null);
				}
				toggle = !toggle;
			}
		});
		return result;
	}

	final Listener f_listener = new Listener() {
		public void handleEvent(final Event event) {
			f_finder.addColumn(new Finder.IColumn() {

				public void createContents(Composite panel, int index) {
					Composite rhs = panel;
					if (event != null) {
						GridLayout gl = new GridLayout();
						gl.numColumns = 2;
						rhs.setLayout(gl);

						Group g = new Group(panel, SWT.NONE);
						g.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP,
								false, false));
						g.setText("Projects");
						RowLayout layout = new RowLayout(SWT.VERTICAL);
						layout.fill = true;
						layout.wrap = false;
						g.setLayout(layout);
						Text t = new Text(g, SWT.SINGLE | SWT.SEARCH);
						newReport(g, "Common", null, 250, 1260);
						newReport(g, "Fluid", null, 1000, 1260);
						newReport(g, "JEdit", null, 10, 1260);
						g.pack();
						rhs = new Composite(panel, SWT.NONE);
						rhs.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP,
								false, false));
					}
					RowLayout l = new RowLayout(SWT.VERTICAL);
					l.fill = true;
					rhs.setLayout(l);
					// panel.setBackground(f_finder.getShell().getDisplay()
					// .getSystemColor(SWT.COLOR_BLUE));
					getLabel("Age", null, rhs, f_listener);
					getLabel("Audit Status", null, rhs, f_listener);
					getLabel("Importance", null, rhs, f_listener);
					getLabel("Finding Type", null, rhs, f_listener);
					getLabel("Package", null, rhs, f_listener);
					getLabel("Project", null, rhs, f_listener);
					getLabel("Recent Activity", null, rhs, f_listener);
					getLabel("Tool Provider", null, rhs, f_listener);
					rhs.setBackground(rhs.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_BLUE));
				}

				private void newButton(String text, Group g) {
					Button b = new Button(g, SWT.CHECK);
					b.setText(text);
					b
							.setImage(SLImages
									.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
				}

				private void newReport(Composite parent, String text,
						Image image, final int value, final int total) {
					final Composite result = new Composite(parent, SWT.NONE);
					GridLayout layout = new GridLayout();
					// layout.horizontalSpacing=0;
					// layout.verticalSpacing=0;
					layout.marginHeight = 0;
					layout.marginWidth = 0;
					layout.numColumns = 2;
					result.setLayout(layout);

					final Button b = new Button(result, SWT.CHECK);
					b.setText(text);
					b.setImage(image);
					final Point bSize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
							false));

					final Canvas c = new Canvas(result, SWT.NONE) {

						@Override
						public Point computeSize(int hint, int hint2,
								boolean changed) {
							return new Point(100, bSize.y);
						}
					};
					// c.setBackground(result.getShell().getDisplay()
					// .getSystemColor(SWT.COLOR_LIST_SELECTION));
					c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
							false));
					c.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent e) {
							final Display display = result.getDisplay();
							Point cSize = c.computeSize(SWT.DEFAULT,
									SWT.DEFAULT);
							GC gc = e.gc;
							Color foreground = gc.getForeground();
							Color background = gc.getBackground();
							gc.setForeground(display
									.getSystemColor(SWT.COLOR_RED));
							gc.setBackground(display
									.getSystemColor(SWT.COLOR_YELLOW));
							int percent = (int) (((double) value / (double) total) * 100);
							int width = (cSize.x - 1) * percent / 100;
							gc
									.fillGradientRectangle(0, 0, width,
											cSize.y, true);
							Rectangle rect2 = new Rectangle(0, 0, cSize.x - 1,
									cSize.y - 1);
							gc.drawRectangle(rect2);
							gc.setForeground(display
									.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
							String text = value + " findings";
							Point size = e.gc.textExtent(text);
							int offset = Math.max(0, (cSize.y - size.y) / 2);
							gc.drawText(text, 0 + 2, 0 + offset, true);
							gc.setForeground(background);
							gc.setBackground(foreground);
							// Do some drawing
							// Rectangle rect = ((Canvas) e.widget).getBounds();
							// e.gc.setForeground(e.display
							// .getSystemColor(SWT.COLOR_RED));
							// e.gc.drawFocus(5, 5, rect.width - 10,
							// rect.height - 10);
							// e.gc.drawText(value + " findings", 0, 0);
						}
					});
				}
			});
		}
	};

	@Override
	public void createPartControl(final Composite parent) {

		final PageBook pages = new PageBook(parent, SWT.NONE);

		final Label noFindingsPage = new Label(pages, SWT.WRAP | SWT.CENTER);
		noFindingsPage.setText(NO_FINDINGS);

		final Composite findingsPage = new Composite(pages, SWT.NONE);
		findingsPage.setLayout(new FillLayout());

		SashForm sf = new SashForm(findingsPage, SWT.VERTICAL | SWT.SMOOTH);

		f_finder = new Finder(sf, SWT.NONE);
		// f_finder.addColumn(new Finder.IColumn() {
		// public void createContents(Composite panel, int index) {
		// final Table table = new Table(panel, SWT.BORDER);
		// table.setHeaderVisible(true);
		// table.setLinesVisible(true);
		// TableColumn column1 = new TableColumn(table, SWT.NONE);
		// column1.setText("Bug Status");
		// column1.setWidth(100);
		// final TableColumn column2 = new TableColumn(table, SWT.NONE);
		// column2.setText("Percent");
		// column2.setWidth(200);
		// String[] labels = new String[] { "Resolved", "New",
		// "Won't Fix", "Invalid" };
		// for (int i = 0; i < labels.length; i++) {
		// TableItem item = new TableItem(table, SWT.NONE);
		// item.setText(labels[i]);
		// }
		// /*
		// * NOTE: MeasureItem, PaintItem and EraseItem are called
		// * repeatedly. Therefore, it is critical for performance that
		// * these methods be as efficient as possible.
		// */
		// table.addListener(SWT.PaintItem, new Listener() {
		// int[] percents = new int[] { 50, 30, 5, 15 };
		//
		// public void handleEvent(Event event) {
		// Display display = f_finder.getDisplay();
		// if (event.index == 1) {
		// GC gc = event.gc;
		// TableItem item = (TableItem) event.item;
		// int index = table.indexOf(item);
		// int percent = percents[index];
		// Color foreground = gc.getForeground();
		// Color background = gc.getBackground();
		// gc.setForeground(display
		// .getSystemColor(SWT.COLOR_RED));
		// gc.setBackground(display
		// .getSystemColor(SWT.COLOR_YELLOW));
		// int width = (column2.getWidth() - 1) * percent
		// / 100;
		// gc.fillGradientRectangle(event.x, event.y, width,
		// event.height, true);
		// Rectangle rect2 = new Rectangle(event.x, event.y,
		// width - 1, event.height - 1);
		// gc.drawRectangle(rect2);
		// gc.setForeground(display
		// .getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		// String text = percent + "%";
		// Point size = event.gc.textExtent(text);
		// int offset = Math.max(0,
		// (event.height - size.y) / 2);
		// gc.drawText(text, event.x + 2, event.y + offset,
		// true);
		// gc.setForeground(background);
		// gc.setBackground(foreground);
		// }
		// }
		// });
		//
		// }
		// });
		f_listener.handleEvent(null);

		/*
		 * Findings for the project
		 */

		final Composite topSash = new Composite(sf, SWT.NONE);
		topSash.setLayout(new GridLayout());

		final Composite findingsBar = new Composite(topSash, SWT.NONE);
		final GridLayout findingsBarLayout = new GridLayout();
		findingsBarLayout.numColumns = 4;
		findingsBar.setLayout(findingsBarLayout);
		findingsBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final ToolBar organizeByBar = new ToolBar(findingsBar, SWT.HORIZONTAL);
		final ToolItem organizations = new ToolItem(organizeByBar, SWT.PUSH);
		organizations.setImage(SLImages.getImage(SLImages.IMG_CATEGORY));
		organizations
				.setToolTipText("Define how findings are organized within this view");

		final Label byLabel = new Label(findingsBar, SWT.NONE);
		byLabel.setText("by");
		final Combo organizeByCombo = new Combo(findingsBar, SWT.DROP_DOWN
				| SWT.READ_ONLY);

		/*
		 * Toolbar for analysis findings
		 */

		final ToolBar toolBar = new ToolBar(findingsBar, SWT.HORIZONTAL);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		final ToolItem filter = new ToolItem(toolBar, SWT.DROP_DOWN);
		filter.setImage(SLImages.getImage(SLImages.IMG_FILTER));
		filter
				.setToolTipText("Configure the filters to be applied to this view");
		filter.setText("Filters");

		new ToolItem(toolBar, SWT.SEPARATOR);

		final ToolItem export = new ToolItem(toolBar, SWT.PUSH);
		export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		export.setToolTipText("Export findings to a file");
		export.setText("Export");

		final Menu toolBarMenu = new Menu(findingsPage.getShell(), SWT.POP_UP);
		final MenuItem showText = new MenuItem(toolBarMenu, SWT.CHECK);
		showText.setText("Show Text");
		showText.setSelection(true);
		showText.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (showText.getSelection()) {
					filter.setText("Filters");
					export.setText("Export");
				} else {
					filter.setText("");
					export.setText("");
				}
				topSash.layout();
			}
		});
		findingsBar.setMenu(toolBarMenu);
		organizeByBar.setMenu(toolBarMenu);
		toolBar.setMenu(toolBarMenu);
		byLabel.setMenu(toolBarMenu);

		ExpandBar bar = new ExpandBar(sf, SWT.V_SCROLL);
		int barIndex = 0;
		// Second item
		final Composite detailsComp = new Composite(bar, SWT.NONE);
		final ExpandItem detailsItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		detailsItem.setText("Details");
		detailsItem.setControl(detailsComp);
		detailsItem.setImage(SLImages.getImage(SLImages.IMG_DETAILS));

		// Second item
		final Composite logComp = new Composite(bar, SWT.NONE);
		final ExpandItem logItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		logItem.setText("Log");
		logItem.setControl(logComp);
		logItem.setImage(SLImages.getImage(SLImages.IMG_COMMENT));

		sf.setWeights(new int[] { 5, 3, 1 });
		bar.setSpacing(2);

		pages.showPage(findingsPage);
	}

	@Override
	public void dispose() {

		super.dispose();
	}

	@Override
	public void setFocus() {

	}
}
