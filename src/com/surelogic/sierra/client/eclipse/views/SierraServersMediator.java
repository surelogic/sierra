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
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.ImageImageDescriptor;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.NewScan;
import com.surelogic.sierra.client.eclipse.actions.ScanChangedProjectsAction;
import com.surelogic.sierra.client.eclipse.dialogs.ConnectProjectsDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.jobs.GetGlobalResultFiltersJob;
import com.surelogic.sierra.client.eclipse.jobs.SendGlobalResultFiltersJob;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class SierraServersMediator 
implements ISierraServerObserver, IViewMediator {

	private final IViewCallback f_view;
	private final Tree f_statusTree;
	private final Menu f_serverListMenu;
	private final IAction f_newServerAction;
	private final IAction f_duplicateServerAction;
	private final IAction f_deleteServerAction;
	private final IAction f_openInBrowserAction;	
	private final MenuItem f_newServerItem;
	private final MenuItem f_duplicateServerItem;
	private final MenuItem f_deleteServerItem;
	private final MenuItem f_serverConnectItem;
	private final MenuItem f_scanAllConnectedProjects;
	private final MenuItem f_rescanAllConnectedProjects;
	private final MenuItem f_synchAllConnectedProjects;
	private final MenuItem f_sendResultFilters;
	private final MenuItem f_getResultFilters;
	private final MenuItem f_serverPropertiesItem;
	private final Menu f_projectListMenu;
	private final MenuItem f_projectConnectItem;
	private final MenuItem f_scanProjectItem;
	private final MenuItem f_rescanProjectItem;
	private final MenuItem f_disconnectProjectItem;

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
			ServerActionListener {
		IJavaProjectsActionListener(String msg) {
			super(msg);
		}

		@Override
		protected final void handleEventOnServer(SierraServer server) {
			final SierraServerManager manager = server.getManager();
			run(server, manager.getProjectsConnectedTo(server));
		}

		protected abstract void run(SierraServer server,
				List<String> projectNames);
	}

	private void scan(String label, List<String> projectNames, boolean rescan) {
		if (projectNames.isEmpty()) {
			return;
		}
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IJavaModel javaModel = JavaCore.create(root);
		try {
			final IJavaProject[] allProjects = javaModel.getJavaProjects();
			final List<IJavaProject> projectsToScan = new ArrayList<IJavaProject>();
			for (String projectName : projectNames) {
				if (projectName == null) {
					SLLogger.getLogger().warning("Null project for " + label);
					continue;
				}
				for (IJavaProject p : allProjects) {
					if (p.getElementName().equals(projectName)) {
						projectsToScan.add(p);
						break;
					}
				}
				SLLogger.getLogger().warning(
						"Could not find project: " + projectName);
			}
			if (rescan) {
				new ScanChangedProjectsAction().run(projectsToScan);
			} else {
				new NewScan().scan(projectsToScan);
			}
		} catch (JavaModelException e) {
			throw new IllegalStateException(e);
		}
	}

	private final SierraServerManager f_manager = SierraServerManager
			.getInstance();

	/*
	private final Listener f_projectListAction = new Listener() {
		public void handleEvent(Event event) {
			final TableItem[] sa = f_projectList.getSelection();
			final boolean enabled = sa.length > 0;
			f_scanProjectItem.setEnabled(enabled);
			f_rescanProjectItem.setEnabled(enabled);
			f_disconnectProjectItem.setEnabled(enabled);
		}
	};
    */

	private static void setImageDescriptor(IAction a, String key) {
		Image img = SLImages.getWorkbenchImage(key);
		a.setImageDescriptor(new ImageImageDescriptor(img));
	}
	
	public SierraServersMediator(SierraServersView view,
			Tree statusTree, Menu serverListMenu,
			MenuItem newServerItem, MenuItem duplicateServerItem,
			MenuItem deleteServerItem, MenuItem serverConnectItem,
			MenuItem scanAllConnectedProjects,
			MenuItem rescanAllConnectedProjects,
			MenuItem synchAllConnectedProjects, MenuItem sendResultFilters,
			MenuItem getResultFilters, MenuItem serverPropertiesItem,
			Menu projectListMenu, MenuItem projectConnectItem,
			MenuItem scanProjectItem, MenuItem rescanProjectItem,
			MenuItem disconnectProjectItem) {
		f_view = view;
		f_statusTree = statusTree;
		f_serverListMenu = serverListMenu;
		f_newServerAction = new Action("New team server location", 
				                 IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				ServerLocationDialog.newServer(f_statusTree.getShell());			
			}
		};		
		setImageDescriptor(f_newServerAction, ISharedImages.IMG_TOOL_NEW_WIZARD);

		f_duplicateServerAction = new Action("Duplicates the selected team server location", 
				                       IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				//f_mediator.setSortByServer(isChecked());				
			}
		}; 
		setImageDescriptor(f_duplicateServerAction, ISharedImages.IMG_TOOL_COPY);
		
		f_deleteServerAction = new Action("Deletes the selected team server location", 
				                    IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				//f_mediator.setSortByServer(isChecked());				
			}
		};
		setImageDescriptor(f_deleteServerAction, ISharedImages.IMG_TOOL_DELETE);
		
		f_openInBrowserAction = new Action("Open the selected team server in a Web browser", 
				                     IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				//f_mediator.setSortByServer(isChecked());				
			}
		};
		f_openInBrowserAction.setText("Browse");
		
		f_newServerItem = newServerItem;
		f_duplicateServerItem = duplicateServerItem;
		f_deleteServerItem = deleteServerItem;
		f_serverConnectItem = serverConnectItem;
		f_scanAllConnectedProjects = scanAllConnectedProjects;
		f_rescanAllConnectedProjects = rescanAllConnectedProjects;
		f_synchAllConnectedProjects = synchAllConnectedProjects;
		f_sendResultFilters = sendResultFilters;
		f_getResultFilters = getResultFilters;
		f_serverPropertiesItem = serverPropertiesItem;
		f_projectListMenu = projectListMenu;
		f_projectConnectItem = projectConnectItem;
		f_scanProjectItem = scanProjectItem;
		f_rescanProjectItem = rescanProjectItem;
		f_disconnectProjectItem = disconnectProjectItem;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-team-servers";
	}

	public String getNoDataId() {
		return "sierra.eclipse.noDataSierraServers";
	}

	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				ServerLocationDialog.newServer(f_statusTree.getShell());
			}
		};
	}
	
	public void init() {
		f_manager.addObserver(this);
		notify(f_manager);

//		f_serverList.addListener(SWT.Selection, new Listener() {
//			public void handleEvent(Event event) {
//				/*
//				 * Determine the server label that has been selected and tell
//				 * the model that it is the focus.
//				 */
//				final TableItem[] sa = f_serverList.getSelection();
//				if (sa.length > 0) {
//					final TableItem selection = sa[0];
//					final String label = selection.getText();
//					final SierraServer server = f_manager.getOrCreate(label);
//					f_manager.setFocus(server);
//				}
//			}
//		});

		final Listener newServerAction = getNoDataListener();

		final Listener duplicateServerAction = new Listener() {
			public void handleEvent(Event event) {
				f_manager.duplicate();
			}
		};

		final Listener deleteServerAction = new IJavaProjectsActionListener(
				"Delete server pressed with no server focus.") {
			@Override
			protected void run(SierraServer server, List<String> projectNames) {
				final String serverName = server.getLabel();
				final String msg;
				if (projectNames.isEmpty()) {
					msg = I18N.msg("sierra.eclipse.serverDeleteWarning",
							serverName);
				} else {
					msg = I18N.msg(
							"sierra.eclipse.serverDeleteWarningConnected",
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

		final Listener openInBrowserAction = new ServerActionListener(
				"Edit server pressed with no server focus.") {
			@Override
			protected void handleEventOnServer(SierraServer server) {
				openInBrowser(server);
			}
		};

		//f_serverList.addListener(SWT.MouseDoubleClick, openInBrowserAction);

		//f_newServerAction.
		//f_newServer.addListener(SWT.Selection, newServerAction);
		f_newServerItem.addListener(SWT.Selection, newServerAction);

		//f_duplicateServer.addListener(SWT.Selection, duplicateServerAction);
		f_duplicateServerItem.addListener(SWT.Selection, duplicateServerAction);

		//f_deleteServer.addListener(SWT.Selection, deleteServerAction);
		f_deleteServerItem.addListener(SWT.Selection, deleteServerAction);

		final Listener connectAction = new Listener() {
			public void handleEvent(Event event) {
				ConnectProjectsDialog dialog = new ConnectProjectsDialog(
						f_statusTree.getShell());
				dialog.open();
			}
		};

		f_serverConnectItem.addListener(SWT.Selection, connectAction);

		f_scanAllConnectedProjects
				.addListener(
						SWT.Selection,
						new IJavaProjectsActionListener(
								"Scan all connected projects pressed with no server focus.") {
							@Override
							protected void run(SierraServer server,
									List<String> projectNames) {
								scan("scan of " + server.getLabel()
										+ "'s projects", projectNames, false);
							}
						});

		f_rescanAllConnectedProjects
				.addListener(
						SWT.Selection,
						new IJavaProjectsActionListener(
								"Re-Scan all connected projects pressed with no server focus.") {
							@Override
							protected void run(SierraServer server,
									List<String> projectNames) {
								scan("scan of " + server.getLabel()
										+ "'s projects", projectNames, true);
							}
						});

		f_synchAllConnectedProjects
				.addListener(
						SWT.Selection,
						new ServerProjectActionListener(
								"Synchronize all connected projects pressed with no server focus.") {
							ServerProjectGroupJob joinJob = null;

							@Override
							protected void start(SierraServer server) {
								joinJob = new ServerProjectGroupJob(server);
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
						MessageDialog dialog = new MessageDialog(f_statusTree
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
				MessageDialog dialog = new MessageDialog(f_statusTree
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
								f_statusTree.getShell(), server,
								ServerLocationDialog.EDIT_TITLE);
						dialog.open();
					}
				});

		//f_openInBrowser.addListener(SWT.Selection, openInBrowserAction);

		//f_projectList.addListener(SWT.Selection, f_projectListAction);

		f_projectConnectItem.addListener(SWT.Selection, connectAction);

		f_scanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<String> projectNames) {
						scan("scan", projectNames, false);
					}
				});
		f_rescanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<String> projectNames) {
						scan("re-scan", projectNames, false);
					}
				});
		f_disconnectProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<String> projectNames) {
						DeleteProjectDataJob.utility(projectNames, null, true);
					}
				});
	}

	private abstract class ProjectsActionListener implements Listener {
		public final void handleEvent(Event event) {
			/*
			List<String> projectNames = new ArrayList<String>();
			final TableItem[] si = f_projectList.getSelection();
			for (TableItem item : si) {
				projectNames.add(item.getText());
			}
			if (!projectNames.isEmpty()) {
				run(projectNames);
			}
			*/
		}

		protected abstract void run(List<String> projectNames);
	}

	public void dispose() {
		// TODO
	}

	public void setFocus() {
		f_statusTree.setFocus();
	}

	public void notify(SierraServerManager manager) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_statusTree.isDisposed())
					return Status.OK_STATUS;
				
				f_view.hasData(!f_manager.isEmpty());
				f_statusTree.setRedraw(false);
				/*
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
				*/

				final SierraServer server = f_manager.getFocus();
				createTreeItems(server);
				
				final boolean focusServer = server != null;
				f_duplicateServerAction.setEnabled(focusServer);
				f_deleteServerAction.setEnabled(focusServer);
				f_openInBrowserAction.setEnabled(focusServer);
				f_duplicateServerItem.setEnabled(focusServer);
				f_deleteServerItem.setEnabled(focusServer);
				f_serverConnectItem.setEnabled(focusServer);
				f_scanAllConnectedProjects.setEnabled(focusServer);
				f_rescanAllConnectedProjects.setEnabled(focusServer);
				f_synchAllConnectedProjects.setEnabled(focusServer);
				f_sendResultFilters.setEnabled(focusServer);
				f_getResultFilters.setEnabled(focusServer);
				f_serverPropertiesItem.setEnabled(focusServer);
				
				/*
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
				f_projectListAction.handleEvent(null);
				 */
				f_statusTree.setRedraw(true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void createTreeItems(SierraServer focus) {
		// TODO Auto-generated method stub		
	}

	/*
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
    */

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

	public void setSortByServer(boolean checked) {
		// TODO Auto-generated method stub
		
	}
}
