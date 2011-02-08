package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;
import com.surelogic.sierra.client.eclipse.views.SynchronizeDetailsView;
import com.surelogic.sierra.client.eclipse.views.SynchronizeView;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class CodeReviewPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		final String localTeamServerId = "com.surelogic.sierra.eclipse.teamserver";
		final String localTeamServerView = "com.surelogic.sierra.eclipse.teamserver.views.TeamServerView";
		final String packageExplorer = "org.eclipse.jdt.ui.PackageExplorer";
		final String editorArea = layout.getEditorArea();
		final String quickSearchArea = FindingsSelectionView.ID;

		final IFolderLayout aboveEditorArea = layout.createFolder(
				"aboveEditorArea", IPageLayout.TOP, 0.4f, editorArea);
		aboveEditorArea.addView(quickSearchArea);
		aboveEditorArea.addView(packageExplorer);
		/*
		 * The local team server view only will exist if that optional feature
		 * is loaded. So we need to check before we add this to the perspective.
		 */
		if (EclipseUtility.bundleExists(localTeamServerId)) {
			aboveEditorArea.addView(localTeamServerView);
		} else {
			SLLogger.getLogger().fine("No team server");
		}

		final IFolderLayout leftOfEditorArea = layout.createFolder(
				"leftOfEditorArea", IPageLayout.LEFT, 0.4f, editorArea);
		leftOfEditorArea.addView(FindingDetailsView.ID);
		leftOfEditorArea.addView(SierraServersView.ID);
		leftOfEditorArea.addView(SynchronizeView.ID);

		layout.addPlaceholder(SynchronizeDetailsView.ID, IPageLayout.BOTTOM,
				0.6f, editorArea);
	}
}
