package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Activator;

public class ServerInteractionPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	static private final String SERVER_PERIOD_LABEL = "sierra.eclipse.serverInteractionPeriodPreferenceLabel";

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to control automatically interaction with connected team servers.");
	}

	RadioGroupFieldEditor f_serverInteractionSetting;
	Group f_group;
	ScaleFieldEditor f_periodInMinutes;
	IntegerFieldEditor f_auditThreshold;

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		f_serverInteractionSetting = new RadioGroupFieldEditor(
				PreferenceConstants.P_SERVER_INTERACTION_SETTING,
				"Automatically synchronize your audits",
				1,
				new String[][] {
						{ ServerInteractionSetting.NEVER.getLabel(),
								ServerInteractionSetting.NEVER.toString() },
						{ ServerInteractionSetting.CHECK.getLabel(),
								ServerInteractionSetting.CHECK.toString() },
						{ ServerInteractionSetting.PERIODIC.getLabel(),
								ServerInteractionSetting.PERIODIC.toString() },
						{ ServerInteractionSetting.THRESHOLD.getLabel(),
								ServerInteractionSetting.THRESHOLD.toString() } },
				panel);
		f_serverInteractionSetting.setPage(this);
		f_serverInteractionSetting.setPreferenceStore(getPreferenceStore());
		f_serverInteractionSetting.load();
		f_serverInteractionSetting
				.setPropertyChangeListener(new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						final ServerInteractionSetting value = ServerInteractionSetting
								.valueOf(event.getNewValue().toString());
						mediateDialogState(value);
					}
				});

		f_group = new Group(panel, SWT.NONE);
		f_group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		f_group.setLayout(new GridLayout());
		f_group.setText("Server Interaction Preferences");

		int periodMin = PreferenceConstants
				.getServerInteractionPeriodInMinutes();
		final String label = I18N.msg(SERVER_PERIOD_LABEL, periodMin);
		f_periodInMinutes = new ScaleFieldEditor(
				PreferenceConstants.P_SERVER_INTERACTION_PERIOD_IN_MINUTES,
				label + "     ", f_group);
		f_periodInMinutes.setMinimum(1);
		f_periodInMinutes.setMaximum(20);
		f_periodInMinutes.setPageIncrement(1);
		f_periodInMinutes.setPage(ServerInteractionPreferencePage.this);
		f_periodInMinutes.setPreferenceStore(getPreferenceStore());
		f_periodInMinutes.load();
		f_periodInMinutes.getScaleControl().addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						updateScaleLabelText();
					}
				});

		f_auditThreshold = new IntegerFieldEditor(
				PreferenceConstants.P_SERVER_INTERACTION_AUDIT_THRESHOLD,
				"Audit threshold (# of audits that have not been synchronized):",
				f_group);
		f_auditThreshold.setPage(this);
		f_auditThreshold.setPreferenceStore(getPreferenceStore());
		f_auditThreshold.load();

		GridLayout groupLayout = (GridLayout) f_group.getLayout();
		groupLayout.numColumns = 1;
		f_group.setLayout(groupLayout);

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		mediateDialogState(PreferenceConstants.getServerInteractionSetting());

		return panel;
	}

	private void updateScaleLabelText() {
		final int periodMin = f_periodInMinutes.getScaleControl()
				.getSelection();
		f_periodInMinutes
				.setLabelText(I18N.msg(SERVER_PERIOD_LABEL, periodMin));
	}

	private void mediateDialogState(final ServerInteractionSetting currentChoice) {
		f_group.setEnabled(currentChoice != ServerInteractionSetting.NEVER);
		f_periodInMinutes.setEnabled(
				currentChoice != ServerInteractionSetting.NEVER, f_group);
		f_auditThreshold.setEnabled(
				currentChoice == ServerInteractionSetting.THRESHOLD, f_group);
	}

	@Override
	protected void performDefaults() {
		f_serverInteractionSetting.loadDefault();
		f_periodInMinutes.loadDefault();
		f_auditThreshold.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_serverInteractionSetting.store();
		f_periodInMinutes.store();
		f_auditThreshold.store();
		return super.performOk();
	}
}