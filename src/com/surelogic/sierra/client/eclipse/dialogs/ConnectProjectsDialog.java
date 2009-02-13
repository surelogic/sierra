package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectAction;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.*;

public final class ConnectProjectsDialog extends Dialog {
    private final boolean disallowUnscannedProjects = false;
	
	private final ConnectedServerManager f_manager = ConnectedServerManager
			.getInstance();

	private final ConnectedServer f_server = f_manager.getFocus();

	private final List<IJavaProject> f_unconnectedProjects;

	private Mediator f_mediator = null;

	public ConnectProjectsDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (f_server == null)
			throw new IllegalStateException(
					"server of focus must be non-null (bug)");
		List<IJavaProject> projects = JDTUtility.getJavaProjects();
		List<String> scannedProjects = disallowUnscannedProjects ? Projects.getInstance().getProjectNames() : null;
		Iterator<IJavaProject> it = projects.iterator();
		while (it.hasNext()) {
			final String projectName = it.next().getElementName();
			if (f_manager.isConnected(projectName)) {
				it.remove();
			}
			if (disallowUnscannedProjects && !scannedProjects.contains(projectName)) {
				it.remove();
			}
		}
		Collections.sort(projects, new Comparator<IJavaProject>() {
			public int compare(IJavaProject o1, IJavaProject o2) {				
				return o1.getElementName().compareTo(o2.getElementName());
			}
			
		});
		f_unconnectedProjects = projects;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label l = new Label(panel, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		l.setLayoutData(data);
		l.setText("Select projects to connect the Sierra server '"
				+ f_server.getName() + "':");

		final Group projectGroup = new Group(panel, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 300;
		projectGroup.setLayoutData(data);
		projectGroup.setText("Unconnected Projects");
		projectGroup.setLayout(new FillLayout());

		final Table projectList = new Table(projectGroup, SWT.CHECK);

		for (IJavaProject p : f_unconnectedProjects) {
			TableItem item = new TableItem(projectList, SWT.NONE);
			item.setText(p.getElementName());
			item.setData(p);
			item.setImage(SLImages.getImage(CommonImages.IMG_PROJECT));
		}

		final Button exportAllToggle = new Button(panel, SWT.CHECK);
		exportAllToggle.setText("Connect all projects to '"
				+ f_server.getName() + "'");
		exportAllToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		
		final Button syncToggle = new Button(panel, SWT.CHECK);
		syncToggle.setText("Synchronize newly connected projects on finish");
		syncToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		syncToggle.setSelection(true);

		f_mediator = new Mediator(exportAllToggle, syncToggle, projectGroup, projectList);
		f_mediator.init();

		return panel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Connect Projects");
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
	}

	@Override
	protected void okPressed() {
		if (f_mediator != null)
			f_mediator.okPressed();
		super.okPressed();
	}

	public void setOKEnabled(boolean enabled) {
		Button ok = getButton(IDialogConstants.OK_ID);
		ok.setEnabled(enabled);
	}

	private class Mediator {

		private final Button f_exportAllToggle;

		private final Button f_syncToggle;
		
		private final Group f_projectGroup;

		private final Table f_queryTable;

		private boolean f_connectAll;

		Mediator(Button exportAllToggle, Button syncToggle, Group projectGroup, Table queryTable) {
			f_exportAllToggle = exportAllToggle;
			f_syncToggle = syncToggle;
			f_connectAll = f_exportAllToggle.getSelection();
			f_projectGroup = projectGroup;
			f_queryTable = queryTable;
			setDialogState();
		}

		void init() {
			f_exportAllToggle.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					f_connectAll = f_exportAllToggle.getSelection();
					if (f_connectAll) {
						// FIX what about unselect all?
						for (TableItem item : f_queryTable.getItems()) {
							item.setChecked(true);
						}
					}
					setDialogState();
				}
			});
		}

		private void setDialogState() {
			f_projectGroup.setEnabled(!f_connectAll);
			f_queryTable.setEnabled(!f_connectAll);
		}

		void okPressed() {
			if (!f_connectAll) {
				for (TableItem item : f_queryTable.getItems()) {
					if (!item.getChecked()) {
						f_unconnectedProjects.remove(item.getData());
					}
				}
			}
			final List<IJavaProject> projects = new ArrayList<IJavaProject>(f_unconnectedProjects);
			final boolean sync = f_syncToggle.getSelection();
			final Job job = new Job("Connecting projects") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ServerInfoService s = ServerInfoServiceClient.create(f_server.getLocation());
					ServerInfoReply reply = s.getServerInfo(new ServerInfoRequest());
					if (reply == null || reply.getUid() == null) {
						showMessageDialog("Bad response", 
								          "The server does not seem to be responding properly.");
						return Status.CANCEL_STATUS;
					}
					if (!reply.getUid().equals(f_server.getUuid())) {
						showMessageDialog("Server mismatch",
								          "The project(s) could not be connected, because the server has changed.");
						return Status.CANCEL_STATUS;
					}
					for (IJavaProject p : projects) {
						f_manager.connect(p.getElementName(), f_server);
					}
					
					if (sync) {
						final UIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(final IProgressMonitor monitor) {
								new SynchronizeProjectAction().run(projects);
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
					return Status.OK_STATUS;
				}
				
			};
			job.schedule();
		}
	}
	
	void showMessageDialog(final String title, final String msg) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final MessageDialog dialog = new MessageDialog(shell,
						title, null, msg,
						MessageDialog.INFORMATION,
						new String[] { "OK" }, 0);
				dialog.open();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
