package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;
import com.surelogic.sierra.client.eclipse.views.SynchronizeView;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class CodeReview implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		final String localTeamServer = "com.surelogic.sierra.eclipse.teamserver.views.TeamServerView";
		final String packageExplorer = "org.eclipse.jdt.ui.PackageExplorer";
		final String editorArea = layout.getEditorArea();
		final String finderArea = FindingsSelectionView.class.getName();

		final IFolderLayout leftEditor = layout.createFolder("leftEditor",
				IPageLayout.LEFT, 0.55f, editorArea);
		leftEditor.addView(finderArea);
		leftEditor.addView(SynchronizeView.class.getName());

		final IFolderLayout belowFinder = layout.createFolder("belowFinder",
				IPageLayout.BOTTOM, 0.45f, finderArea);
		belowFinder.addView(FindingDetailsView.class.getName());

		final IFolderLayout belowEditor = layout.createFolder("belowEditor",
				IPageLayout.BOTTOM, 0.70f, editorArea);
		belowEditor.addView(SierraServersView.class.getName());
		/*
		 * The local team server view only will exist if that optional feature
		 * is loaded. So we need to check before we add this to the perspective.
		 */
		if (ViewUtility.viewExists(localTeamServer))
			belowEditor.addView(localTeamServer);
		belowEditor.addView(packageExplorer);
	}
}
