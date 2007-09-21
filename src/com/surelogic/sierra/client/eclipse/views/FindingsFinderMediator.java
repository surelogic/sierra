package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IPorousObserver;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public final class FindingsFinderMediator implements IProjectsObserver {

	private final PageBook f_pages;
	private final Control f_noFindingsPage;
	private final Control f_findingsPage;
	private final CascadingList f_finder;
	private final ToolItem f_clearSelectionItem;
	private final Link f_breadcrumbs;
	private final Link f_savedSelections;

	private final SelectionManager f_manager = SelectionManager.getInstance();

	private Selection f_workingSelection;

	FindingsFinderMediator(PageBook pages, Control noFindingsPage,
			Control findingsPage, CascadingList finder,
			ToolItem clearSelectionItem, Link breadcrumbs, Link savedSelections) {
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_finder = finder;
		f_clearSelectionItem = clearSelectionItem;
		f_breadcrumbs = breadcrumbs;
		f_savedSelections = savedSelections;
	}

	public void init() {
		f_savedSelections.setText("Saved selections:");

		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_finder.setFocus();
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void notify(Projects p) {
		final Control page;
		if (p.isEmpty()) {
			page = f_noFindingsPage;
		} else {
			page = f_findingsPage;
		}
		// beware the thread context this method call might be made in.
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				f_pages.showPage(page);
			}
		});
	}

	private void reset() {
		f_breadcrumbs.setText("");
		f_workingSelection = f_manager.construct();
		f_finder.addColumnAfter(new CascadingList.IColumn() {
			public void createContents(Composite panel, int index) {
				constructFilterSelectorMenu(panel);
			}
		}, -1);
	}

	Composite constructFilterSelectorMenu(Composite parent) {
		final Composite menu = new Composite(parent, SWT.NONE);
		final RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		menu.setLayout(layout);

		for (final ISelectionFilterFactory factory : f_workingSelection
				.getAvailableFilters()) {
			constructFilterSelector(factory.getFilterLabel(), null, menu,
					null, new Listener() {
						public void handleEvent(Event event) {
							// TODO Auto-generated method stub

						}
					});
		}

		return menu;
	}

	Composite constructFilterSelector(String text, Image image,
			Composite parent, Filter filter, final Listener onClick) {
		final Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
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

			private boolean f_toggle = true;

			public void handleEvent(Event event) {
				if (f_toggle) {
					result.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					iL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					tL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					aL.setBackground(result.getShell().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					if (onClick != null)
						onClick.handleEvent(event);
				} else {
					result.setBackground(null);
					iL.setBackground(null);
					tL.setBackground(null);
					aL.setBackground(null);
				}
				f_toggle = !f_toggle;
			}
		});
		return result;
	}

	static class FilterSelecterListener implements Listener, IPorousObserver {

		private final Composite f_control;

		public FilterSelecterListener(Composite control) {
			assert control != null;
			f_control = control;
		}

		public void handleEvent(Event event) {
			// TODO Auto-generated method stub

		}

		public void porous(Filter filter) {
			// TODO Auto-generated method stub

		}

	}
}
