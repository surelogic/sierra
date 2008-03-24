package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
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
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingAudits;

public final class SierraServersMediator extends AbstractSierraViewMediator 
implements ISierraServerObserver {

    private List<ProjectStatus> projects = Collections.emptyList();
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
		super(view);
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
		asyncUpdateContents();
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
		ServerStatusSort sort;
		if (checked) {
			sort = ServerStatusSort.BY_SERVER;
		} else {
			sort = ServerStatusSort.BY_PROJECT;
		}
		if (sort != PreferenceConstants.getServerStatusSort()) {
			PreferenceConstants.setServerStatusSort(sort);
			changed(); // FIX do I need to reload from the database?
		}
	}
	
	/* Below this is the code to update the view from the database
	 */
	
	@Override
	public void changed() {
		asyncUpdateContents();
	}

	private void asyncUpdateContents() {
		final Job job = new DatabaseJob(
				"Updating project status") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
				} catch (Exception e) {
					final int errNo = 58; // FIX
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateContents() throws Exception {
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {			
			ClientFindingManager cfm = ClientFindingManager.getInstance(c);
			final List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
			for(String name : Projects.getInstance().getProjectNames()) {				
				// Check for new local audits
				List<FindingAudits> findings = cfm.getNewLocalAudits(name); 
				// FIX Check for new remote audits
				
				// FIX Check for a full scan (later than what's on the server?)
				final File scan = NewScan.getScanDocumentFile(name);
				ProjectStatus s = new ProjectStatus(name, scan, findings);
				projects.add(s);
			}
			asyncUpdateContentsForUI(new IViewUpdater() {
				public void updateContentsForUI() {
					updateContentsInUI(projects);
				}
			});
			c.commit();
		} catch (Exception e) {
			c.rollback();
			exc = e;
		} finally {
			try {
				c.close();
			} finally {
				if (exc != null) {
					throw exc;
				}
			}
		}
	}

	public void updateContentsInUI(final List<ProjectStatus> projects) {
		// No need to synchronize since only updated/viewed in UI thread?
		this.projects = projects;
		
		if (f_statusTree.isDisposed())
			return;
		
		f_statusTree.setRedraw(false);

		final SierraServer server = f_manager.getFocus();
		createTreeItems();
		
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
		f_projectListAction.handleEvent(null);
		 */
		f_statusTree.setRedraw(true);
	}
	
	protected void createTreeItems() {
		f_statusTree.removeAll();
				
		final boolean someServers = !f_manager.isEmpty();
		final boolean someProjects = !projects.isEmpty();
		final boolean somethingToSee = someServers || someProjects;
		f_statusTree.setVisible(somethingToSee);
		f_view.hasData(somethingToSee);
		
		if (!somethingToSee) {
			return;
		}
		else if (!someServers) {
			createProjectItems();
		}
		else if (!someProjects) {
			createServerItems();
		}
		else switch (PreferenceConstants.getServerStatusSort()) {
		case BY_PROJECT:
			createProjectItems();
			break;
		case BY_SERVER:
		default: 
			createServerItems();
		}
		f_statusTree.getParent().layout();
		for(TreeItem item : f_statusTree.getItems()) {
			item.setExpanded(true);
		}
	}
	
	private void createServerItems() {
		final SierraServer focus = f_manager.getFocus();
		TreeItem focused = null;
		for(String label : f_manager.getLabels()) {
			SierraServer server = f_manager.getServerByLabel(label);
			TreeItem item = new TreeItem(f_statusTree, SWT.NONE);
	
			item.setText(label+" ["+server.toURLWithContextPath()+']');
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
			item.setData(server);
			
			if (focus != null && label.equals(focus.getLabel())) {
				focused = item;
			}
			createProjectItems(item, server);
		}
		if (focused != null) {
			f_statusTree.setSelection(focused);
		}
	}
	
	private void createProjectItems(TreeItem parent, SierraServer server) {
		for(String projectName : f_manager.getProjectsConnectedTo(server)) {
			ProjectStatus s = null;
			for(ProjectStatus p : projects) {
				if (projectName.equals(p.name)) {
					s = p;
					break;
				}
			}
			if (s == null) {
				throw new IllegalStateException("No project: "+projectName);
			}
			initProjectItem(new TreeItem(parent, SWT.NONE), server, s);
		}
	}
	
	private void initProjectItem(final TreeItem root, final SierraServer server, 
			                     final ProjectStatus ps) { 
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
		if (server != null) {
			root.setText(ps.name+" ["+server.getLabel()+']');
		} else {
			root.setText(ps.name);
		}	
		root.setImage(SLImages
				.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		root.setData(ps);
		root.setExpanded(true);
		
		if (ps.scanDoc.exists()) {
			TreeItem scan = new TreeItem(root, SWT.NONE);
			Date modified = new Date(ps.scanDoc.lastModified());
			scan.setText("Last full scan on "+dateFormat.format(modified));
			scan.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SCAN));
			scan.setData(ps.scanDoc);
		}
		if (!ps.findings.isEmpty()) {
			TreeItem audits = new TreeItem(root, SWT.NONE);
			audits.setText(ps.numAudits+" audit(s) on "+ps.findings.size()+" finding(s)");
			audits.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP));
			audits.setData(ps.findings);
			
			if (ps.earliestAudit != null) {
				TreeItem earliest = new TreeItem(audits, SWT.NONE);
				earliest.setText("Earliest on "+dateFormat.format(ps.latestAudit));
			}
			if (ps.latestAudit != null && ps.earliestAudit != ps.latestAudit) {
				TreeItem latest = new TreeItem(audits, SWT.NONE);
				latest.setText("Latest on "+dateFormat.format(ps.latestAudit));
			}
		}
	}

	private void createProjectItems() {
		for (ProjectStatus ps : projects) {
			final TreeItem root = new TreeItem(f_statusTree, SWT.NONE);
			final SierraServer server = f_manager.getServer(ps.name);
			initProjectItem(root, server, ps);
		}
	}
}
