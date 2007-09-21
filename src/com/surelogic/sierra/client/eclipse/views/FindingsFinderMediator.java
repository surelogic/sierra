package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;

public final class FindingsFinderMediator implements IProjectsObserver {

	private final PageBook f_pages;
	private final Control f_noFindingsPage;
	private final Control f_findingsPage;
	private final CascadingList f_finder;
	private final ToolItem f_clearSelectionItem;
	private final Link f_breadcrumbs;
	private final Link f_savedSelections;

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
}
