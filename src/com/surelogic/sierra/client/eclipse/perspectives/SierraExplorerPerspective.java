package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.sierra.client.eclipse.views.FindingsView;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class SierraExplorerPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();

		layout.addView(FindingsView.class.getName(), IPageLayout.LEFT, 0.50f,
				editorArea);

		final IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.50f, editorArea);
		bottom.addView(SierraServersView.class.getName());
		bottom.addView("org.eclipse.jdt.ui.PackageExplorer");
	}
}
