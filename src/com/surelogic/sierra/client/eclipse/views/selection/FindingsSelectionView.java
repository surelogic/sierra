package com.surelogic.sierra.client.eclipse.views.selection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.jsure.ImportJSureAction;
import com.surelogic.sierra.client.eclipse.views.AbstractSierraView;
import com.surelogic.sierra.client.eclipse.wizards.FindingSearchExportWizard;
import com.surelogic.sierra.client.eclipse.wizards.FindingSearchImportWizard;

public final class FindingsSelectionView extends AbstractSierraView<FindingsSelectionMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView";

	@Override
	protected FindingsSelectionMediator createMorePartControls(final Composite findingsPage) {
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
		layout.numColumns = 4;
		layout.verticalSpacing = 0;
		breadcrumbsPanel.setLayout(layout);

		final Link breadcrumbs = new Link(breadcrumbsPanel, SWT.NORMAL);
		breadcrumbs.setText("");
		breadcrumbs
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		final Label findingsIcon = new Label(breadcrumbsPanel, SWT.NONE);
		findingsIcon.setText("");
		findingsIcon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Link findingsStatus = new Link(breadcrumbsPanel, SWT.NONE);
		findingsStatus.setText("");
		findingsStatus
				.setToolTipText("Click to change the maximum number of findings shown in this view");
		findingsStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));

		final ToolBar clearSelectionBar = new ToolBar(breadcrumbsPanel,
				SWT.HORIZONTAL | SWT.FLAT);
		clearSelectionBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER,
				false, false));
		final ToolItem columnSelectionItem = new ToolItem(clearSelectionBar,
				SWT.DROP_DOWN);
		columnSelectionItem.setImage(SLImages.getImage(CommonImages.IMG_COLUMNS));
		columnSelectionItem.setToolTipText("Set Columns to Show");

		final ToolItem clearSelectionItem = new ToolItem(clearSelectionBar,
				SWT.PUSH);
		clearSelectionItem.setImage(SLImages.getImage(CommonImages.IMG_GRAY_X));
		clearSelectionItem.setToolTipText("Clear Current Search");

		final CascadingList cascadingList = new CascadingList(findingsPage,
				SWT.NONE);
		cascadingList
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.SELECT_ALL.getId(), new Action() {
					@Override
					public void run() {
						f_mediator.selectAll();
					}
				});

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
				.getImage(CommonImages.IMG_SIERRA_INVESTIGATE_DOT));
		openSearchItem.setToolTipText("Open Search");
		final ToolItem saveSearchAsItem = new ToolItem(searchBar, SWT.PUSH);
		saveSearchAsItem.setImage(SLImages.getImage(CommonImages.IMG_SAVEAS_EDIT));
		saveSearchAsItem.setToolTipText("Save Search As");
		final ToolItem deleteSearchItem = new ToolItem(searchBar, SWT.PUSH);
		deleteSearchItem.setImage(SLImages.getImage(CommonImages.IMG_GRAY_X_DOT));
		deleteSearchItem.setToolTipText("Delete Saved Search");
		final Link savedSelections = new Link(selectionPersistencePanel,
				SWT.WRAP);
		savedSelections.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true));

		/*
		 * Allow direct access to the import and export wizards from the view.
		 */
		final Action importAction = new Action("Import Searches...") {
			@Override
			public void run() {
				final FindingSearchImportWizard wizard = new FindingSearchImportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		addToViewMenu(importAction);
		final Action exportAction = new Action("Export Searches...") {
			@Override
			public void run() {
				final FindingSearchExportWizard wizard = new FindingSearchExportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		addToViewMenu(exportAction);
		
		if (XUtil.useExperimental()) {			
			final Action jsureAction = new Action("Import JSure Document") {
				@Override
				public void run() {
					new ImportJSureAction().run(null);
				}
			};
			addToViewMenu(jsureAction);
		}

		return new FindingsSelectionMediator(this, 
				findingsPage, cascadingList, clearSelectionItem, breadcrumbs,
				findingsIcon, findingsStatus, columnSelectionItem,
				openSearchItem, saveSearchAsItem, deleteSearchItem,
				savedSelections);
	}
}
