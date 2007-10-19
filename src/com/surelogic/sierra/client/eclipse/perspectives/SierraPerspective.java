package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.sierra.client.eclipse.views.FindingsDetailsView;
import com.surelogic.sierra.client.eclipse.views.FindingsSelectionView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class SierraPerspective implements IPerspectiveFactory {

	public static final String SIERRA_PERSPECTIVE = "com.surelogic.sierra.client.eclipse.perspectives.SierraPerspective";

	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		final String finderArea = FindingsSelectionView.class.getName();

		layout.addView(finderArea, IPageLayout.LEFT, 0.55f, editorArea);

		final IFolderLayout belowFinder = layout.createFolder("belowFinder",
				IPageLayout.BOTTOM, 0.45f, finderArea);
		belowFinder.addView(FindingsDetailsView.class.getName());

		final IFolderLayout belowEditor = layout.createFolder("belowEditor",
				IPageLayout.BOTTOM, 0.70f, editorArea);
		belowEditor.addView(SierraServersView.class.getName());
		belowEditor.addView("org.eclipse.jdt.ui.PackageExplorer");
	}
}
