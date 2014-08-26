package com.surelogic.sierra.eclipse.teamserver.preferences;

import java.util.logging.Level;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
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

import com.surelogic.common.SLUtility;
import com.surelogic.common.core.MemoryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;

public class LocalTeamServerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  static private final String SERVER_MB_LABEL = "sierra.eclipse.teamserver.preference.page.serverMemoryPreferenceLabel";

  Label f_estimate;
  ScaleFieldEditor f_serverMemoryMB;
  RadioGroupFieldEditor f_showAbove;

  @Override
  public void init(IWorkbench workbench) {
    setPreferenceStore(EclipseUIUtility.getPreferences());
    setDescription(I18N.msg("sierra.eclipse.teamserver.preference.page.msg"));
  }

  @Override
  protected Control createContents(Composite parent) {
    final Composite panel = new Composite(parent, SWT.NONE);
    GridLayout grid = new GridLayout();
    panel.setLayout(grid);

    final Group memoryGroup = new Group(panel, SWT.NONE);
    memoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    memoryGroup.setText(I18N.msg("sierra.eclipse.teamserver.preference.page.group.memory"));

    final int estimatedMax = MemoryUtility.computeMaxMemorySizeInMb();
    int mb = LocalTeamServerPreferencesUtility.getServerMemoryMB();
    if (mb > estimatedMax) {
      mb = estimatedMax;
      LocalTeamServerPreferencesUtility.setServerMemoryMB(mb);
    }

    final String label = I18N.msg(SERVER_MB_LABEL, SLUtility.toStringHumanWithCommas(mb));
    f_serverMemoryMB = new ScaleFieldEditor(LocalTeamServerPreferencesUtility.SERVER_MEMORY_MB, label + "     ", memoryGroup);
    f_serverMemoryMB.fillIntoGrid(memoryGroup, 2);
    f_serverMemoryMB.setMinimum(256);
    f_serverMemoryMB.setMaximum(estimatedMax);
    f_serverMemoryMB.setPageIncrement(256);
    f_serverMemoryMB.setPage(this);
    f_serverMemoryMB.setPreferenceStore(EclipseUIUtility.getPreferences());
    f_serverMemoryMB.load();
    f_serverMemoryMB.getScaleControl().addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        updateMBInLabel();
      }
    });

    f_estimate = new Label(memoryGroup, SWT.NONE);
    f_estimate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
    f_estimate.setText(I18N.msg("sierra.eclipse.teamserver.preference.page.computedMaxToolMemoryLabel",
        SLUtility.toStringHumanWithCommas(estimatedMax)));

    memoryGroup.setLayout(new GridLayout(2, false));

    final Group loggingGroup = new Group(panel, SWT.NONE);
    loggingGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    loggingGroup.setText(I18N.msg("sierra.eclipse.teamserver.preference.page.group.logging"));

    f_showAbove = new RadioGroupFieldEditor(LocalTeamServerPreferencesUtility.SERVER_LOGGING_LEVEL,
        I18N.msg("sierra.eclipse.teamserver.preference.page.showAbove"), 1, new String[][] {
            { Level.SEVERE.toString(), Level.SEVERE.toString() }, { Level.WARNING.toString(), Level.WARNING.toString() },
            { Level.INFO.toString(), Level.INFO.toString() }, { Level.FINE.toString(), Level.FINE.toString() },
            { Level.FINER.toString(), Level.FINER.toString() }, { Level.FINEST.toString(), Level.FINEST.toString() } },
        loggingGroup);
    f_showAbove.setPage(this);
    f_showAbove.setPreferenceStore(EclipseUIUtility.getPreferences());
    f_showAbove.load();

    /*
     * Allow access to help via the F1 key.
     */
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.surelogic.sierra.client.eclipse.preferences-sierra");

    return panel;
  }

  void updateMBInLabel() {
    final int mb = f_serverMemoryMB.getScaleControl().getSelection();
    f_serverMemoryMB.setLabelText(I18N.msg(SERVER_MB_LABEL, SLUtility.toStringHumanWithCommas(mb)));
  }

  @Override
  protected void performDefaults() {
    f_serverMemoryMB.loadDefault();
    f_showAbove.loadDefault();
    super.performDefaults();

    updateMBInLabel();
  }

  @Override
  public boolean performOk() {
    f_serverMemoryMB.store();
    f_showAbove.store();
    return super.performOk();
  }
}