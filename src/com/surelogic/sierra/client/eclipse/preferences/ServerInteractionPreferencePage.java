package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
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
		setDescription("Use this page to control automatic interaction with connected team servers.");
	}

	ScaleFieldEditor f_periodInMinutes;
	IntegerFieldEditor f_auditThreshold;
	RadioGroupFieldEditor f_serverFailureReporting;
	IntegerFieldEditor f_retryThreshold;

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		int periodMin = PreferenceConstants
				.getServerInteractionPeriodInMinutes();
		final String label = I18N.msg(SERVER_PERIOD_LABEL, periodMin);
		f_periodInMinutes = new ScaleFieldEditor(
				PreferenceConstants.P_SERVER_INTERACTION_PERIOD_IN_MINUTES,
				label + "     ", panel);
		f_periodInMinutes.fillIntoGrid(panel, 2);
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
				panel);
		f_auditThreshold.fillIntoGrid(panel, 2);
		f_auditThreshold.setPage(this);
		f_auditThreshold.setPreferenceStore(getPreferenceStore());
		f_auditThreshold.load();

		f_serverFailureReporting = new RadioGroupFieldEditor(
				PreferenceConstants.P_SERVER_FAILURE_REPORTING,
				"Policy for handling server failures:",
				1,
				new String[][] {
						{ "Ignore", ServerFailureReport.IGNORE.toString() },
						{ "Pop-up a balloon",
								ServerFailureReport.SHOW_BALLOON.toString() },
						{ "Pop-up a dialog",
								ServerFailureReport.SHOW_DIALOG.toString() }, },
				panel);
		f_serverFailureReporting.fillIntoGrid(panel, 2);
		f_serverFailureReporting.setPage(this);
		f_serverFailureReporting.setPreferenceStore(getPreferenceStore());
		f_serverFailureReporting.load();

		f_retryThreshold = new IntegerFieldEditor(
				PreferenceConstants.P_SERVER_INTERACTION_RETRY_THRESHOLD,
				"Retry threshold (# of consecutive failures before switching into manual mode):",
				panel);
		f_retryThreshold.fillIntoGrid(panel, 2);
		f_retryThreshold.setPage(this);
		f_retryThreshold.setPreferenceStore(getPreferenceStore());
		f_retryThreshold.load();

		panel.setLayout(new GridLayout(2, false));

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	private void updateScaleLabelText() {
		final int periodMin = f_periodInMinutes.getScaleControl()
				.getSelection();
		f_periodInMinutes
				.setLabelText(I18N.msg(SERVER_PERIOD_LABEL, periodMin));
	}

	@Override
	protected void performDefaults() {
		f_periodInMinutes.loadDefault();
		f_auditThreshold.loadDefault();
		f_serverFailureReporting.loadDefault();
		f_retryThreshold.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_periodInMinutes.store();
		f_auditThreshold.store();
		f_serverFailureReporting.store();
		f_retryThreshold.store();
		return super.performOk();
	}
}