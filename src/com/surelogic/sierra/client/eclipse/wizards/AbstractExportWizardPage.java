package com.surelogic.sierra.client.eclipse.wizards;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public abstract class AbstractExportWizardPage<T> extends WizardPage {
  protected CheckboxTableViewer f_TableViewer;
  protected final List<T> f_selections = new ArrayList<T>();
  protected Text f_exportFilenameText;
  private final String unitForT;
  
  protected AbstractExportWizardPage(String name, String label) {
    super(name);
    this.unitForT = label;
  }

  protected final void setupControl(Composite parent, String title,
                                    IStructuredContentProvider content, 
                                    ILabelProvider labels, final Class<T> clazz,
                                    String defaultName) {
    initializeDialogUnits(parent);

    Composite workArea = new Composite(parent, SWT.NONE);
    setControl(workArea);

    workArea.setLayout(new GridLayout());
    workArea.setLayoutData(new GridData(GridData.FILL_BOTH
        | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    Label titel = new Label(workArea, SWT.NONE);
    titel.setText(title);

    Composite listComposite = new Composite(workArea, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginWidth = 0;
    layout.makeColumnsEqualWidth = false;
    listComposite.setLayout(layout);

    listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

    Table table = new Table(listComposite, SWT.CHECK | SWT.BORDER
        | SWT.V_SCROLL | SWT.H_SCROLL);
    f_TableViewer = new CheckboxTableViewer(table);
    table.setLayout(new TableLayout());
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    data.heightHint = 300;
    table.setLayoutData(data);
    f_TableViewer.setContentProvider(content);
    f_TableViewer.setLabelProvider(labels);
    f_TableViewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        if (clazz.isInstance(event.getElement())) {
          @SuppressWarnings("unchecked")
          T holder = (T) event.getElement();
          if (event.getChecked()) {
            f_selections.add(holder);

          } else {
            f_selections.remove(holder);
          }
          updateEnablement();
        }
      }
    });

    initializeTable();
    createSelectionButtons(listComposite);
    createTextFields(workArea, defaultName);
    setControl(workArea);
    Dialog.applyDialogFont(parent);
  }
  
  protected final void createSelectionButtons(Composite composite) {

    Composite buttonsComposite = new Composite(composite, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    buttonsComposite.setLayout(layout);

    buttonsComposite.setLayoutData(new GridData(
        GridData.VERTICAL_ALIGN_BEGINNING));

    Button selectAll = new Button(buttonsComposite, SWT.PUSH);
    selectAll.setText("Select All");
    selectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (int i = 0; i < f_TableViewer.getTable().getItemCount(); i++) {
          if (f_TableViewer.getElementAt(i) instanceof ConnectedServer) {
            @SuppressWarnings("unchecked")
            T holder = (T) f_TableViewer
                .getElementAt(i);
            f_selections.add(holder);
          }
        }
        f_TableViewer.setAllChecked(true);
        updateEnablement();
      }
    });
    setButtonLayoutData(selectAll);

    Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
    deselectAll.setText("Deselect All");
    deselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        f_selections.clear();
        f_TableViewer.setAllChecked(false);
        updateEnablement();
      }
    });
    setButtonLayoutData(deselectAll);
  }
  
  protected final void createTextFields(Composite composite, String defaultName) {
  	// Server file name
  	Composite containerGroup = new Composite(composite, SWT.NONE);
  	GridLayout layout = new GridLayout();
  	layout.numColumns = 3;
  	containerGroup.setLayout(layout);
  	containerGroup.setLayoutData(new GridData(
  			GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
  
  	// Label
  	Label buildfilenameLabel = new Label(containerGroup, SWT.NONE);
  	buildfilenameLabel.setText("Export file:");
  
  	f_exportFilenameText = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
  	f_exportFilenameText.setText(System.getProperty("user.home")
  			+ System.getProperty("file.separator")
  			+ defaultName);
  	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
  			| GridData.GRAB_HORIZONTAL);
  	f_exportFilenameText.setLayoutData(data);
  
  	final Button browseButton = new Button(containerGroup, SWT.PUSH);
  	browseButton.setText("Browse...");
  	browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
  			false));
  	browseButton.addListener(SWT.Selection, new Listener() {
  		private FileDialog fd;
  
  		public void handleEvent(Event event) {
  			if (fd == null) {
  				fd = new FileDialog(getShell(), SWT.SAVE);
  				fd.setText("Destination File");
  				fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
  				fd.setFilterNames(new String[] { "XML Files (*.xml)",
  						"All Files (*.*)" });
  			}
  			final String fileName = f_exportFilenameText.getText();
  			int i = fileName.lastIndexOf(System
  					.getProperty("file.separator"));
  			if (i != -1) {
  				final String path = fileName.substring(0, i);
  				fd.setFilterPath(path);
  				if (i + 1 < fileName.length()) {
  					final String file = fileName.substring(i + 1);
  					fd.setFileName(file);
  				}
  			}
  			final String selectedFilename = fd.open();
  			if (selectedFilename != null) {
  				f_exportFilenameText.setText(selectedFilename);
  			}
  		}
  	});
  
  	ModifyListener listener = new ModifyListener() {
  		public void modifyText(ModifyEvent e) {
  			updateEnablement();
  		}
  	};
  	f_exportFilenameText.addModifyListener(listener);
  }
  
  protected final void updateEnablement() {
    boolean complete = true;

    // TODO: Implement file name check. Filenames with invalid characters
    // are still permitted

    if (f_selections.isEmpty()) {
      setErrorMessage("At least one "+unitForT+" must be selected");
      complete = false;
    }
    if (f_exportFilenameText.getText().length() == 0) {
      setErrorMessage("Export file name is required");
      complete = false;
    }
    if (complete) {
      setErrorMessage(null);
    }
    setPageComplete(complete);
  }
  
  @Override
  public final void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      f_TableViewer.getTable().setFocus();
    }
  }
  
  protected abstract void initializeTable();
}
