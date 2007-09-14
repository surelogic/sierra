package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class FindingsView2 extends ViewPart {

	private static final String NO_FINDINGS = "No findings ... please run Sierra analysis on a project to generate a set of findings";

	private FindingsMediator f_mediator = null;

	Finder f_finder;

	final Listener f_listener = new Listener() {
		public void handleEvent(Event event) {
			if (event != null) {
				Button pressed = (Button) event.widget;
				int index = (Integer) pressed.getData();
				f_finder.emptyAfter(index);
			}
			f_finder.addColumn(new Finder.IColumn() {
				public void createContents(Composite panel, int index) {
					panel.setLayout(new RowLayout(SWT.VERTICAL));
					panel.setBackground(f_finder.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_BLUE));
					for (int i = 0; i < 15; i++) {
						Button b = new Button(panel, SWT.NONE);
						b.setData(index);
						b.setText("FOO BAR");
						b.addListener(SWT.Selection, f_listener);
					}
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

		sf.setWeights(new int[] { 1, 3, 1 });
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
