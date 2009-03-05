package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectAction;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoService;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;

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
		List<String> scannedProjects = disallowUnscannedProjects ? Projects
				.getInstance().getProjectNames() : null;
		Iterator<IJavaProject> it = projects.iterator();
		while (it.hasNext()) {
			final String projectName = it.next().getElementName();
			if (f_manager.isConnected(projectName)) {
				it.remove();
			}
			if (disallowUnscannedProjects
					&& !scannedProjects.contains(projectName)) {
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
		data.heightHint = 200;
		projectGroup.setLayoutData(data);
		projectGroup.setText("Unconnected Projects");
		projectGroup.setLayout(new FillLayout());

		final Table projectTable = new Table(projectGroup, SWT.CHECK);

		for (IJavaProject p : f_unconnectedProjects) {
			TableItem item = new TableItem(projectTable, SWT.NONE);
			item.setText(p.getElementName());
			item.setData(p);
			item.setImage(SLImages.getImage(CommonImages.IMG_PROJECT));
		}

		final Composite allNonePanel = new Composite(panel, SWT.NONE);
		allNonePanel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final GridLayout allNoneLayout = new GridLayout();
		allNoneLayout.numColumns = 2;
		allNoneLayout.makeColumnsEqualWidth = true;
		allNonePanel.setLayout(allNoneLayout);
		final Button allButton = new Button(allNonePanel, SWT.PUSH);
		allButton.setText("Select All");
		allButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		final Button noneButton = new Button(allNonePanel, SWT.PUSH);
		noneButton.setText("Deselect All");
		noneButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		f_mediator = new Mediator(allButton, noneButton, projectTable);
		f_mediator.init();

		return panel;
	}

	@Override
	protected final Control createContents(final Composite parent) {
		final Control contents = super.createContents(parent);
		if (f_mediator != null)
			f_mediator.setOKState();
		return contents;
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

	private class Mediator {

		private final Button f_allButton;

		private final Button f_noneButton;

		private final Table f_projectTable;

		Mediator(Button allButton, Button noneButton, Table projectTable) {
			f_allButton = allButton;
			f_noneButton = noneButton;
			f_projectTable = projectTable;
		}

		private void init() {
			f_allButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					setCheckedAll(true);
				}
			});
			f_noneButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					setCheckedAll(false);
				}
			});
			f_projectTable.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
					setOKState();
				}
			});
		}

		private final void setOKState() {
			/*
			 * Is anything checked?
			 */
			boolean isAnythingChecked = false;
			if (f_projectTable != null && !f_projectTable.isDisposed()) {
				for (final TableItem item : f_projectTable.getItems()) {
					if (item.getChecked()) {
						isAnythingChecked = true;
						break;
					}
				}
				/*
				 * Set the state of the OK button.
				 */
				getButton(IDialogConstants.OK_ID).setEnabled(isAnythingChecked);
			}
		}

		private void setCheckedAll(final boolean value) {
			for (TableItem item : f_projectTable.getItems()) {
				item.setChecked(value);
			}
			setOKState();
		}

		private void okPressed() {
			for (TableItem item : f_projectTable.getItems()) {
				if (!item.getChecked()) {
					f_unconnectedProjects.remove(item.getData());
				}
			}
			final List<IJavaProject> projects = new ArrayList<IJavaProject>(
					f_unconnectedProjects);
			final Job job = new Job("Connecting projects to "
					+ f_server.getName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ServerInfoService s = ServerInfoServiceClient
							.create(f_server.getLocation());
					ServerInfoReply reply = s
							.getServerInfo(new ServerInfoRequest());
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

					final UIJob syncJob = new SLUIJob() {
						@Override
						public IStatus runInUIThread(
								final IProgressMonitor monitor) {
							new SynchronizeProjectAction().run(projects);
							return Status.OK_STATUS;
						}
					};
					syncJob.schedule();
					return Status.OK_STATUS;
				}

			};
			job.schedule();
		}
	}

	private void showMessageDialog(final String title, final String msg) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				final Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				final MessageDialog dialog = new MessageDialog(shell, title,
						null, msg, MessageDialog.INFORMATION,
						new String[] { "OK" }, 0);
				dialog.open();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
