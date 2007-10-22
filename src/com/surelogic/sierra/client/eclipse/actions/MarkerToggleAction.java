package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public class MarkerToggleAction implements IWorkbenchWindowActionDelegate {

	private final MarkersHandler f_handler = MarkersHandler.getInstance();

	public void dispose() {
		// Nothing to do

	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(IAction action) {
		final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			if (PreferenceConstants.showMarkers()) {
				f_handler.clearAllMarkers();
				PreferenceConstants.setMarkersVisibility(false);
			} else {
				f_handler.changed();
				PreferenceConstants.setMarkersVisibility(true);
			}
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do

	}
}
