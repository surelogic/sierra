package com.surelogic.sierra.client.eclipse.preferences;

import java.net.URL;
import java.util.logging.Level;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.surelogic.common.CommonImages;
import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public class ToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String DESELECT_TOOL_WARNING = "A tool that is not checked will be skipped during all scans.  For more fine-grained control of scan results, setup a <A HREF=\"scan filter\">'Scan Filter'</A> instead.";
	private static final String FINDBUGS_INFO = "<A HREF=\"http://findbugs.sourceforge.net\">FindBugs</A> is a static analysis tool created at University of Maryland for finding bugs in Java code.";
	private static final String PMD_INFO = "<A HREF=\"http://pmd.sourceforge.net\">PMD</A> is a static analysis tool to look for multiple issues like potential bugs, dead, duplicate and sub-optimal code, and over-complicated expressions.";
	private static final String RECKONER_INFO = "<A HREF=\"http://www.surelogic.com\">Reckoner</A> is a static analysis tool created by SureLogic, Inc. that collects metrics about Java code.";
	private static final String TAB_SPACE = "\t";

	private BooleanFieldEditor f_runFindbugsFlag;
	private BooleanFieldEditor f_runPMDFlag;
	private BooleanFieldEditor f_runReckonerFlag;
	private BooleanFieldEditor f_runCheckStyleFlag;

	static final Listener LINK_LISTENER = new Listener() {
		public void handleEvent(Event event) {
			final String name = event.text;
			if (name != null) {
				if (name.startsWith("http")) {
					try {
						final IWebBrowser browser = PlatformUI
								.getWorkbench()
								.getBrowserSupport()
								.createBrowser(
										IWorkbenchBrowserSupport.LOCATION_BAR
												| IWorkbenchBrowserSupport.NAVIGATION_BAR
												| IWorkbenchBrowserSupport.STATUS,
										name, name, name);
						browser.openURL(new URL(name));
					} catch (Exception e) {
						SLLogger.getLogger().log(Level.SEVERE,
								"Exception occurred when opening " + name);
					}
				} else {
					PreferencesUtil.createPreferenceDialogOn(null,
							PreferencesAction.PREF_ID,
							PreferencesAction.FILTER, null).open();
				}
			}
		}
	};

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		panel.setLayout(gridLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		panel.setLayoutData(data);

		final Group toolsGroup = new Group(panel, SWT.NONE);
		toolsGroup.setText("Tools");
		toolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				1, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		toolsGroup.setLayout(gridLayout);

		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 100;

		f_runFindbugsFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_FINDBUGS, "FindBugs\u2122",
				toolsGroup);
		f_runFindbugsFlag.setPage(this);
		f_runFindbugsFlag.setPreferenceStore(getPreferenceStore());
		f_runFindbugsFlag.load();

		addSpacedText(toolsGroup, FINDBUGS_INFO);

		f_runPMDFlag = new BooleanFieldEditor(PreferenceConstants.P_RUN_PMD,
				"PMD\u2122", toolsGroup);
		f_runPMDFlag.setPage(this);
		f_runPMDFlag.setPreferenceStore(getPreferenceStore());
		f_runPMDFlag.load();

		addSpacedText(toolsGroup, PMD_INFO);

		f_runReckonerFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_RECKONER, "Reckoner", toolsGroup);
		f_runReckonerFlag.setPage(this);
		f_runReckonerFlag.setPreferenceStore(getPreferenceStore());
		f_runReckonerFlag.load();

		addSpacedText(toolsGroup, RECKONER_INFO);

		final Composite warning = new Composite(panel, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label warningImg = new Label(warning, SWT.NONE);
		warningImg.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
		warningImg
				.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		final Link deselectWarning = new Link(warning, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		deselectWarning.setLayoutData(data);
		deselectWarning.setText(DESELECT_TOOL_WARNING);
		deselectWarning.addListener(SWT.Selection, LINK_LISTENER);

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select the tools run during a Sierra scan.");
	}

	private void addSpacedText(Composite parent, String text) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		addSpace(composite);

		final Link infoText = new Link(composite, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		infoText.setLayoutData(data);
		infoText.setText(text);
		infoText.addListener(SWT.Selection, LINK_LISTENER);
	}

	/**
	 * Utility to add a space
	 * 
	 * @param parent
	 */
	private void addSpace(Composite parent) {
		final Label tabSpace = new Label(parent, SWT.NONE);
		tabSpace.setText(TAB_SPACE);
		tabSpace.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false,
				1, 1));
	}

	@Override
	protected void performApply() {
		f_runFindbugsFlag.store();
		if (XUtil.useExperimental()) {
			f_runCheckStyleFlag.store();
		}
		f_runPMDFlag.store();
		f_runReckonerFlag.store();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		f_runFindbugsFlag.loadDefault();
		if (XUtil.useExperimental()) {
			f_runCheckStyleFlag.loadDefault();
		}
		f_runPMDFlag.loadDefault();
		f_runReckonerFlag.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_runFindbugsFlag.store();
		if (XUtil.useExperimental()) {
			f_runCheckStyleFlag.store();
		}
		f_runPMDFlag.store();
		f_runReckonerFlag.store();
		return super.performOk();
	}
}
