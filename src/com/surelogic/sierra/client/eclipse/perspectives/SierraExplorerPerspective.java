package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.sierra.client.eclipse.views.FindingsFinderView;
import com.surelogic.sierra.client.eclipse.views.FindingsGraphView;
import com.surelogic.sierra.client.eclipse.views.FindingsView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class SierraExplorerPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		final String finderArea = FindingsFinderView.class.getName();

		layout.addView(finderArea, IPageLayout.LEFT, 0.55f, editorArea);

		final IFolderLayout belowFinder = layout.createFolder("belowFinder",
				IPageLayout.BOTTOM, 0.40f, finderArea);
		belowFinder.addView(FindingsGraphView.class.getName());
		belowFinder.addView(FindingsView.class.getName());

		final IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.70f, editorArea);
		bottom.addView("org.eclipse.jdt.ui.PackageExplorer");
		bottom.addView(SierraServersView.class.getName());
	}
}
