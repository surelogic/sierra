package com.surelogic.sierra.eclipse.teamserver.preferences;

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

import com.surelogic.common.eclipse.MemoryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.eclipse.teamserver.Activator;

public class LocalTeamServerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	static private final String ESTIMATE_LABEL = "sierra.eclipse.teamserver.computedMaxToolMemoryLabel";
	static private final String SERVER_MB_LABEL = "sierra.eclipse.teamserver.serverMemoryPreferenceLabel";

	Label f_estimate;
	ScaleFieldEditor f_serverMemoryMB;

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize your local Sierra team server.");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group memoryGroup = new Group(panel, SWT.NONE);
		memoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		memoryGroup.setText("Memory Use");

		final int estimatedMax = MemoryUtility.computeMaxMemorySize();
		int mb = PreferenceConstants.getServerMemoryMB();
		if (mb > estimatedMax) {
			mb = estimatedMax;
			PreferenceConstants.setServerMemoryMB(mb);
		}

		final String label = I18N.msg(SERVER_MB_LABEL, mb);
		f_serverMemoryMB = new ScaleFieldEditor(
				PreferenceConstants.P_SERVER_MEMORY_MB, label + "     ",
				memoryGroup);
		f_serverMemoryMB.setMinimum(256);
		f_serverMemoryMB.setMaximum(estimatedMax);
		f_serverMemoryMB.setPageIncrement(256);
		f_serverMemoryMB.setPage(this);
		f_serverMemoryMB.setPreferenceStore(getPreferenceStore());
		f_serverMemoryMB.load();
		f_serverMemoryMB.getScaleControl().addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						updateMBInLabel();
					}
				});

		f_estimate = new Label(memoryGroup, SWT.NONE);
		f_estimate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		f_estimate.setText(I18N.msg(ESTIMATE_LABEL, estimatedMax));

		if (memoryGroup.getLayout() instanceof GridLayout) {
			GridLayout gl = (GridLayout) memoryGroup.getLayout();
			gl.numColumns = 1;
			memoryGroup.setLayout(gl);
		}

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	private void updateMBInLabel() {
		final int mb = f_serverMemoryMB.getScaleControl().getSelection();
		f_serverMemoryMB.setLabelText(I18N.msg(SERVER_MB_LABEL, mb));
	}

	@Override
	protected void performDefaults() {
		f_serverMemoryMB.loadDefault();
		super.performDefaults();

		updateMBInLabel();
	}

	@Override
	public boolean performOk() {
		f_serverMemoryMB.store();
		return super.performOk();
	}
}