package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.*;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public class JavaProjectSelectionDialog extends Dialog {
  private final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
  private final IJavaModel javaModel = JavaCore.create(root);
  private final Image projectImage;
  private final List<IJavaProject> initialProjects;
  
	private Table f_projectTable;
  private final List<IJavaProject> f_projects;
  
	private final String label;

	public static List<IJavaProject> getProjects(final String label, final List<IJavaProject> projects) {
	  if (projects.isEmpty() || PreferenceConstants.alwaysAllowUserToSelectProjectsToScan()) {
	    final List<IJavaProject> returns = new ArrayList<IJavaProject>();
	    // Copied from AbstractScan
	    UIJob job = new UIJob("Put up test dialog") {
	      @Override
	      public IStatus runInUIThread(IProgressMonitor monitor) {
	        final Shell shell = 
	          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	        final JavaProjectSelectionDialog dialog = 
	          new JavaProjectSelectionDialog(shell, label, projects, returns);
	        
	        if (dialog.open() == Window.CANCEL) {
	          return Status.CANCEL_STATUS;
	        }
	        return Status.OK_STATUS;
	      }
	    };
	    /*
	    job.setSystem(true);
	    job.schedule();
	    try {
	      job.join();
	    } catch (InterruptedException e) {
	      // Nothing to do
	    } 
	    */
	    IStatus status = job.runInUIThread(null);
	    if (status == Status.CANCEL_STATUS) {
	      return Collections.emptyList();
	    }
      return returns;
	  }
	  return projects;
	}    
	
	private JavaProjectSelectionDialog(Shell parentShell, String label, 
	                                  List<IJavaProject> initial, List<IJavaProject> returns) {
		super(parentShell);
		this.label = label;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		projectImage = SLImages.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT);
		initialProjects = initial;
		f_projects = returns;
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(projectImage);
		newShell.setText("Select Java Projects");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1,
				1));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC)); 
		
		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		final Label l = new Label(entryPanel, SWT.WRAP);
		l.setText(label);

		f_projectTable = new Table(entryPanel, SWT.FULL_SELECTION | SWT.CHECK);
		f_projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		try {
      for (IJavaProject jp : javaModel.getJavaProjects()) {
      	TableItem item = new TableItem(f_projectTable, SWT.NONE);
      	item.setText(jp.getElementName());
      	item.setImage(projectImage);
      	item.setData(jp);
      	if (initialProjects.contains(jp)) {
          item.setChecked(true);          
          f_projects.add(jp);
      	}
      }
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

		addToEntryPanel(entryPanel);

		f_projectTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_projects.clear();
				for(TableItem item : f_projectTable.getItems()) {
				  if (item.getChecked()) {
				    f_projects.add((IJavaProject) item.getData());
				  }
				}				 
				setOKState();
			}
		});
    
		// add controls to composite as necessary
    final Button check = new Button(panel, SWT.CHECK);
    check.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
    check.setText("Always show this dialog in the future");
    check.setSelection(PreferenceConstants.alwaysAllowUserToSelectProjectsToScan());
    check.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        boolean show = !PreferenceConstants.alwaysAllowUserToSelectProjectsToScan();
        PreferenceConstants.setAlwaysAllowUserToSelectProjectsToScan(show);
        check.setSelection(show);
      }     
    });  
		return panel;
	}

	protected void addToEntryPanel(Composite entryPanel) {
		// Do nothing
	}

	@Override
	protected final Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private final void setOKState() {
		getButton(IDialogConstants.OK_ID).setEnabled(!f_projects.isEmpty());
	}
}
