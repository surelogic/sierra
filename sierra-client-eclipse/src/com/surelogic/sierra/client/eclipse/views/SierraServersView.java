package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public final class SierraServersView extends
		AbstractSierraView<SierraServersMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SierraServersView";

	public static final int INFO_WIDTH_HINT = 70;

	@Override
	protected SierraServersMediator createMorePartControls(
			final Composite parent) {
		final TreeViewer statusTree = new TreeViewer(parent, SWT.MULTI);		
		return new SierraServersMediator(this, statusTree);
	}
	
	@Override
	protected Action createPreferencesAction() {
		final Action serverInteractionAction = 
			new Action(PREFERENCES_MSG, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				final PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(null,
								PreferencesAction.SERVER_INTERACTION_ID,
								PreferencesAction.FILTER, null);
				dialog.open();
			}
		};
		return serverInteractionAction;
	}
}
