package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public final class FilterServerSelectionDialog extends AbstractServerSelectionDialog {
  private final boolean sendFilters;
  
  public FilterServerSelectionDialog(Shell parentShell, boolean sendFilters) {
    super(parentShell, sendFilters ? "Select the server to send scan filters to:" : 
                                     "Select the server to get scan filters from:");
    this.sendFilters = sendFilters;
  }

  @Override
  protected void addToEntryPanel(Composite entryPanel) {
    final Label l = new Label(entryPanel, SWT.WRAP);
    final StringBuilder msg = new StringBuilder();
    if (sendFilters) {
      msg.append("Note that this will make your local scan filters to become");
      msg.append(" the scan filters used by (and available from)");
      msg.append(" the selected Sierra server");
    } else {
      msg.append("Note that this will overwrite your local scan filters with");
      msg.append(" the scan filters on");
      msg.append(" the selected Sierra server");
    }
    l.setText(msg.toString());
  }
}
