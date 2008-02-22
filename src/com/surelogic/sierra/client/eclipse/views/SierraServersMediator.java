package com.surelogic.sierra.client.eclipse.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.*;
import com.surelogic.sierra.client.eclipse.dialogs.ConnectProjectsDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
import com.surelogic.sierra.client.eclipse.jobs.*;
import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class SierraServersMediator implements ISierraServerObserver {

	final Table f_serverList;
	final ToolItem f_newServer;
	final ToolItem f_duplicateServer;
	final ToolItem f_deleteServer;
	final MenuItem f_newServerItem;
	final MenuItem f_duplicateServerItem;
	final MenuItem f_deleteServerItem;
	final MenuItem f_scanAllConnectedProjects;
	final MenuItem f_rescanAllConnectedProjects;
	final MenuItem f_synchAllConnectedProjects;
	final MenuItem f_sendResultFilters;
	final MenuItem f_getResultFilters;
	final MenuItem f_serverPropertiesItem;
	final Button f_openInBrowser;
	final Group f_infoGroup;
	final Label f_serverURL;
	final Table f_projectList;
	final MenuItem f_connectProjectItem;
	final MenuItem f_disconnectProjectItem;

	private class ServerActionListener implements Listener {
		private final String msgIfNoServer;

		ServerActionListener() {
			this(null);
		}

		ServerActionListener(String msg) {
			msgIfNoServer = msg;
		}

		public void handleEvent(Event event) {
			SierraServer server = f_manager.getFocus();
			if (server != null) {
				handleEventOnServer(server);
			} else {
				handleEventWithoutServer();
			}
		}

		protected void handleEventOnServer(SierraServer server) {
			// Do nothing
		}

		protected void handleEventWithoutServer() {
			if (msgIfNoServer != null) {
				SLLogger.getLogger().warning(msgIfNoServer);
			}
		}
	}

	private abstract class ServerProjectActionListener extends
			ServerActionListener {
		ServerProjectActionListener(String msg) {
			super(msg);
		}

		@Override
		protected final void handleEventOnServer(SierraServer server) {
			final SierraServerManager manager = server.getManager();
			final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
				public void run(String nullName, SierraServer server,
						Shell shell) {
					start(server);
					for (String projectName : manager
							.getProjectsConnectedTo(server)) {
						if (manager.isConnected(projectName)) {
							runForServerProject(server, projectName);
						}
					}
					finish(server);
				}
			};
			final Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell();
			ServerAuthenticationDialog.promptPasswordIfNecessary(null, server,
					shell, serverAction);
		}

		protected void start(SierraServer server) {
			// Do nothing
		}

		protected abstract void runForServerProject(SierraServer server,
				String projectName);

		protected void finish(SierraServer server) {
			// Do nothing
		}
	}

	private abstract class IJavaProjectsActionListener extends
			ServerProjectActionListener {
		final List<IJavaProject> projects = new ArrayList<IJavaProject>();
		IJavaProject[] allProjects;

		IJavaProjectsActionListener(String msg) {
			super(msg);
		}

		@Override
		protected final void start(SierraServer server) {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
					.getRoot();
			final IJavaModel javaModel = JavaCore.create(root);
			try {
				allProjects = javaModel.getJavaProjects();
			} catch (JavaModelException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		protected final void runForServerProject(SierraServer server,
				String projectName) {
			if (projectName == null) {
				SLLogger.getLogger().warning(
						"Null project for server " + server.getLabel());
				return;
			}
			for (IJavaProject p : allProjects) {
				if (p.getElementName().equals(projectName)) {
					projects.add(p);
					return;
				}
			}
			SLLogger.getLogger().warning(
					"Could not find project: " + projectName);
		}
	}

	final Listener f_newServerAction = new Listener() {
		public void handleEvent(Event event) {
			ServerLocationDialog.newServer(f_serverList.getShell());
		}
	};

	final Listener f_duplicateServerAction = new Listener() {
		public void handleEvent(Event event) {
			f_manager.duplicate();
		}
	};

	final Listener f_deleteServerAction = new ServerActionListener() {
		@Override
		protected void handleEventOnServer(SierraServer server) {
			final List<String> projectNames = f_manager
					.getProjectsConnectedTo(server);
			final String serverName = server.getLabel();
			final String msg;
			if (projectNames.isEmpty()) {
				msg = I18N
						.msg("sierra.eclipse.serverDeleteWarning", serverName);
			} else {
				msg = I18N.msg("sierra.eclipse.serverDeleteWarningConnected",
						serverName, serverName, serverName);
			}
			if (MessageDialog.openConfirm(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Confirm Sierra Server Deletion", msg)) {
				f_manager.delete(server);
				if (!projectNames.isEmpty()) {
					final DeleteProjectDataJob deleteProjectJob = new DeleteProjectDataJob(
							projectNames);
					deleteProjectJob.runJob();
				}
			}
		}
	};

	final Listener f_openInBrowserAction = new ServerActionListener(
			"Edit server pressed with no server focus.") {
		@Override
		protected void handleEventOnServer(SierraServer server) {
			openInBrowser(server);
		}
	};

	final Listener f_projectListAction = new Listener() {
		public void handleEvent(Event event) {
			/*
			 * Determine the server label that has been selected and tell the
			 * model that it is the focus.
			 */
			final TableItem[] sa = f_projectList.getSelection();
			f_disconnectProjectItem.setEnabled(sa.length > 0);
		}
	};

	final SierraServerManager f_manager = SierraServerManager.getInstance();

	public SierraServersMediator(Table serverList, ToolItem newServer,
			ToolItem duplicateServer, ToolItem deleteServer,
			MenuItem newServerItem, MenuItem duplicateServerItem,
			MenuItem deleteServerItem, MenuItem scanAllConnectedProjects,
			MenuItem rescanAllConnectedProjects,
			MenuItem synchAllConnectedProjects, MenuItem sendResultFilters,
			MenuItem getResultFilters, MenuItem serverPropertiesItem,
			Button openInBrowser, Group infoGroup, Label serverURL,
			Table projectList, MenuItem connectProjectItem,
			MenuItem disconnectProjectItem) {
		f_serverList = serverList;
		f_newServer = newServer;
		f_duplicateServer = duplicateServer;
		f_deleteServer = deleteServer;
		f_newServerItem = newServerItem;
		f_duplicateServerItem = duplicateServerItem;
		f_deleteServerItem = deleteServerItem;
		f_scanAllConnectedProjects = scanAllConnectedProjects;
		f_rescanAllConnectedProjects = rescanAllConnectedProjects;
		f_synchAllConnectedProjects = synchAllConnectedProjects;
		f_sendResultFilters = sendResultFilters;
		f_getResultFilters = getResultFilters;
		f_serverPropertiesItem = serverPropertiesItem;
		f_openInBrowser = openInBrowser;
		f_infoGroup = infoGroup;
		f_serverURL = serverURL;
		f_projectList = projectList;
		f_connectProjectItem = connectProjectItem;
		f_disconnectProjectItem = disconnectProjectItem;
	}

	public void init() {
		f_manager.addObserver(this);
		notify(f_manager);

		f_serverList.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				/*
				 * Determine the server label that has been selected and tell
				 * the model that it is the focus.
				 */
				final TableItem[] sa = f_serverList.getSelection();
				if (sa.length > 0) {
					final TableItem selection = sa[0];
					final String label = selection.getText();
					final SierraServer server = f_manager.getOrCreate(label);
					f_manager.setFocus(server);
				}
			}
		});

		f_serverList.addListener(SWT.MouseDoubleClick, f_openInBrowserAction);

		f_newServer.addListener(SWT.Selection, f_newServerAction);
		f_newServerItem.addListener(SWT.Selection, f_newServerAction);

		f_duplicateServer.addListener(SWT.Selection, f_duplicateServerAction);
		f_duplicateServerItem.addListener(SWT.Selection,
				f_duplicateServerAction);

		f_deleteServer.addListener(SWT.Selection, f_deleteServerAction);
		f_deleteServerItem.addListener(SWT.Selection, f_deleteServerAction);

		f_scanAllConnectedProjects
				.addListener(
						SWT.Selection,
						new IJavaProjectsActionListener(
								"Scan all connected projects pressed with no server focus.") {
							@Override
							protected void finish(SierraServer server) {
								new NewScan().scan(projects);
							}
						});

		f_rescanAllConnectedProjects
				.addListener(
						SWT.Selection,
						new IJavaProjectsActionListener(
								"Re-scan all connected projects pressed with no server focus.") {
							@Override
							protected void finish(SierraServer server) {
								new ScanChangedProjectsAction().run(projects);
							}
						});

		f_synchAllConnectedProjects
				.addListener(
						SWT.Selection,
						new ServerProjectActionListener(
								"Synchronize all connected projects pressed with no server focus.") {
							SynchronizeGroupJob joinJob = null;

							@Override
							protected void start(SierraServer server) {
								joinJob = new SynchronizeGroupJob(server);
							}

							@Override
							protected void runForServerProject(
									SierraServer server, String projectName) {
								final SynchronizeJob job = new SynchronizeJob(
										joinJob, projectName, server);
								job.schedule();
							}

							@Override
							protected void finish(SierraServer server) {
								joinJob.schedule();
								joinJob = null;
							}
						});

		f_sendResultFilters.addListener(SWT.Selection,
				new ServerActionListener(
						"Send scan filters pressed with no server focus.") {
					@Override
					protected void handleEventOnServer(SierraServer server) {
						final StringBuilder msg = new StringBuilder();
						msg
								.append("Do you want your local scan filters to become");
						msg
								.append(" the scan filters used by (and available from)");
						msg.append(" the Sierra server '");
						msg.append(server.getLabel());
						msg.append("'?");
						MessageDialog dialog = new MessageDialog(f_serverList
								.getShell(), "Send Scan Filters", null, msg
								.toString(), MessageDialog.QUESTION,
								new String[] { "Yes", "No" }, 0);
						if (dialog.open() == 0) {
							/*
							 * Yes was selected, so send the result filters to
							 * the server.
							 */
							final Job job = new SendGlobalResultFiltersJob(
									server);
							job.schedule();
						}
					}
				});

		f_getResultFilters.addListener(SWT.Selection, new ServerActionListener(
				"Get scan filters pressed with no server focus.") {
			@Override
			protected void handleEventOnServer(SierraServer server) {
				final StringBuilder msg = new StringBuilder();
				msg
						.append("Do you want overwrite your local scan filters with");
				msg.append(" the scan filters on");
				msg.append(" the Sierra server '");
				msg.append(server.getLabel());
				msg.append("'?");
				MessageDialog dialog = new MessageDialog(f_serverList
						.getShell(), "Get Scan Filters", null, msg.toString(),
						MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
				if (dialog.open() == 0) {
					/*
					 * Yes was selected, so get the result filters from the
					 * server.
					 */
					final Job job = new GetGlobalResultFiltersJob(server);
					job.schedule();
				}
			}
		});

		f_serverPropertiesItem.addListener(SWT.Selection,
				new ServerActionListener(
						"Edit server pressed with no server focus.") {
					@Override
					protected void handleEventOnServer(SierraServer server) {
						final ServerLocationDialog dialog = new ServerLocationDialog(
								f_serverList.getShell(), server,
								ServerLocationDialog.EDIT_TITLE);
						dialog.open();
					}
				});

		f_openInBrowser.addListener(SWT.Selection, f_openInBrowserAction);

		f_projectList.addListener(SWT.Selection, f_projectListAction);

		f_connectProjectItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ConnectProjectsDialog dialog = new ConnectProjectsDialog(
						f_serverList.getShell());
				dialog.open();
			}
		});

		f_disconnectProjectItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				List<String> projectNames = new ArrayList<String>();
				final TableItem[] si = f_projectList.getSelection();
				for (TableItem item : si) {
					projectNames.add(item.getText());
				}
				if (!projectNames.isEmpty()) {
					DeleteProjectDataJob.utility(projectNames, null, true);
				}
			}
		});
	}

	public void dispose() {
		// TODO
	}

	public void setFocus() {
		f_serverList.setFocus();
	}

	public void notify(SierraServerManager manager) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_serverList.isDisposed())
					return Status.OK_STATUS;
				f_serverList.setRedraw(false);
				String[] labels = f_manager.getLabels();
				TableItem[] items = f_serverList.getItems();
				if (!same(items, labels)) {
					for (TableItem item : items)
						item.dispose();
					for (String label : labels) {
						TableItem item = new TableItem(f_serverList, SWT.NONE);
						item.setText(label);
						item.setImage(SLImages
								.getImage(SLImages.IMG_SIERRA_SERVER));
					}
				}
				SierraServer server = f_manager.getFocus();
				final boolean focusServer = server != null;
				f_duplicateServer.setEnabled(focusServer);
				f_deleteServer.setEnabled(focusServer);
				f_duplicateServerItem.setEnabled(focusServer);
				f_deleteServerItem.setEnabled(focusServer);
				f_synchAllConnectedProjects.setEnabled(focusServer);
				f_sendResultFilters.setEnabled(focusServer);
				f_getResultFilters.setEnabled(focusServer);
				f_serverPropertiesItem.setEnabled(focusServer);
				f_openInBrowser.setEnabled(focusServer);
				f_infoGroup.setEnabled(focusServer);
				if (focusServer) {
					items = f_serverList.getItems();
					for (int i = 0; i < items.length; i++) {
						if (items[i].getText().equals(server.getLabel())) {
							f_serverList.select(i);
							break;
						}
					}
					f_infoGroup.setText(server.getLabel());
					f_serverURL.setText(server.toURLWithContextPath());
					items = f_projectList.getItems();
					for (TableItem item : items) {
						item.dispose();
					}
					for (String projectName : f_manager
							.getProjectsConnectedTo(server)) {
						TableItem item = new TableItem(f_projectList, SWT.NONE);
						item.setText(projectName);
						item
								.setImage(SLImages
										.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
					}
				} else {
					f_infoGroup.setText("");
					f_serverURL.setText("");
					items = f_projectList.getItems();
					for (TableItem item : items) {
						item.dispose();
					}
				}
				f_infoGroup.layout();
				f_projectListAction.handleEvent(null);
				f_serverList.setRedraw(true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static boolean same(TableItem[] items, String[] labels) {
		if (items.length != labels.length)
			return false;
		for (int i = 0; i < labels.length; i++) {
			boolean same = items[i].getText().equals(labels[i]);
			if (!same)
				return false;
		}
		return true;
	}

	private static void openInBrowser(SierraServer server) {
		final String name = "Sierra Server '" + server.getLabel() + "'";

		try {
			final IWebBrowser browser = PlatformUI.getWorkbench()
					.getBrowserSupport().createBrowser(
							IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.STATUS,
							server.getLabel(), name, name);
			final URL url = server.toAuthorizedURL();
			browser.openURL(url);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(26), e);
		}
	}
}
