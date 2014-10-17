package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.client.eclipse.views.FindingsView;
import com.surelogic.sierra.client.eclipse.views.ScannedProjectsView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;
import com.surelogic.sierra.client.eclipse.views.SynchronizeDetailsView;
import com.surelogic.sierra.client.eclipse.views.SynchronizeView;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class CodeReviewPerspective implements IPerspectiveFactory {

  @Override
  public void createInitialLayout(IPageLayout layout) {
    final String localTeamServerView = "com.surelogic.sierra.eclipse.teamserver.views.TeamServerView";
    final String editorArea = layout.getEditorArea();

    final IFolderLayout aboveEditorAreaF = layout.createFolder("aboveEditorArea", IPageLayout.TOP, 0.4f, editorArea);
    aboveEditorAreaF.addView(FindingsSelectionView.ID);
    aboveEditorAreaF.addView(SierraServersView.ID);
    aboveEditorAreaF.addView(SynchronizeView.ID);
    /*
     * The local team server view only will exist if that optional feature is
     * loaded. So we need to check before we add this to the perspective.
     */
    if (EclipseUtility.isLocalTeamServerInstalled()) {
      aboveEditorAreaF.addView(localTeamServerView);
    }

    final IFolderLayout leftOfFindingsSelectorF = layout.createFolder("leftOfFindingsSelector", IPageLayout.LEFT, 0.2f,
        "aboveEditorArea");
    leftOfFindingsSelectorF.addView(ScannedProjectsView.ID);

    final IFolderLayout leftEditorAreaF = layout.createFolder("leftEditorArea", IPageLayout.LEFT, 0.7f, editorArea);
    leftEditorAreaF.addView(FindingDetailsView.ID);

    final IFolderLayout leftLeftAreaF = layout.createFolder("leftLeftArea", IPageLayout.LEFT, 0.5f, "leftEditorArea");
    leftLeftAreaF.addView(FindingsView.ID);

    layout.addPlaceholder(SynchronizeDetailsView.ID, IPageLayout.BOTTOM, 0.6f, editorArea);
  }
}
