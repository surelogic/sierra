package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.ImageImageDescriptor;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.WorkspaceUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.*;
import com.surelogic.sierra.client.eclipse.dialogs.*;
import com.surelogic.sierra.client.eclipse.jobs.AbstractServerProjectJob;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.jobs.GetGlobalResultFiltersJob;
import com.surelogic.sierra.client.eclipse.jobs.SendGlobalResultFiltersJob;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerInteractionSetting;
import com.surelogic.sierra.jdbc.EmptyProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingAudits;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.message.SyncTrailResponse;

public final class SierraServersMediator extends AbstractSierraViewMediator 
implements ISierraServerObserver, IProjectsObserver {
	private static final String NO_SERVER_DATA = "Needs to grab from server";
	
	/**
	 * This should only be changed in the UI thread
	 */
    private List<ProjectStatus> projects = Collections.emptyList();
    
	/**
	 * A map from a project to the server response
	 * 
	 * Protected by itself
	 * This should only be accessed in a database job 
	 * (possibly from multiple threads)
	 */
    private final Map<String,List<SyncTrailResponse>> responseMap = 
    	new HashMap<String,List<SyncTrailResponse>>();   
    
    private final AtomicLong lastServerUpdateTime = 
    	new AtomicLong(System.currentTimeMillis());
    
    private final AtomicReference<ServerProjectGroupJob> lastSyncGroup =
        new AtomicReference<ServerProjectGroupJob>();
    
    private final AtomicReference<Job> lastUpdateJob = 
    	new AtomicReference<Job>();
    
	private final Tree f_statusTree;
	private final Menu f_contextMenu;
	private final ActionListener f_serverSyncAction;
	private final ActionListener f_serverUpdateAction;
	private final ActionListener f_newServerAction;
	private final ActionListener f_duplicateServerAction;
	private final ActionListener f_deleteServerAction;
	private final ActionListener f_openInBrowserAction;	
	private final MenuItem f_newServerItem;
	private final MenuItem f_browseServerItem;
	private final MenuItem f_duplicateServerItem;
	private final MenuItem f_deleteServerItem;
	private final MenuItem f_serverConnectItem;
	private final MenuItem f_synchConnectedProjects;
	private final MenuItem f_sendResultFilters;
	private final MenuItem f_getResultFilters;
	private final MenuItem f_serverPropertiesItem;
	private final MenuItem f_scanProjectItem;
	private final MenuItem f_rescanProjectItem;
	private final MenuItem f_publishScansItem;
	private final MenuItem f_disconnectProjectItem;

	private abstract class ActionListener extends Action implements Listener {
		ActionListener(String text, String tooltip) {
			super(text, IAction.AS_PUSH_BUTTON);
			this.setToolTipText(tooltip);
			
		}
		ActionListener(Image image, String tooltip) {
			this(tooltip, tooltip);
			this.setImageDescriptor(new ImageImageDescriptor(image));			
		}
		
		public final void handleEvent(Event event) {
			run();
		}
		
		@Override
		public abstract void run();		
	}
	
	private class ServerActionListener extends ActionListener {
		private final String msgIfNoServer;

		ServerActionListener(String text, String tooltip, String msg) {
			super(text, tooltip);
			msgIfNoServer = msg;
		}
		
		ServerActionListener(Image image, String tooltip, String msg) {
			super(image, tooltip);
			msgIfNoServer = msg;
		}
		
		ServerActionListener(String msg) {
			super("", "");
			msgIfNoServer = msg;
		}

		@Override
		public final void run() {
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

	private abstract class IJavaProjectsActionListener extends
			ServerActionListener {
		IJavaProjectsActionListener(String msg) {
			super(msg);
		} 
		IJavaProjectsActionListener(Image image, String tooltip, String msg) {
			super(image, tooltip, msg);
		}
		
		@Override
		protected final void handleEventOnServer(SierraServer server) {
			final SierraServerManager manager = server.getManager();
			run(server, manager.getProjectsConnectedTo(server));
		}

		protected abstract void run(SierraServer server,
				List<String> projectNames);
	}

	private final SierraServerManager f_manager = SierraServerManager
			.getInstance();
	
	public SierraServersMediator(SierraServersView view,
			Tree statusTree, Menu contextMenu,
			MenuItem newServerItem, MenuItem browseServerItem,
			MenuItem duplicateServerItem,
			MenuItem deleteServerItem, MenuItem serverConnectItem,
			MenuItem synchConnectedProjects, MenuItem sendResultFilters,
			MenuItem getResultFilters, MenuItem serverPropertiesItem,
			MenuItem scanProjectItem, MenuItem rescanProjectItem,
			MenuItem publishScansItem, MenuItem disconnectProjectItem) {
		super(view);
		f_statusTree = statusTree;
		f_contextMenu = contextMenu;
		
		f_serverUpdateAction = 
			new ActionListener(SLImages.getImage(SLImages.IMG_SIERRA_SERVER),
	                           "Get latest server information") {
			@Override
			public void run() {
				asyncUpdateServerInfo();		
			}
		};		
		view.addToActionBar(f_serverUpdateAction);
		f_serverSyncAction =
			new ActionListener(SLImages.getImage(SLImages.IMG_SIERRA_SYNC),
            "Synchronize Connected Projects") {
			@Override
			public void run() {
				asyncSyncWithServer();		
			}
		};		
		view.addToActionBar(f_serverSyncAction);
		view.addToActionBar(new Separator());
		
		f_newServerAction = 
			new ActionListener(SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD),
					           "New team server location") {
			@Override
			public void run() {
				ServerLocationDialog.newServer(f_statusTree.getShell());			
			}
		};		
		view.addToActionBar(f_newServerAction);

		f_openInBrowserAction = 
			new ServerActionListener("Browse", 
				                     "Open the selected team server in a Web browser",
				                     "No server to browse") {
			@Override
			protected void handleEventOnServer(SierraServer server) {
				openInBrowser(server);		
			}
		};
		f_openInBrowserAction.setEnabled(false);
		view.addToActionBar(f_openInBrowserAction);
		
		f_duplicateServerAction = 
			new ServerActionListener(SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY),
					                 "Duplicates the selected team server location",
					                 "No server to duplicate") {
			@Override
			protected void handleEventOnServer(SierraServer server) {
				f_manager.duplicate();		
			}
		}; 
		f_duplicateServerAction.setEnabled(false);
		view.addToActionBar(f_duplicateServerAction);
		
		f_deleteServerAction = new IJavaProjectsActionListener(
					SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE),
					"Deletes the selected team server location",
					"No server to delete") {
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
		
		f_deleteServerAction.setEnabled(false);
		view.addToActionBar(f_deleteServerAction);
		
		f_newServerItem = newServerItem;
		f_browseServerItem = browseServerItem;
		f_duplicateServerItem = duplicateServerItem;
		f_deleteServerItem = deleteServerItem;
		f_serverConnectItem = serverConnectItem;
		f_synchConnectedProjects = synchConnectedProjects;
		f_sendResultFilters = sendResultFilters;
		f_getResultFilters = getResultFilters;
		f_serverPropertiesItem = serverPropertiesItem;
		f_scanProjectItem = scanProjectItem;
		f_rescanProjectItem = rescanProjectItem;
		f_publishScansItem = publishScansItem;
		f_disconnectProjectItem = disconnectProjectItem;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-team-servers";
	}

	public String getNoDataId() {
		return "sierra.eclipse.noDataSierraServers";
	}

	@Override
	public Listener getNoDataListener() {
		return f_newServerAction;
	}
	
	@Override
	public void init() {
		super.init();
		f_manager.addObserver(this);
		notify(f_manager);

		// f_serverList.addListener(SWT.MouseDoubleClick, openInBrowserAction);

		f_newServerItem.addListener(SWT.Selection, f_newServerAction);
		f_browseServerItem.addListener(SWT.Selection, f_openInBrowserAction);
		f_duplicateServerItem.addListener(SWT.Selection, f_duplicateServerAction);
		f_deleteServerItem.addListener(SWT.Selection, f_deleteServerAction);

		final Listener connectAction = new ServerActionListener("No server to connect to") {
			@Override
			protected void handleEventOnServer(SierraServer server) {
				ConnectProjectsDialog dialog = new ConnectProjectsDialog(
						f_statusTree.getShell());
				dialog.open();
			}
		};

		f_serverConnectItem.addListener(SWT.Selection, connectAction);

		f_synchConnectedProjects.addListener(SWT.Selection,
			new ProjectsActionListener() {
				@Override
				protected void run(List<IJavaProject> projects) {
					new SynchronizeProjectAction().run(projects);
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
						ServerLocationDialog.editServer(f_statusTree.getShell(), server);
					}
				});

		f_scanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<IJavaProject> projects) {
						new NewScan().scan(projects);
					}
				});
		f_rescanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<IJavaProject> projects) {
						new ScanChangedProjectsAction().run(projects);
					}
				});
		f_publishScansItem.addListener(SWT.Selection, new ProjectsActionListener() {
			@Override
			protected void run(List<IJavaProject> projects) {
				// FIX check for projects w/o scans?
				new PublishScanAction().run(projects);
			}
		});
		f_disconnectProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(List<IJavaProject> projects) {
						List<String> projectNames = new ArrayList<String>();
						for(IJavaProject p : projects) {
							projectNames.add(p.getElementName());
						}
						DeleteProjectDataJob.utility(projectNames, null, true);
					}
				});
		
		f_statusTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TreeItem item = (TreeItem) event.item;
				if (item.getData() instanceof FindingAudits) {
					FindingAudits f = (FindingAudits) item.getData();
					FindingDetailsView.findingSelected(f.getFindingId(), false);
				}
				else if (item.getData() instanceof ScanInfo) {
					ScanInfo info = (ScanInfo) item.getData();
					if (info.isPartial()) {
						TreeItem project = item.getParentItem(); 
						ProjectStatus ps = (ProjectStatus) project.getData();
						new NewScan().scan(ps.project);
					}
				}
				else if (item.getData() == NO_SERVER_DATA) {
					asyncUpdateServerInfo();
				}
				
				List<SierraServer> servers = collectServers();
				final boolean onlyServer = servers.size() == 1;
				if (onlyServer) {
					f_manager.setFocus(servers.get(0));
				}
				f_duplicateServerAction.setEnabled(onlyServer);
				f_deleteServerAction.setEnabled(onlyServer);
				f_openInBrowserAction.setEnabled(onlyServer);
			}			
		});
		f_contextMenu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				f_newServerItem.setEnabled(true);
				
				List<SierraServer> servers = collectServers();
				final boolean onlyServer = servers.size() == 1;
				f_duplicateServerAction.setEnabled(onlyServer);
				f_deleteServerAction.setEnabled(onlyServer);
				f_openInBrowserAction.setEnabled(onlyServer);
				f_browseServerItem.setEnabled(onlyServer);
				f_duplicateServerItem.setEnabled(onlyServer);
				f_deleteServerItem.setEnabled(onlyServer);
				f_serverConnectItem.setEnabled(onlyServer);
				f_sendResultFilters.setEnabled(onlyServer);
				f_getResultFilters.setEnabled(onlyServer);
				f_serverPropertiesItem.setEnabled(onlyServer);
				
				List<ProjectStatus> status = collectSelectedProjectStatus();
				final boolean someProjects = !status.isEmpty();
				boolean allConnected = someProjects;
				boolean allHasScans = someProjects;
				if (someProjects) {
					for(ProjectStatus ps : status) {
						if (!f_manager.isConnected(ps.name)) {
							allConnected = false;							
						}
						if (ps.scanInfo.isPartial()) {
							allHasScans = false;
						}
					}				
				}			
				f_scanProjectItem.setEnabled(someProjects);
				f_rescanProjectItem.setEnabled(someProjects);
				f_synchConnectedProjects.setEnabled(someProjects);
				f_publishScansItem.setEnabled(allHasScans);
				f_disconnectProjectItem.setEnabled(allConnected);				
			}			
		});
		
		final AutoJob doServerAutoUpdate = 
			new AutoJob("Server auto-update", lastServerUpdateTime) {
			@Override protected boolean isEnabled() {
				return PreferenceConstants.getServerInteractionSetting().doServerAutoUpdate();
			}
			@Override protected long getDelay() {
				return PreferenceConstants.getServerInteractionPeriodInMinutes() * 60000;			
			}
			@Override protected void run() {
				asyncUpdateServerInfo();	
			}
		};		
		doServerAutoUpdate.schedule(doServerAutoUpdate.getDelay());
		
		final AutoJob doServerAutoSync = 
			new AutoJob("Server auto-sync", 
 					    SynchronizeAllProjectsAction.getLastSyncTime()) {
			@Override protected boolean isEnabled() {
				return PreferenceConstants.getServerInteractionSetting().doServerAutoSync();
			}
			@Override protected long getDelay() {
				return PreferenceConstants.getServerInteractionPeriodInMinutes() * 60000;			
			}
			@Override protected void run() {
				asyncSyncWithServer();	
			}
		};		
		doServerAutoSync.schedule(doServerAutoSync.getDelay());
		
		final IPreferenceStore store = 
			Activator.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == PreferenceConstants.P_SERVER_INTERACTION_SETTING) {
					if (event.getNewValue() != event.getOldValue()) {
						final ServerInteractionSetting s = 
							ServerInteractionSetting.valueOf((String) event.getNewValue());
						final Job job = new DatabaseJob("Switching server interaction") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								synchronized (responseMap) {
									if (s.useAuditThreshold() && checkAutoSyncTrigger(projects)) {
										asyncSyncWithServer();
									}						
									else if (s.doServerAutoSync()) {
										asyncSyncWithServer();
									}
									else if (s.doServerAutoUpdate()) {
										asyncUpdateServerInfo();
									}
								}
								return Status.OK_STATUS;
							}						
						};
						job.schedule();
					}
				}				
			}
			
		});
	}
	
	private abstract class AutoJob extends Job {
		final AtomicLong lastTime;
		public AutoJob(String name, AtomicLong last) {
			super(name);
			setSystem(true);
			lastTime = last;
		}
		protected long computeGap() {
			long now  = System.currentTimeMillis();
			long next = lastTime.get() + getDelay();
			return next - now;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (isEnabled()) {

				long gap  = computeGap();
			    if (gap > 0) {
			    	System.out.println("Wait a bit longer: "+gap);
			    	schedule(gap);
					return Status.OK_STATUS;
			    } 				    	
			    // No need to wait ...
			    run();
			}
			schedule(getDelay());
			
			return Status.OK_STATUS;
		}
		protected abstract void run();
		protected abstract boolean isEnabled();
		protected abstract long getDelay(); // In msec
	}
	
	private List<SierraServer> collectServers() {
		final TreeItem[] si = f_statusTree.getSelection();
		if (si.length == 0) {
			return Collections.emptyList();
		}
		List<SierraServer> servers = new ArrayList<SierraServer>();
		for (TreeItem item : si) {
			if (item.getData() instanceof SierraServer) {
				if (servers.contains(item.getData())) {
					continue;
				}
				servers.add((SierraServer) item.getData());
			}
			else {
				// System.out.println("Got a non-server selection: "+item.getText());
				return Collections.emptyList();
			}
		}
		return servers;
	}
	
	private List<ProjectStatus> collectSelectedProjectStatus() {
		final TreeItem[] si = f_statusTree.getSelection();
		if (si.length == 0) {
			return Collections.emptyList();
		}
		List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
		for (TreeItem item : si) {
			if (item.getData() instanceof ProjectStatus) {
				if (projects.contains(item.getData())) {
					continue;
				}
				projects.add((ProjectStatus) item.getData());
			}
			else if (item.getData() instanceof SierraServer) {
				collectProjects(projects, item);
			}
			else if ("Unconnected".equals(item.getText())) {
				collectProjects(projects, item);
			}
			else {
				ProjectStatus status = inProject(item.getParentItem());
				if (status != null) {
					if (projects.contains(item.getData())) {
						continue;
					}
					projects.add((ProjectStatus) item.getData());
					continue;
				}
				System.out.println("Ignoring selection: "+item.getText());
			}
		}
		return projects;
	}

	private ProjectStatus inProject(TreeItem item) {
		while (item != null) {
			if (item.getData() instanceof ProjectStatus) {
				return (ProjectStatus) item.getData();
			}
			item = item.getParentItem();
		}
		return null;
	}

	private void collectProjects(List<ProjectStatus> projects, TreeItem parent) {
		for (TreeItem item : parent.getItems()) {
			if (projects.contains(item.getData())) {
				continue;
			}
			projects.add((ProjectStatus) item.getData());
		}
	}

	private abstract class ProjectsActionListener implements Listener {
		public final void handleEvent(Event event) {
			List<IJavaProject> projects = new ArrayList<IJavaProject>();
			final TreeItem[] si = f_statusTree.getSelection();
			for (TreeItem item : si) {
				if (item.getData() instanceof ProjectStatus) {
					ProjectStatus ps = (ProjectStatus) item.getData();
					projects.add(ps.project);
				}
				else if (item.getData() instanceof SierraServer) {
					handleProjects(projects, item);
				}
				else if ("Unconnected".equals(item.getText())) {
					handleProjects(projects, item);
				}
				else {
					System.out.println("Ignoring selection: "+item.getText());
				}
			}
			if (!projects.isEmpty()) {
				run(projects);
			}
		}

		private void handleProjects(List<IJavaProject> projects, TreeItem parent) {
			for (TreeItem item : parent.getItems()) {
				ProjectStatus ps = (ProjectStatus) item.getData();
				projects.add(ps.project);
			}
		}

		protected abstract void run(List<IJavaProject> projects);
	}

	@Override
	public void dispose() {
		f_statusTree.dispose();
		super.dispose();
	}

	public void setFocus() {
		f_statusTree.setFocus();
	}

	public void notify(SierraServerManager manager) {
		asyncUpdateContents();
	}
	
	public void notify(Projects p) {
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
		if (server == null) {
			return;
		}		
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
			updateContentsInUI(projects); 
		}
	}
	
	/* Below this is the code to update the view from the database
	 */
	
	@Override
	public void changed() {
		asyncUpdateContents();
	}

	void asyncSyncWithServer() {
		long now = System.currentTimeMillis();
		lastServerUpdateTime.set(now); // Sync >> update
		System.out.println("Sync at: "+now);
		
		Job group = lastSyncGroup.get();
		if (group == null || group.getResult() != null) {
			SynchronizeAllProjectsAction sync = 
				new SynchronizeAllProjectsAction(false);
			sync.run(null);
			lastSyncGroup.set(sync.getGroup());					
		} else {
			System.out.println("Last sync is still running");
		}
	}
	
	void asyncUpdateServerInfo() {
		long now = System.currentTimeMillis();
		lastServerUpdateTime.set(now);
		System.out.println("Update at: "+now);
		
		Job lastJob = lastUpdateJob.get();
		if (lastJob == null || lastJob.getResult() != null) {
			final Job job = new DatabaseJob("Updating server status") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Updating server info", IProgressMonitor.UNKNOWN);
					try {
						updateServerInfo();
					} catch (Exception e) {
						final int errNo = 58; // FIX
						final String msg = I18N.err(errNo);
						return SLStatus.createErrorStatus(errNo, msg, e);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			lastUpdateJob.set(job);
			job.schedule();
		}
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

	private void updateServerInfo() throws Exception {
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {			
			ClientProjectManager cpm = ClientProjectManager.getInstance(c);
			synchronized (responseMap) {				
				final int threshold = PreferenceConstants.getServerInteractionRetryThreshold();
				Set<SierraServer> failedServers = null;
				responseMap.clear();				
				
				for(IJavaProject jp : JDTUtility.getJavaProjects()) {
					final String name = jp.getElementName();
					final int numProblems = Projects.getInstance().getProblemCount(name);
					if (!Projects.getInstance().contains(name) ||
						numProblems > threshold) {
						continue; // Not scanned
					}		

					// Check for new remote audits
					final SierraServer server = f_manager.getServer(name);
					List<SyncTrailResponse> responses = null;
					if (server != null) {
						if (failedServers != null && failedServers.contains(server)) {
							continue;
						}
						final int numServerProbs = server.getProblemCount();
						if (numServerProbs > threshold) {
							if (failedServers == null) {
								failedServers = new HashSet<SierraServer>();
							}
							failedServers.add(server);
							continue;
						}
						if (numServerProbs + numProblems > threshold) {
							continue;
						}
						
						SLProgressMonitor monitor = EmptyProgressMonitor.instance();
						// Try to distinguish server failure/disconnection and RPC failure 
						TroubleshootConnection tc;
						try {
							responses = cpm.getProjectUpdates(server.getServer(), name, monitor);
						} catch (ServerMismatchException e) {
							tc = new TroubleshootWrongServer(server, name);
							if (handleServerProblem(tc, e)) {
								if (failedServers == null) {
									failedServers = new HashSet<SierraServer>();
								}
								failedServers.add(server);
							}
						} catch (SierraServiceClientException e) {
							tc = AbstractServerProjectJob.getTroubleshootConnection(server, name, e);													
							if (handleServerProblem(tc, e)) {
								if (failedServers == null) {
									failedServers = new HashSet<SierraServer>();
								}
								failedServers.add(server);
							}
						} catch (Exception e) {
							tc = new TroubleshootException(server, name, e, 
									                       e instanceof SQLException);
							if (handleServerProblem(tc, e)) {
								if (failedServers == null) {
									failedServers = new HashSet<SierraServer>();
								}
								failedServers.add(server);
							}
						}
					}
					if (responses != null) {
						responseMap.put(name, responses);
						handleServerSuccess(server, name);
					}
				}
			}
			c.commit();
			
			asyncUpdateContents();
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

	/**
	 * Protected by responseMap
	 * @param project 
	 */
	private void handleServerSuccess(SierraServer server, String project) {
		// Contact was successful, so reset counts
		server.markAsConnected();		
		Projects.getInstance().markAsConnected(project);
	}

	/**
	 * Protected by responseMap
	 * 
	 * @return true if consider the server failed
	 */
	private boolean handleServerProblem(TroubleshootConnection tc, Exception e) {
		tc.fix();
		return tc.failServer();
	}
	
	private void updateContents() throws Exception {
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {			
			ClientProjectManager cpm = ClientProjectManager.getInstance(c);
			ClientFindingManager cfm = cpm.getFindingManager();
			ScanManager sm           = ScanManager.getInstance(c);
			final List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
			synchronized (responseMap) {
				for(IJavaProject jp : JDTUtility.getJavaProjects()) {
					final String name = jp.getElementName();
					if (!Projects.getInstance().contains(name)) {
						continue; // Not scanned
					}		

					// Check for new local audits
					List<FindingAudits> findings = cfm.getNewLocalAudits(name); 

					// Check for new remote audits
					List<SyncTrailResponse> responses = responseMap.get(name);
					SierraServer server = f_manager.getServer(name);
					int numServerProblems = server == null ? -1 : server.getProblemCount();
					int numProjectProblems = Projects.getInstance().getProblemCount(name);

					// FIX Check for a full scan (later than what's on the server?)
					final File scan = NewScan.getScanDocumentFile(name);					
					ScanInfo info = sm.getLatestScanInfo(name);
					ProjectStatus s = new ProjectStatus(jp, scan, info, findings, responses, 
							numServerProblems, numProjectProblems);
					projects.add(s);
				}
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
		
		List<SierraServer> servers = collectServers();
		final boolean onlyServer = servers.size() == 1;
		f_duplicateServerAction.setEnabled(onlyServer);
		f_deleteServerAction.setEnabled(onlyServer);
		f_openInBrowserAction.setEnabled(onlyServer);
		
		createTreeItems();
		f_statusTree.setRedraw(true);
		
		checkAutoSyncTrigger(projects);
	}

	/**
	 * @return true if triggered an auto-sync
	 */
	private boolean checkAutoSyncTrigger(final List<ProjectStatus> projects) {
		if (!PreferenceConstants.getServerInteractionSetting().useAuditThreshold()) {
			return false;
		}
		final int auditThreshold = PreferenceConstants.getServerInteractionAuditThreshold();
		if (auditThreshold > 0) {
			int audits = 0;
			for(ProjectStatus ps : projects) {
				audits += ps.numLocalAudits + ps.numServerAudits;
			}
			// FIX should this be per-project?
			if (audits > auditThreshold) {
				asyncSyncWithServer();
				return true;
			}
		}
		return false;
	}
	
	protected void createTreeItems() {
		f_statusTree.removeAll();
				
		final boolean someServers = !f_manager.isEmpty();
		final boolean someProjects = !projects.isEmpty();
		final boolean somethingToSee = someServers || someProjects;
		f_statusTree.setVisible(somethingToSee);
		f_view.hasData(somethingToSee);
		
		boolean byServer = false;
		if (!somethingToSee) {
			return;
		}
		else if (!someServers) {
			createProjectItems();
		}
		else if (!someProjects) {
			byServer = true;
			createServerItems();
		}
		else switch (PreferenceConstants.getServerStatusSort()) {
		case BY_PROJECT:
			createProjectItems();
			break;
		case BY_SERVER:
		default: 
			byServer = true;
			createServerItems();
		}
		f_statusTree.getParent().layout();
		for(TreeItem item : f_statusTree.getItems()) {
			item.setExpanded(true);
			if (byServer) {
				for(TreeItem item2 : item.getItems()) {
					if (item2.getText().endsWith("Connected Projects")) {
						item2.setExpanded(true);
						// Expand projects
						for(TreeItem item3 : item2.getItems()) {
							item3.setExpanded(true);
						}
					}
				}
			}
		}
	}
	
	enum ChangeStatus { 
		NONE() {
			@Override String getLabel() {
				return "";
			}
		}, LOCAL() {
			@Override String getLabel() {
				return "> ";
			}
		}, REMOTE() {
			@Override String getLabel() {
				return "< ";
			}
		}, BOTH() {
			@Override String getLabel() {
				return "< ";
			}
		};
		abstract String getLabel();
		
		ChangeStatus merge(ChangeStatus s) {
			if (s == null) {
				return this;
			}
			switch (this) {
			case NONE:
				return s;
			case LOCAL:
				if (s == NONE) {
					return LOCAL;
				}
				return s == REMOTE ? BOTH : s;
			case REMOTE:
				if (s == NONE) {
					return REMOTE;
				}
				return s == LOCAL ? BOTH : s;
			case BOTH:			
			default:
				return BOTH;
			}
		}
 	}
	
	private void createServerItems() {
		final SierraServer focus = f_manager.getFocus();
		TreeItem focused = null;
		for(String label : f_manager.getLabels()) {
			SierraServer server = f_manager.getServerByLabel(label);
			TreeItem item = new TreeItem(f_statusTree, SWT.NONE);
	
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
			item.setData(server);
			
			if (focus != null && label.equals(focus.getLabel())) {
				focused = item;
			}
			ChangeStatus status = ChangeStatus.NONE;
			if (!f_manager.getProjectsConnectedTo(server).isEmpty()) {
				TreeItem projects = new TreeItem(item, SWT.NONE);
				projects.setImage(SLImages
						.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));

				status = createProjectItems(projects, server);
				projects.setText(status.getLabel()+"Connected Projects");
			}
			ChangeStatus status2 = createScanFilters(item, server);
			ChangeStatus status3 = status.merge(status2);
			item.setText(status3.getLabel()+label+" ["+server.toURLWithContextPath()+']');
		}
		createUnassociatedProjectItems();
		
		if (focused != null) {
			f_statusTree.setSelection(focused);
		}
	}
	
	private ChangeStatus createScanFilters(TreeItem parent, SierraServer server) {
		TreeItem root = new TreeItem(parent, SWT.NONE);
		root.setText("Scan Filters");
		root.setImage(SLImages.getImage(SLImages.IMG_FILTER));
		
		TreeItem label = new TreeItem(root, SWT.NONE);
		label.setText("Coming ...");
		return ChangeStatus.NONE;
	}

	private void createUnassociatedProjectItems() {
		TreeItem parent = null;
		ChangeStatus status = ChangeStatus.NONE;
		
		for(ProjectStatus ps : projects) {
			final SierraServer server = f_manager.getServer(ps.name);
			if (server == null) {
				if (parent == null) {
					parent = new TreeItem(f_statusTree, SWT.NONE);
					parent.setImage(SLImages.getImage(SLImages.IMG_QUERY));
				}
				ChangeStatus pStatus = initProjectItem(new TreeItem(parent, SWT.NONE), server, ps);
				status = status.merge(pStatus);
			}
		}
		if (parent != null) {
			parent.setText(status.getLabel()+"Unconnected");
		}
		
	}

	private ChangeStatus createProjectItems(TreeItem parent, SierraServer server) {
		ChangeStatus status = ChangeStatus.NONE;
		for(String projectName : f_manager.getProjectsConnectedTo(server)) {
			ProjectStatus s = null;
			for(ProjectStatus p : projects) {
				if (projectName.equals(p.name)) {
					s = p;
					break;
				}
			}
			if (s == null) {
				IJavaProject jp = JDTUtility.getJavaProject(projectName);				
				if (jp != null) {
					// No scan data?
					TreeItem root = 
						createProjectItem(parent, server, projectName);					
					root.setData(new ProjectStatus(jp));
					
					new TreeItem(root, SWT.NONE).setText("Needs a local scan");
					continue;
				} else { // closed project?
					IProject p = WorkspaceUtility.getProject(projectName);
					if (p != null && p.exists()) {
						if (p.isOpen()) {
							throw new IllegalStateException("Not a Java project: "+projectName);
						} else { // closed
							TreeItem root = 
								createProjectItem(parent, server, projectName);	
							new TreeItem(root, SWT.NONE).setText("Closed ... no info available");
							continue;
						}
					}
					throw new IllegalStateException("No such Java project: "+projectName);
				}
			}
			ChangeStatus pStatus = initProjectItem(new TreeItem(parent, SWT.NONE), server, s);
			status = status.merge(pStatus);
		}
		return status;
	}

	private TreeItem createProjectItem(TreeItem parent, SierraServer server,
			String projectName) {
		TreeItem root = new TreeItem(parent, SWT.NONE);
		root.setText(projectName+" ["+server.getLabel()+']');
		root.setImage(SLImages
				.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		return root;
	}
	
	private ChangeStatus initProjectItem(final TreeItem root, final SierraServer server, 
			                             final ProjectStatus ps) { 
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
		ChangeStatus status = ChangeStatus.NONE;
			
		root.setImage(SLImages
				.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		root.setData(ps);
		root.setExpanded(true);
		
		if (ps.scanDoc != null && ps.scanDoc.exists()) {
			TreeItem scan = new TreeItem(root, SWT.NONE);
			if (ps.scanInfo != null) {
				Date lastScanTime = ps.scanInfo.getScanTime();
				
				if (ps.scanInfo.isPartial()) {
					// Latest is a re-scan
					scan.setText("Re-scan done locally on "+dateFormat.format(lastScanTime)+" ... click to start full scan");
					scan.setImage(SLImages.getImage(SLImages.IMG_SIERRA_INVESTIGATE));
				} else {
					scan.setText("Last full scan done locally on "+dateFormat.format(lastScanTime));
					scan.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SCAN));
				}
				scan.setData(ps.scanInfo);
				
			} else {
				Date docModified = new Date(ps.scanDoc.lastModified());
				scan.setText("Last full scan done locally on "+dateFormat.format(docModified));
				scan.setData(ps.scanDoc);
				scan.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SCAN));
			}
			//status = status.merge(ChangeStatus.LOCAL);
		}
		if (!ps.localFindings.isEmpty()) {
			TreeItem audits = createAuditItems(root, false, ps.numLocalAudits, ps.localFindings.size(), 
					                           ps.earliestLocalAudit, ps.latestLocalAudit);
			createLocalAuditDetails(audits, ps.localFindings);
			status = status.merge(ChangeStatus.LOCAL);
		}		
		if (ps.numServerProblems > 0) {
			TreeItem problems = new TreeItem(root, SWT.NONE);
			problems.setText(ps.numServerProblems+" consecutive failure"+s(ps.numServerProblems)+
					         " connecting to "+server.getLabel());
			problems.setImage(SLImages.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		}
		if (ps.numProjectProblems > 0) {
			TreeItem problems = new TreeItem(root, SWT.NONE);
			problems.setText(ps.numProjectProblems+" consecutive failure"+s(ps.numProjectProblems)+
					         " getting server info from "+server.getLabel());
			problems.setImage(SLImages.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		}
		if (ps.serverData == null) {
			TreeItem noServer = new TreeItem(root, SWT.NONE);
			noServer.setText("No server info available ... click to update");
			noServer.setData(NO_SERVER_DATA);
		} 
		else if (!ps.serverData.isEmpty()) {
			status = status.merge(ChangeStatus.REMOTE);
			TreeItem audits = createAuditItems(root, true, ps.numServerAudits, ps.serverData.size(), 
					                           ps.earliestServerAudit, ps.latestServerAudit);
			createServerAuditDetails(ps, audits);
		}
		if (server != null) {
			root.setText(status.getLabel()+ps.name+" ["+server.getLabel()+']');
		} else {
			root.setText(status.getLabel()+ps.name);
		}	
		//setAllDataIfNull(root, ps);
		return status;
	}

	/*
	private void setAllDataIfNull(TreeItem root, ProjectStatus ps) {
		if (root != null) {
			if (root.getData() == null) {
				root.setData(ps);
			}
			for(TreeItem item : root.getItems()) {
				setAllDataIfNull(item, ps);
			}
		}
	}
	*/
	
	private TreeItem createAuditItems(final TreeItem root, boolean server, 
			                          int numAudits, int findings, Date earliestA, Date latestA) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
		TreeItem audits = new TreeItem(root, SWT.NONE);
		
		if (server) {
			audits.setText("< "+numAudits+" audit"+s(numAudits)+
					       " on "+findings+" finding"+s(findings)+" on the server");
		} else {
			audits.setText("> "+numAudits+" audit"+s(numAudits)+
					       " on "+findings+" finding"+s(findings));
		}
		audits.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP));
		
		if (earliestA != null) {
			TreeItem earliest = new TreeItem(audits, SWT.NONE);
			earliest.setText("Earliest on "+dateFormat.format(earliestA));
		}
		if (latestA != null && earliestA != latestA) {
			TreeItem latest = new TreeItem(audits, SWT.NONE);
			latest.setText("Latest on "+dateFormat.format(latestA));
		}
		return audits;
	}

	private void createLocalAuditDetails(TreeItem audits, List<FindingAudits> findings) {
		for(FindingAudits f : findings) {
			TreeItem item = new TreeItem(audits, SWT.NONE);
			int num = f.getAudits().size();
			item.setText(num+" audit"+s(num)+" on finding "+f.getFindingId());
			item.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
			item.setData(f);
		}
	}
	
	private void createServerAuditDetails(final ProjectStatus ps,
			TreeItem audits) {
		if (ps.comments > 0) {
			new TreeItem(audits, SWT.NONE).setText(ps.comments+" comment"+s(ps.comments));
		}
		if (ps.importance > 0) {
			new TreeItem(audits, SWT.NONE)
			    .setText(ps.importance+" change"+s(ps.importance)+" to the importance");
		}
		if (ps.summary > 0) {
			new TreeItem(audits, SWT.NONE)
			    .setText(ps.summary+" change"+s(ps.summary)+" to the summary");
		}
		if (ps.read > 0) {
			new TreeItem(audits, SWT.NONE)
			    .setText(ps.read+" other finding"+s(ps.read)+" examined");
		}
		for(Map.Entry<String,Integer> e : ps.userCount.entrySet()) {
			if (e.getValue() != null) {
				int count = e.getValue().intValue();
				if (count > 0) {
					new TreeItem(audits, SWT.NONE)
					    .setText(count+" audit"+s(count)+" by "+e.getKey());
				}
			}
		}
	}
	
	/**
	 * Show by Project
	 */
	private void createProjectItems() {
		for (ProjectStatus ps : projects) {
			final TreeItem root = new TreeItem(f_statusTree, SWT.NONE);
			final SierraServer server = f_manager.getServer(ps.name);
			initProjectItem(root, server, ps);
		}
	}
		
	private static String s(int num) {
		return num <= 1 ? "" : "s";
	}
}
