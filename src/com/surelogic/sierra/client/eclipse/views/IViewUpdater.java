package com.surelogic.sierra.client.eclipse.views;

public interface IViewUpdater {
	/**
	 * Only to be called from the SWT thread
	 */
	void updateContentsForUI();
}
