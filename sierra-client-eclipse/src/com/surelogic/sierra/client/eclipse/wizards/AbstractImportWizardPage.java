package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractImportWizardPage extends WizardPage {
  protected Text f_importFilenameField;
  protected Button f_importFileBrowseButton;
  protected boolean f_invalidFile = true;
  
  protected AbstractImportWizardPage(String name) {
    super(name);
  }
  
  protected void createImportFileGroup(Composite parent) {
    // import file selection group
    Composite importFileSelectionGroup = new Composite(parent, SWT.NONE);
    
    Label dest = new Label(importFileSelectionGroup, SWT.NONE);
    dest.setText("From file:");
    
    // import filename entry field
    f_importFilenameField = new Text(importFileSelectionGroup, SWT.SINGLE
        | SWT.BORDER);
    
    // import file browse button
    f_importFileBrowseButton = new Button(importFileSelectionGroup,
        SWT.PUSH);
    f_importFileBrowseButton.setText("Browse");
    f_importFileBrowseButton.addListener(SWT.Selection, new Listener() {
      private FileDialog fd;

      public void handleEvent(Event event) {
        if (fd == null) {
          fd = new FileDialog(getShell(), SWT.OPEN);
          fd.setText("Destination File");
          fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
          fd.setFilterNames(new String[] { "XML Files (*.xml)",
              "All Files (*.*)" });
        }

        final String selectedFilename = fd.open();
        if (selectedFilename != null) {
          f_importFilenameField.setText(selectedFilename);
        }
      }
    });

    new Label(parent, SWT.NONE); // vertical spacer

    ModifyListener listener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        File holder = new File(f_importFilenameField.getText());

        if (holder.exists() && !holder.isDirectory()) {
          updateState(holder);
        }
        updateEnablement();
      }
    };

    f_importFilenameField.addModifyListener(listener);
    
    layoutImportFileGroup(importFileSelectionGroup);
  }
  
  protected abstract void layoutImportFileGroup(Composite importFileSelectionGroup);

  protected void updateState(File holder) {
    f_invalidFile = false;
  }
  
  protected final void updateEnablement() {
    boolean complete = true;

    // TODO: Implement file name check. Filenames with invalid characters
    // are still permitted

    if (f_invalidFile) {
      setErrorMessage("Invalid file");
      complete = false;
    }
    if (f_importFilenameField.getText().length() == 0) {
      setErrorMessage("Import file name is required");
      complete = false;
    }

    if (!areCustomFieldsComplete()) {
      complete = false;
    }
    
    if (complete) {
      setErrorMessage(null);
    }

    setPageComplete(complete);
  }
  
  protected boolean areCustomFieldsComplete() {
    return true;
  }
}
