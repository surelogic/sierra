package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.client.eclipse.views.FindingsView;
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
    final String packageExplorer = "org.eclipse.jdt.ui.PackageExplorer";
    final String editorArea = layout.getEditorArea();
    final String aboveEditorArea = "aboveEditorArea";

    final IFolderLayout aboveEditorAreaF = layout.createFolder(aboveEditorArea, IPageLayout.TOP, 0.4f, editorArea);
    aboveEditorAreaF.addView(FindingsSelectionView.ID);
    aboveEditorAreaF.addView(packageExplorer);

    final IFolderLayout rightSearchAreaF = layout.createFolder("rightSearchArea", IPageLayout.RIGHT, 0.6f, aboveEditorArea);
    rightSearchAreaF.addView(FindingsView.ID);
    /*
     * The local team server view only will exist if that optional feature is
     * loaded. So we need to check before we add this to the perspective.
     */
    if (EclipseUtility.isLocalTeamServerInstalled()) {
      aboveEditorAreaF.addView(localTeamServerView);
    }

    final IFolderLayout leftOfEditorArea = layout.createFolder("leftOfEditorArea", IPageLayout.LEFT, 0.4f, editorArea);
    leftOfEditorArea.addView(FindingDetailsView.ID);
    leftOfEditorArea.addView(SierraServersView.ID);
    leftOfEditorArea.addView(SynchronizeView.ID);

    layout.addPlaceholder(SynchronizeDetailsView.ID, IPageLayout.BOTTOM, 0.6f, editorArea);
  }
}
