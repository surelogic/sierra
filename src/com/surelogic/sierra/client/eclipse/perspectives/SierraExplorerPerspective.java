package com.surelogic.sierra.client.eclipse.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.surelogic.sierra.client.eclipse.views.FindingsView;

/**
 * Defines the Sierra Explorer perspective within the workbench.
 */
public final class SierraExplorerPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();

		layout.addView("org.eclipse.jdt.ui.PackageExplorer", IPageLayout.LEFT,
				0.20f, editorArea);

		layout.addView(FindingsView.class.getName(), IPageLayout.TOP, 0.80f,
				editorArea);
	}
}
