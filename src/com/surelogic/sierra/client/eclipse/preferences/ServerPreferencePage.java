package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.Activator;

public class ServerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/*
	static private final String ESTIMATE_LABEL = "sierra.eclipse.teamserver.computedMaxToolMemoryLabel";
	static private final String SERVER_MB_LABEL = "sierra.eclipse.teamserver.serverMemoryPreferenceLabel";
    */
	class AutoX {
		final String f_label;
		final Group f_group;
		final BooleanFieldEditor f_autoRun;
		final ScaleFieldEditor f_autoRunDelay;
		
		AutoX(String label, Composite panel, String runKey, String delayKey) {
			f_label = label;
			f_group = new Group(panel, SWT.NONE);
			f_group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			f_group.setLayout(new GridLayout(1, false));
			f_group.setText("Auto-"+label);
			
			GridData gd;
			f_autoRun = new BooleanFieldEditor(runKey, "Enable auto-"+label, f_group);
			f_autoRun.setPage(ServerPreferencePage.this);
			f_autoRun.setPreferenceStore(getPreferenceStore());
			f_autoRun.load();

			gd = new GridData(SWT.FILL, SWT.TOP, true, true);
			new Label(f_group, SWT.NONE).setLayoutData(gd);
		
			f_autoRunDelay = new ScaleFieldEditor(delayKey, "", f_group);
			f_autoRunDelay.setMinimum(1);
			f_autoRunDelay.setMaximum(20);
			f_autoRunDelay.setPageIncrement(1);
			f_autoRunDelay.setPage(ServerPreferencePage.this);
			f_autoRunDelay.setPreferenceStore(getPreferenceStore());
			f_autoRunDelay.load();
			f_autoRunDelay.getScaleControl().addListener(SWT.Selection,
					new Listener() {
						public void handleEvent(Event event) {
							updateLabel();
						}
					});
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			f_autoRunDelay.getLabelControl(f_group).setLayoutData(gd);
			gd = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false);
			f_autoRunDelay.getScaleControl().setLayoutData(gd);
			updateLabel();
		}

		private void updateLabel() {
			f_autoRunDelay.setLabelText("Delay between "+f_label+"s: "+
					                    f_autoRunDelay.getScaleControl().getSelection()+
					                    " min");
		}
		
		public void loadDefault() {
			f_autoRun.loadDefault();
			f_autoRunDelay.loadDefault();
			updateLabel();
		}

		public void store() {
			f_autoRun.store();
			f_autoRunDelay.store();
		}
	}
	
	AutoX f_autoUpdate;
	AutoX f_autoSync;
	
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize your interactions with your Sierra team servers.");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		f_autoUpdate = new AutoX("update", panel, 
				                 PreferenceConstants.P_SERVER_AUTO_UPDATE,
				                 PreferenceConstants.P_SERVER_AUTO_UPDATE_DELAY);
		f_autoSync = new AutoX("sync", panel, 
				               PreferenceConstants.P_SERVER_AUTO_SYNC,
				               PreferenceConstants.P_SERVER_AUTO_SYNC_DELAY);

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	@Override
	protected void performDefaults() {
		f_autoUpdate.loadDefault();
		f_autoSync.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_autoUpdate.store();
		f_autoSync.store();
		return super.performOk();
	}
}