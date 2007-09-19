package com.surelogic.sierra.client.eclipse.views;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class FindingsView2 extends ViewPart {

	private static final String NO_FINDINGS = "No findings ... please run Sierra analysis on a project to generate a set of findings";

	private FindingsMediator f_mediator = null;

	Finder f_finder;

	Finder.IColumn f_columnC = new Finder.IColumn() {

		public void createContents(Composite panel, int index) {
			Composite rhs = panel;
			GridLayout gl = new GridLayout();
			gl.numColumns = 2;
			rhs.setLayout(gl);

			Group g = new Group(panel, SWT.NONE);
			g.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false, false));
			g.setText("Projects");
			RowLayout layout = new RowLayout(SWT.VERTICAL);
			layout.fill = true;
			layout.wrap = false;
			g.setLayout(layout);
			// Text t = new Text(g, SWT.SINGLE | SWT.SEARCH);
			Label lt = new Label(g, SWT.RIGHT);
			lt.setText("Findings");
			newReport(g, "Common", null, 450, 1260);
			newReport(g, "Fluid", null, 800, 1260);
			newReport(g, "JEdit", null, 10, 1260);
			Label st = new Label(g, SWT.RIGHT);
			st.setText("1,260");

			final Link li = new Link(g, SWT.WRAP);
			li
					.setText("<A HREF=\"select\">Select All</A> <A HREF=\"deselect\">Deselect All</A>");
			// Composite controls = new Composite(g, SWT.NONE);
			// RowLayout rlc = new RowLayout(SWT.HORIZONTAL);
			// rlc.wrap = false;
			// controls.setLayout(rlc);
			//			
			// Button sa = new Button(controls, SWT.FLAT);
			// sa.setText("Select All");
			// Button dsa = new Button(controls, SWT.FLAT);
			// dsa.setText("Deselect all");
			g.pack();
			rhs = new Composite(panel, SWT.NONE);
			rhs.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false, false));

			RowLayout l = new RowLayout(SWT.VERTICAL);
			l.fill = true;
			rhs.setLayout(l);

			getLabel("Age", null, rhs, f_listener);
			getLabel("Audit Status", null, rhs, f_listener);
			getLabel("Importance", null, rhs, f_listener);
			getLabel("Finding Type", null, rhs, f_listener);
			getLabel("Package", null, rhs, f_listener);
			getLabel("Project", null, rhs, f_listener);
			getLabel("Recent Activity", null, rhs, f_listener);
			getLabel("Tool", null, rhs, f_listener);

			// Composite c = new Composite(rhs, SWT.NONE);
			// c.setLayout(new RowLayout(SWT.HORIZONTAL));
			// new Button(c, SWT.NONE).setText("Show");
			// new Button(c, SWT.NONE).setText("Graph");
		}

		private void newButton(String text, Group g) {
			Button b = new Button(g, SWT.CHECK);
			b.setText(text);
			b.setImage(SLImages
					.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		}

		private void newReport(Composite parent, String text, Image image,
				final int value, final int total) {
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
			b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			final Canvas c = new Canvas(result, SWT.NONE) {

				@Override
				public Point computeSize(int hint, int hint2, boolean changed) {
					return new Point(75, bSize.y);
				}
			};
			// c.setBackground(result.getShell().getDisplay()
			// .getSystemColor(SWT.COLOR_LIST_SELECTION));
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
					if (width < 2)
						width = 2;
					gc.fillGradientRectangle(0, 0, width, cSize.y, true);
					Rectangle rect2 = new Rectangle(0, 0, cSize.x - 1,
							cSize.y - 1);
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
					// gc.setForeground(display
					// .getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					String text = value + "";
					Point size = e.gc.textExtent(text);
					int offset = Math.max(0, (cSize.y - size.y) / 2);
					int rightJ = cSize.x - 2 - size.x;
					gc.drawText(text, rightJ, 0 + offset, true);
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
	};

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
					// f_finder.emptyAfter(f_finder.getColumnIndex(iL));
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
			Widget in = event.widget;
			if (!(in instanceof Control))
				f_finder.addColumn(f_columnC);
			else
				f_finder.addColumnAfter(f_columnC, f_finder
						.getColumnIndex((Control) in));
		}
	};

	@Override
	public void createPartControl(final Composite parent) {

		final PageBook pages = new PageBook(parent, SWT.NONE);

		final Label noFindingsPage = new Label(pages, SWT.WRAP | SWT.CENTER);
		noFindingsPage.setText(NO_FINDINGS);

		final Composite findingsPage = new Composite(pages, SWT.NONE);
		// findingsPage.setLayout(new FillLayout());

		// SashForm sf = new SashForm(findingsPage, SWT.VERTICAL | SWT.SMOOTH);

		// top

		Composite finderC = findingsPage;
		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		finderC.setLayout(gl);

		Composite cbc = new Composite(finderC, SWT.NONE);
		gl = new GridLayout();
		gl.numColumns = 2;
		cbc.setLayout(gl);

		ToolBar tbHome = new ToolBar(cbc, SWT.HORIZONTAL | SWT.FLAT);
		tbHome
				.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
						false));
		ToolItem homeItem = new ToolItem(tbHome, SWT.PUSH);
		homeItem.setImage(SLImages.getImage(SLImages.IMG_QUERY));
		final Link li = new Link(cbc, SWT.WRAP);
		li
				.setText("<A HREF=\"select\">Project</A> > <A HREF=\"deselect\">Importance</A>");
		li.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));

		f_finder = new Finder(finderC, SWT.NONE);
		f_finder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		f_finder.addColumn(f_columnC);

		Composite clilb = new Composite(finderC, SWT.NONE);
		clilb
				.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
						false));
		clilb.setLayout(new RowLayout(SWT.HORIZONTAL));

		//Button lilb = new Button(clilb, SWT.PUSH|SWT.FLAT);
		//lilb.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		final Link lib = new Link(clilb, SWT.WRAP);
		lib
				.setText("Saved: <A HREF=\"select\">New Stuff</A> <A HREF=\"deselect\">Cross Project</A>  <A HREF=\"deselect\">My Packages</A>");

		// bottom
		//
		// TabFolder folder = new TabFolder(sf, SWT.NONE);
		// TabItem tabTable = new TabItem(folder, SWT.NONE);
		// tabTable.setText("Table");
		// TabItem tabGraph = new TabItem(folder, SWT.NONE);
		// tabGraph.setText("Graph");

		/*
		 * Findings for the project
		 */

		// final Composite topSash = new Composite(sf, SWT.NONE);
		// topSash.setLayout(new GridLayout());
		//
		// final Composite findingsBar = new Composite(topSash, SWT.NONE);
		// final GridLayout findingsBarLayout = new GridLayout();
		// findingsBarLayout.numColumns = 4;
		// findingsBar.setLayout(findingsBarLayout);
		// findingsBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
		// false));
		//
		// final ToolBar organizeByBar = new ToolBar(findingsBar,
		// SWT.HORIZONTAL);
		// final ToolItem organizations = new ToolItem(organizeByBar, SWT.PUSH);
		// organizations.setImage(SLImages.getImage(SLImages.IMG_CATEGORY));
		// organizations
		// .setToolTipText("Define how findings are organized within this
		// view");
		//
		// final Label byLabel = new Label(findingsBar, SWT.NONE);
		// byLabel.setText("by");
		// final Combo organizeByCombo = new Combo(findingsBar, SWT.DROP_DOWN
		// | SWT.READ_ONLY);
		//
		// /*
		// * Toolbar for analysis findings
		// */
		//
		// final ToolBar toolBar = new ToolBar(findingsBar, SWT.HORIZONTAL);
		// toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
		// false));
		//
		// final ToolItem filter = new ToolItem(toolBar, SWT.DROP_DOWN);
		// filter.setImage(SLImages.getImage(SLImages.IMG_FILTER));
		// filter
		// .setToolTipText("Configure the filters to be applied to this view");
		// filter.setText("Filters");
		//
		// new ToolItem(toolBar, SWT.SEPARATOR);
		//
		// final ToolItem export = new ToolItem(toolBar, SWT.PUSH);
		// export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		// export.setToolTipText("Export findings to a file");
		// export.setText("Export");
		//
		// final Menu toolBarMenu = new Menu(findingsPage.getShell(),
		// SWT.POP_UP);
		// final MenuItem showText = new MenuItem(toolBarMenu, SWT.CHECK);
		// showText.setText("Show Text");
		// showText.setSelection(true);
		// showText.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event event) {
		// if (showText.getSelection()) {
		// filter.setText("Filters");
		// export.setText("Export");
		// } else {
		// filter.setText("");
		// export.setText("");
		// }
		// topSash.layout();
		// }
		// });
		// findingsBar.setMenu(toolBarMenu);
		// organizeByBar.setMenu(toolBarMenu);
		// toolBar.setMenu(toolBarMenu);
		// byLabel.setMenu(toolBarMenu);
		//
		// ExpandBar bar = new ExpandBar(sf, SWT.V_SCROLL);
		// int barIndex = 0;
		// // Second item
		// final Composite detailsComp = new Composite(bar, SWT.NONE);
		// final ExpandItem detailsItem = new ExpandItem(bar, SWT.NONE,
		// barIndex++);
		// detailsItem.setText("Details");
		// detailsItem.setControl(detailsComp);
		// detailsItem.setImage(SLImages.getImage(SLImages.IMG_DETAILS));
		//
		// // Second item
		// final Composite logComp = new Composite(bar, SWT.NONE);
		// final ExpandItem logItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		// logItem.setText("Log");
		// logItem.setControl(logComp);
		// logItem.setImage(SLImages.getImage(SLImages.IMG_COMMENT));
		// sf.setWeights(new int[] { 3, 5 });
		// bar.setSpacing(2);
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
