package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class ServerSelectionDialog extends AbstractServerSelectionDialog {
	private boolean f_useForAllUnconnectedProjects = false;

	public boolean useForAllUnconnectedProjects() {
		return f_useForAllUnconnectedProjects;
	}
	
	public void setUseForAllUnconnectedProjects(boolean value) {
		f_useForAllUnconnectedProjects = value;
	}

	public ServerSelectionDialog(Shell parentShell, String projectName) {
		super(parentShell, "Select the server to connect '" + projectName + "' to:");
	}

  @Override
  protected void addToEntryPanel(Composite entryPanel) {
    final Button useForAll = new Button(entryPanel, SWT.CHECK);
    useForAll.setText("Use this server for all unconnected projects");
    useForAll.setSelection(f_useForAllUnconnectedProjects);
    useForAll.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        f_useForAllUnconnectedProjects = useForAll.getSelection();
      }
    });
  }
}
