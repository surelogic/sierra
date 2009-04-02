package com.surelogic.sierra.client.eclipse.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.ToolUtil;

public class ToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String DESELECT_TOOL_WARNING = "A tool that is not checked will be skipped during all scans.  For more fine-grained control of scan results, setup a <A HREF=\"scan filter\">'Scan Filter'</A> instead.";
	private static final String TAB_SPACE = "\t";

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

	BooleanFieldEditor[] flags;	
	
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

		// Sort tools by name
		final List<IToolFactory> factories = ToolUtil.findToolFactories();
		Collections.sort(factories, new Comparator<IToolFactory>() {
			public int compare(IToolFactory o1, IToolFactory o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		final List<BooleanFieldEditor> editors = new ArrayList<BooleanFieldEditor>();
		for(IToolFactory tf : factories) {
			BooleanFieldEditor flag = 
				new BooleanFieldEditor(PreferenceConstants.getToolPref(tf), tf.getName(), toolsGroup);
			flag.setPage(this);
			flag.setPreferenceStore(getPreferenceStore());
			flag.load();
			addSpacedText(toolsGroup, tf.getHTMLInfo());
			editors.add(flag);
		}
		flags = editors.toArray(new BooleanFieldEditor[editors.size()]);
		
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
		for(BooleanFieldEditor flag : flags) {
			flag.store();
		}
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		for(BooleanFieldEditor flag : flags) {
			flag.loadDefault();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		for(BooleanFieldEditor flag : flags) {
			flag.store();
		}
		return super.performOk();
	}
}
