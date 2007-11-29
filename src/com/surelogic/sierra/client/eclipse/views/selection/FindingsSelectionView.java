package com.surelogic.sierra.client.eclipse.views.selection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;

public final class FindingsSelectionView extends ViewPart {

	public static final String NO_FINDINGS = "No findings ... please run Sierra analysis on a project to generate a set of findings";

	private FindingsSelectionMediator f_mediator = null;

	@Override
	public void createPartControl(final Composite parent) {

		final PageBook pages = new PageBook(parent, SWT.NONE);

		final Label noFindingsPage = new Label(pages, SWT.WRAP);
		noFindingsPage.setText(NO_FINDINGS);

		final Composite findingsPage = new Composite(pages, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		findingsPage.setLayout(layout);

		final Composite breadcrumbsPanel = new Composite(findingsPage, SWT.NONE);
		breadcrumbsPanel.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
				true, false));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		layout.verticalSpacing = 0;
		breadcrumbsPanel.setLayout(layout);

		final Link breadcrumbs = new Link(breadcrumbsPanel, SWT.NORMAL);
		breadcrumbs.setText("");
		breadcrumbs
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		final ToolBar clearSelectionBar = new ToolBar(breadcrumbsPanel,
				SWT.HORIZONTAL | SWT.FLAT);
		clearSelectionBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER,
				false, false));
		final ToolItem clearSelectionItem = new ToolItem(clearSelectionBar,
				SWT.PUSH);
		clearSelectionItem.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_DELETE));
		clearSelectionItem.setToolTipText("Clear Current Search");

		final CascadingList cascadingList = new CascadingList(findingsPage,
				SWT.NONE);
		cascadingList
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite selectionPersistencePanel = new Composite(findingsPage,
				SWT.NONE);
		selectionPersistencePanel.setLayoutData(new GridData(SWT.FILL,
				SWT.DEFAULT, true, false));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		layout.verticalSpacing = 0;
		selectionPersistencePanel.setLayout(layout);

		final ToolBar searchBar = new ToolBar(selectionPersistencePanel,
				SWT.HORIZONTAL | SWT.FLAT);
		searchBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
				false));
		final ToolItem openSearchItem = new ToolItem(searchBar, SWT.PUSH);
		openSearchItem.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_INVESTIGATE_DOT));
		openSearchItem.setToolTipText("Open Search");
		final ToolItem saveSearchAsItem = new ToolItem(searchBar, SWT.PUSH);
		saveSearchAsItem.setImage(SLImages.getImage(SLImages.IMG_SAVEAS_EDIT));
		saveSearchAsItem.setToolTipText("Save Search As");
		final ToolItem deleteSearchItem = new ToolItem(searchBar, SWT.PUSH);
		deleteSearchItem.setImage(SLImages.getImage(SLImages.IMG_GRAY_X_DOT));
		deleteSearchItem.setToolTipText("Delete Saved Search");
		final Link savedSelections = new Link(selectionPersistencePanel,
				SWT.WRAP);
		savedSelections.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true));

		f_mediator = new FindingsSelectionMediator(pages, noFindingsPage,
				findingsPage, cascadingList, clearSelectionItem, breadcrumbs,
				openSearchItem, saveSearchAsItem, deleteSearchItem,
				savedSelections);
		f_mediator.init();
	}

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (f_mediator != null)
			f_mediator.setFocus();
	}
}
