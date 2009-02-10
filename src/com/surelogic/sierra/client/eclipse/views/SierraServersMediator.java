package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.surelogic.common.eclipse.ImageImageDescriptor;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.WorkspaceUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.images.CommonImages;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.NewScan;
import com.surelogic.sierra.client.eclipse.actions.PublishScanAction;
import com.surelogic.sierra.client.eclipse.actions.ScanChangedProjectsAction;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectAction;
import com.surelogic.sierra.client.eclipse.dialogs.ConnectProjectsDialog;
import com.surelogic.sierra.client.eclipse.dialogs.PromptForFilterNameDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.jobs.OverwriteLocalScanFilterJob;
import com.surelogic.sierra.client.eclipse.jobs.SendScanFiltersJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingAudits;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.Scans;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.NamedServer;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ScanFilter;

public final class SierraServersMediator extends AbstractSierraViewMediator
		implements ISierraServerObserver, IProjectsObserver {
	private static final boolean showFiltersOnServer = true;

	static final String SCAN_FILTERS = "Scan Filters";

	static final String CATEGORIES = "Categories";

	static final String CONNECTED_PROJECTS = "Connected Projects";

	static final String UNCONNECTED_PROJECTS = "Unconnected";

	/**
	 * This should only be changed in the UI thread
	 */
	private List<ProjectStatus> projects = Collections.emptyList();

	/**
	 * This should only be changed in the UI thread
	 */
	private Map<NamedServer, List<ScanFilter>> localFilters = Collections
			.emptyMap();

	/**
	 * A map from a project name to the project info
	 * 
	 * Protected by itself This should only be accessed in a database job
	 * (possibly from multiple threads)
	 */
	private final Map<String, ProjectDO> projectMap = new HashMap<String, ProjectDO>();

	private final TreeViewer f_statusTree;
	private final ActionListener f_serverSyncAction;
	private final ActionListener f_toggleAutoSyncAction;
	private final ActionListener f_newServerAction;
	private final ActionListener f_deleteServerAction;
	private final ActionListener f_openInBrowserAction;

	private final Listener f_serverConnectAction;
	private final Listener f_synchConnectedProjectsAction;
	private final Listener f_sendResultFiltersAction;
	private final Listener f_getResultFiltersAction;
	private final Listener f_serverPropertiesAction;
	private final Listener f_scanProjectAction;
	private final Listener f_rescanProjectAction;
	private final Listener f_publishScansAction;
	private final Listener f_disconnectProjectAction;

	private abstract class ActionListener extends Action implements Listener {
		ActionListener(final String text, final String tooltip) {
			super(text, IAction.AS_PUSH_BUTTON);
			setToolTipText(tooltip);
		}

		ActionListener(final Image image, final String tooltip) {
			this(tooltip, tooltip);
			setImageDescriptor(new ImageImageDescriptor(image));
		}

		public final void handleEvent(final Event event) {
			run();
		}

		@Override
		public abstract void run();
	}

	private class ServerActionListener extends ActionListener {
		private final String msgIfNoServer;

		ServerActionListener(final String text, final String tooltip,
				final String msg) {
			super(text, tooltip);
			msgIfNoServer = msg;
		}

		ServerActionListener(final Image image, final String tooltip,
				final String msg) {
			super(image, tooltip);
			msgIfNoServer = msg;
		}

		ServerActionListener(final String msg) {
			super("", "");
			msgIfNoServer = msg;
		}

		@Override
		public final void run() {
			final SelectedServers ss = collectServers();
			ss.other.addAll(ss.indirect);
			final List<ConnectedServer> servers = ss.other;
			if (servers.size() == 1) {
				handleEventOnServer(servers.get(0));
				/*
				 * } final ConnectedServer server = f_manager.getFocus(); if
				 * (server != null) { handleEventOnServer(server);
				 */
			} else {
				handleEventWithoutServer();
			}
		}

		protected void handleEventOnServer(final ConnectedServer server) {
			// Do nothing
		}

		protected void handleEventWithoutServer() {
			if (msgIfNoServer != null) {
				SLLogger.getLogger().warning(msgIfNoServer);
			}
		}
	}

	private abstract class ScanFilterActionListener extends ActionListener {
		ScanFilterActionListener(final String tooltip) {
			super((Image) null, tooltip);
		}

		@Override
		public final void run() {
			for (final ScanFilter f : collectScanFilters()) {
				handleEventOnFilter(f);
			}
		}

		protected abstract void handleEventOnFilter(final ScanFilter filter);
	}

	private abstract class IJavaProjectsActionListener extends
			ServerActionListener {
		IJavaProjectsActionListener(final String msg) {
			super(msg);
		}

		IJavaProjectsActionListener(final Image image, final String tooltip,
				final String msg) {
			super(image, tooltip, msg);
		}

		@Override
		protected final void handleEventOnServer(final ConnectedServer server) {
			run(server, ConnectedServerManager.getInstance()
					.getProjectsConnectedTo(server));
		}

		protected abstract void run(ConnectedServer server,
				List<String> projectNames);
	}

	private final ConnectedServerManager f_manager = ConnectedServerManager
			.getInstance();

	public SierraServersMediator(final SierraServersView view,
			final TreeViewer statusTree) {
		super(view);

		f_statusTree = statusTree;
		f_statusTree.getTree().addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(final Event event) {
				final Menu contextMenu = new Menu(statusTree.getTree()
						.getShell(), SWT.POP_UP);
				setupContextMenu(contextMenu);
				statusTree.getTree().setMenu(contextMenu);

				// System.out.println("Empty Selection: "+statusTree.getSelection().isEmpty());
			}
		});

		f_statusTree.setContentProvider(new ContentProvider());
		f_statusTree.setLabelProvider(new LabelProvider());

		f_toggleAutoSyncAction = new ServerActionListener("Toggle Auto-sync",
				"Toggle whether the server(s) automatically synchronize",
				"No server to toggle") {
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				ConnectedServerManager.getInstance().setAutoSyncFor(server,
						!server.getLocation().isAutoSync());
			}
		};

		f_serverSyncAction = new ActionListener("Synchronize All",
				"Synchronize with all servers and connected projects") {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return SLImages
						.getImageDescriptor(CommonImages.IMG_SIERRA_SYNC);
			}

			@Override
			public void run() {
				SierraServersAutoSync
						.asyncSyncWithServer(ServerSyncType.ALL, 0);
			}
		};
		view.addToActionBar(f_serverSyncAction);
		/*
		 * view.addToActionBar(f_serverSyncAction); view.addToActionBar(new
		 * Separator());
		 */
		f_newServerAction = new ActionListener(SLImages
				.getImage(CommonImages.IMG_EDIT_NEW),
				"New team server location") {
			@Override
			public void run() {
				ServerLocationDialog.newServer(f_statusTree.getTree()
						.getShell());
			}
		};
		view.addToActionBar(f_newServerAction);

		f_openInBrowserAction = new ServerActionListener("Browse",
				"Open the selected team server in a Web browser",
				"No server to browse") {
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				openInBrowser(server);
			}
		};

		f_deleteServerAction = new IJavaProjectsActionListener(SLImages
				.getImage(CommonImages.IMG_EDIT_DELETE),
				"Deletes the selected team server location",
				"No server to delete") {
			@Override
			protected void run(final ConnectedServer server,
					final List<String> projectNames) {
				final String serverName = server.getName();
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

		f_synchConnectedProjectsAction = new ProjectsActionListener() {
			@Override
			protected void run(final List<IJavaProject> projects) {
				new SynchronizeProjectAction().run(projects);
			}
		};

		f_serverConnectAction = new ServerActionListener(
				"No server to connect to, or no project to connect") {
			boolean f_syncAfterConnect = true;

			private List<IJavaProject> collectProjects() {
				final ProjectStatusCollector<IJavaProject> projects = new ProjectStatusCollector<IJavaProject>() {
					@Override
					IJavaProject getSelectedInfo(final ProjectStatus s) {
						return s.project;
					}
				};
				return projects
						.collectSelectedProjects((IStructuredSelection) f_statusTree
								.getSelection());
			}

			private void doConnect(final ConnectedServer server,
					final List<IJavaProject> projects) {
				for (final IJavaProject project : projects) {
					if (f_manager.isConnected(project.getElementName())) {
						continue;
					}
					f_manager.connect(project.getElementName(), server);
				}
			}

			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				final List<IJavaProject> projects = collectProjects();
				// Remove connected ones
				final ConnectedServerManager csm = ConnectedServerManager
						.getInstance();
				final Iterator<IJavaProject> it = projects.iterator();
				while (it.hasNext()) {
					final IJavaProject p = it.next();
					if (csm.getServer(p.getElementName()) != null) {
						it.remove();
					}
				}
				if (!projects.isEmpty()) {
					boolean syncAfterConnect = true;
					final MessageDialog dialog = new MessageDialog(
							f_statusTree.getTree().getShell(),
							"Synchronize Project(s) on Connect",
							null,
							"Do you want to synchronize the newly connected projects?",
							MessageDialog.QUESTION,
							new String[] { "Yes", "No" }, 0);
					syncAfterConnect = dialog.open() == 0;
					doConnect(server, projects);

					if (syncAfterConnect) {
						new SynchronizeProjectAction().run(projects);
					}
					return;
				}
				final ConnectProjectsDialog dialog = new ConnectProjectsDialog(
						f_statusTree.getTree().getShell());
				dialog.open();
			}

			@Override
			protected void handleEventWithoutServer() {
				final List<IJavaProject> projects = collectProjects();
				if (projects.isEmpty()) {
					super.handleEventWithoutServer();
				} else {
					final ServerSelectionDialog dialog = new ServerSelectionDialog(
							f_statusTree.getTree().getShell(), projects.get(0)
									.getElementName()) {
						@Override
						protected void addToEntryPanel(
								final Composite entryPanel) {
							super.addToEntryPanel(entryPanel);

							final Button syncToggle = new Button(entryPanel,
									SWT.CHECK);
							syncToggle
									.setText("Synchronize newly connected projects on finish");
							syncToggle.setSelection(true);
							syncToggle.addListener(SWT.Selection,
									new Listener() {
										public void handleEvent(
												final Event event) {
											f_syncAfterConnect = syncToggle
													.getSelection();
										}
									});
						}
					};
					dialog.setUseForAllUnconnectedProjects(true);
					if (dialog.open() == Window.CANCEL) {
						return;
					}
					final ConnectedServer server = dialog.getServer();
					if (!dialog.confirmNonnullServer()) {
						return;
					}
					doConnect(server, projects);

					if (f_syncAfterConnect) {
						new SynchronizeProjectAction().run(projects);
					}
				}
			}
		};

		f_sendResultFiltersAction = new ServerActionListener(
				"Send local scan filter pressed with no server focus.") {

			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				final String msg = "What do you want to call your scan filter "
						+ "on the Sierra server '" + server.getName() + "'";
				final PromptForFilterNameDialog dialog = new PromptForFilterNameDialog(
						f_statusTree.getTree().getShell(), msg);
				if (dialog.open() == 0) {
					/*
					 * Yes was selected, so send the local scan filters to the
					 * server.
					 */
					if (SendScanFiltersJob.ENABLED) {
						final Job job = new SendScanFiltersJob(
								ServerFailureReport.SHOW_DIALOG, server, dialog
										.getText());
						job.schedule();
					}
				}
			}
		};

		f_getResultFiltersAction = new ScanFilterActionListener(
				"Overwrite local scan filter") {
			@Override
			protected void handleEventOnFilter(final ScanFilter f) {
				final String msg = "Do you want to overwrite your local scan filter with"
						+ " the scan filter '" + f.getName() + "'?";
				final MessageDialog dialog = new MessageDialog(f_statusTree
						.getTree().getShell(), "Overwrite Local Scan Filter",
						null, msg, MessageDialog.QUESTION, new String[] {
								"Yes", "No" }, 0);
				if (dialog.open() == 0) {
					/*
					 * Yes was selected, so get the result filters from the
					 * server.
					 */

					final Job job = new OverwriteLocalScanFilterJob(f);
					job.schedule();
				}
			}
		};

		f_serverPropertiesAction = new ServerActionListener(
				"Edit server pressed with no server focus.") {
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				ServerLocationDialog.editServer(f_statusTree.getTree()
						.getShell(), server);
			}
		};

		f_scanProjectAction = new ProjectsActionListener() {
			@Override
			protected void run(final List<IJavaProject> projects) {
				new NewScan().scan(projects);
			}
		};
		f_rescanProjectAction = new ProjectsActionListener() {
			@Override
			protected void run(final List<IJavaProject> projects) {
				new ScanChangedProjectsAction().run(projects);
			}
		};
		f_publishScansAction = new ProjectsActionListener() {
			@Override
			protected void run(final List<IJavaProject> projects) {
				// FIX check for projects w/o scans?
				new PublishScanAction().run(projects);
			}
		};
		f_disconnectProjectAction = new ProjectsActionListener() {
			@Override
			protected void run(final List<IJavaProject> projects) {
				final List<String> projectNames = new ArrayList<String>();
				for (final IJavaProject p : projects) {
					projectNames.add(p.getElementName());
				}
				DeleteProjectDataJob.utility(projectNames, null, true);
			}
		};
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-team-servers";
	}

	public String getNoDataI18N() {
		return "sierra.eclipse.noDataSierraServers";
	}

	@Override
	public Listener getNoDataListener() {
		return f_newServerAction;
	}

	@Override
	public void init() {
		// Actions in reverse order
		f_view.addToViewMenu(f_serverSyncAction);
		// f_view.addToViewMenu(f_buglinkSyncAction);
		// f_view.addToViewMenu(f_serverUpdateAction);
		// f_view.addToViewMenu(f_toggleAutoSyncAction);
		f_view.addToViewMenu(new Separator());

		final ServerStatusSort sort = PreferenceConstants.getServerStatusSort();
		final Action sortByServerAction = new Action("Show by Team Server",
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				setSortByServer(isChecked());
			}
		};
		final Action sortByProjectAction = new Action("Show by Project",
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				setSortByServer(!isChecked());
			}
		};
		sortByServerAction.setChecked(ServerStatusSort.BY_SERVER == sort);
		sortByProjectAction.setChecked(ServerStatusSort.BY_PROJECT == sort);
		f_view.addToViewMenu(sortByProjectAction);
		f_view.addToViewMenu(sortByServerAction);

		super.init();
		f_manager.addObserver(this);
		notify(f_manager);

		f_statusTree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				if (event.getSelection().isEmpty()) {
					return; // FIX
				}
				IStructuredSelection selection;
				if (event.getSelection() instanceof IStructuredSelection) {
					selection = (IStructuredSelection) event.getSelection();
				} else {
					return;
				}
				if (selection.size() != 1) {
					return;
				}
				final ServersViewContent item = (ServersViewContent) selection
						.getFirstElement();
				if (item.getData() instanceof FindingAudits) {
					final FindingAudits f = (FindingAudits) item.getData();
					FindingDetailsView.findingSelected(f.getFindingId(), false);
				} else if (item.getData() instanceof ScanInfo) {
					final ScanInfo info = (ScanInfo) item.getData();
					if (info.isPartial()) {
						final ServersViewContent project = item.getParent();
						final ProjectStatus ps = (ProjectStatus) project
								.getData();
						new NewScan().scan(ps.project);
					}
				}
			}

		});
		f_statusTree
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(
							final SelectionChangedEvent event) {
						final List<ConnectedServer> servers = collectServers().indirect;
						final boolean onlyServer = servers.size() == 1;
						if (onlyServer) {
							final ConnectedServer focus = servers.get(0);
							if (f_manager.getFocus() != focus) {
								f_manager.setFocus(servers.get(0));
							}
						}
					}
				});
	}

	void setupContextMenu(final Menu contextMenu) {
		final SelectedServers servers = collectServers();
		final boolean onlyServer = servers.indirect.size() == 1;
		final boolean onlyTeamServer;
		final boolean onlyBugLink;
		final AutoSyncType syncType;
		if (onlyServer) {
			final ConnectedServer focus = servers.indirect.get(0);
			onlyTeamServer = focus.isTeamServer();
			onlyBugLink = focus != null;
			syncType = focus.getLocation().isAutoSync() ? AutoSyncType.ON
					: AutoSyncType.OFF;
		} else {
			onlyTeamServer = onlyBugLink = false;
			syncType = AutoSyncType.MIXED;
		}
		final boolean enableSendFilters = SendScanFiltersJob.ENABLED
				&& onlyBugLink && onlyServer;
		final boolean enableConnect = onlyTeamServer
				&& !servers.direct.isEmpty();
		if (onlyServer) {
			addServerMenuItems(contextMenu, syncType, enableConnect,
					enableSendFilters);
			return;
		}

		final List<ScanFilter> filters = collectScanFilters();
		final boolean onlyScanFilter = filters.size() == 1;
		if (onlyScanFilter) {
			addScanFilterMenuItems(contextMenu);
			return;
		}

		final List<ProjectStatus> status = collectSelectedProjectStatus();
		final boolean someProjects = !status.isEmpty();
		boolean allConnected = someProjects;
		boolean allHasScans = someProjects;
		if (someProjects) {
			for (final ProjectStatus ps : status) {
				if ((f_manager == null) || (ps == null)) {
					// LOG.severe("Null project status");
					continue;
				}
				if (!f_manager.isConnected(ps.name)) {
					allConnected = false;
				}
				if (ps.scanDoc == null || !ps.scanDoc.exists()) {
					allHasScans = false;
				} else if (ps.scanInfo != null && ps.scanInfo.isPartial()) {
					allHasScans = false;
				}
			}
		}
		if (someProjects) {
			addProjectMenuItems(contextMenu, allHasScans, allConnected);
			return;
		}
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		if (si.size() == 1) {
			final ServersViewContent c = (ServersViewContent) si
					.getFirstElement();
			if (c.getData() == null && c.getText().endsWith(SCAN_FILTERS)) {
				addSendLocalFilter_MenuItem(contextMenu);
			}
			return;
		}
		addNothingSelected_MenuItems(contextMenu);
	}

	private List<ScanFilter> collectScanFilters() {
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		if (si.size() == 0) {
			return Collections.emptyList();
		}
		final List<ScanFilter> filters = new ArrayList<ScanFilter>();
		@SuppressWarnings("unchecked")
		final Iterator it = si.iterator();
		while (it.hasNext()) {
			final ServersViewContent item = (ServersViewContent) it.next();
			if (item.getData() instanceof ScanFilter) {
				filters.add((ScanFilter) item.getData());
			}
		}
		return filters;
	}

	private static class SelectedServers {
		final List<ConnectedServer> direct;
		final List<ConnectedServer> indirect;
		final List<ConnectedServer> other;

		SelectedServers(final boolean allocate) {
			if (allocate) {
				direct = new ArrayList<ConnectedServer>();
				indirect = new ArrayList<ConnectedServer>();
				other = new ArrayList<ConnectedServer>();
			} else {
				direct = indirect = other = Collections.emptyList();
			}
		}

		void clear() {
			direct.clear();
			indirect.clear();
			other.clear();
		}
	}

	SelectedServers collectServers() {
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		/*
		 * String lastMethod = new
		 * Throwable().getStackTrace()[1].getMethodName();
		 * System.out.println("collectServers() from "
		 * +lastMethod+"(): "+si.size());
		 */
		if (si.size() == 0) {
			return new SelectedServers(false);
		}
		final SelectedServers servers = new SelectedServers(true);
		@SuppressWarnings("unchecked")
		final Iterator it = si.iterator();
		while (it.hasNext()) {
			final ServersViewContent item = (ServersViewContent) it.next();
			if (item.getData() instanceof ConnectedServer) {
				final ConnectedServer s = (ConnectedServer) item.getData();
				if (servers.indirect.contains(s)) {
					continue;
				}
				servers.direct.add(s);
				servers.indirect.add(s);
			} else {
				if (item.getText().endsWith(CONNECTED_PROJECTS)) {
					return new SelectedServers(false);
				}
				if (item.getText().endsWith(SCAN_FILTERS)) {
					servers.clear();
					servers.other.add(inServer(item));
					return servers;
				}
				// System.out.println("Got a non-server selection:
				// "+item.getText());
				final ConnectedServer s = inServer(item);
				if (s != null) {
					if (servers.indirect.contains(s)) {
						continue;
					}
					servers.indirect.add(s);
					continue;
				}
				return new SelectedServers(false);
			}
		}
		return servers;
	}

	private ConnectedServer inServer(ServersViewContent item) {
		while (item != null) {
			final Object data = item.getData();
			if (data instanceof ProjectStatus) {
				// Inside of a project, not a server
				return null;
			}
			if (data instanceof ScanFilter) {
				// Not a server
				return null;
			}
			if (data instanceof ConnectedServer) {
				return (ConnectedServer) item.getData();
			}
			/*
			 * if (data == null && SCAN_FILTERS.equals(item.getText())) { // Not
			 * a server return null; }
			 */
			item = item.getParent();
		}
		return null;
	}

	private List<ProjectStatus> collectSelectedProjectStatus() {
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		final ProjectStatusCollector<ProjectStatus> collector = new ProjectStatusCollector<ProjectStatus>() {
			@Override
			ProjectStatus getSelectedInfo(final ProjectStatus s) {
				return s;
			}
		};
		return collector.collectSelectedProjects(si);
	}

	private abstract class ProjectStatusCollector<T> {
		private void add(final List<T> projects, final ProjectStatus s) {
			final T info = getSelectedInfo(s);
			if (projects.contains(info)) {
				// Nothing else to do, since it's already there
				return;
			}
			projects.add(info);
		}

		protected List<T> collectSelectedProjects(final IStructuredSelection si) {
			if (si.size() == 0) {
				return Collections.emptyList();
			}
			final List<T> projects = new ArrayList<T>();

			@SuppressWarnings("unchecked")
			final Iterator it = si.iterator();
			while (it.hasNext()) {
				final ServersViewContent item = (ServersViewContent) it.next();
				if (item.getData() instanceof ProjectStatus) {
					add(projects, (ProjectStatus) item.getData());
				} else if (item.getData() instanceof ConnectedServer) {
					collectProjects(projects, item, true);
				} else if (item.getText().endsWith(CONNECTED_PROJECTS)) {
					collectProjects(projects, item, false);
				} else if (item.getText().endsWith(UNCONNECTED_PROJECTS)) {
					collectProjects(projects, item, false);
				} else {
					final ProjectStatus status = inProject(item.getParent());
					if (status != null) {
						add(projects, status);
						continue;
					}
					System.out.println("Ignoring selection: " + item.getText());
				}
			}
			return projects;
		}

		private void collectProjects(final List<T> projects,
				final ServersViewContent parent, final boolean server) {
			final ServersViewContent projectItems;
			if (server) {
				if (parent.getChildren().length == 0) {
					return; // No children
				}
				// look for CONNECTED_PROJECTS (if a server)
				ServersViewContent temp = null;
				for (final ServersViewContent c : parent.getChildren()) {
					if (c.getText().endsWith(CONNECTED_PROJECTS)) {
						temp = c;
						break;
					}
				}
				projectItems = temp;
			} else {
				projectItems = parent;
			}
			if (projectItems == null) {
				return;
			}
			for (final ServersViewContent item : projectItems.getChildren()) {
				if (item.getData() == null) {
					LOG.severe("Null project status");
					continue;
				}
				if (item.getData() instanceof ProjectStatus) {
					add(projects, (ProjectStatus) item.getData());
				} else {
					LOG.severe("Unexpected: " + item.getData());
				}
			}
		}

		private ProjectStatus inProject(ServersViewContent item) {
			while (item != null) {
				if (item.getData() instanceof ProjectStatus) {
					return (ProjectStatus) item.getData();
				}
				item = item.getParent();
			}
			return null;
		}

		abstract T getSelectedInfo(ProjectStatus s);
	}

	private abstract class ProjectsActionListener extends
			ProjectStatusCollector<IJavaProject> implements Listener {
		public final void handleEvent(final Event event) {
			// FIX merge with collectProjects?
			final IStructuredSelection si = (IStructuredSelection) f_statusTree
					.getSelection();
			final List<IJavaProject> projects = collectSelectedProjects(si);
			if (!projects.isEmpty()) {
				run(projects);
			}
		}

		@Override
		IJavaProject getSelectedInfo(final ProjectStatus s) {
			return s.project;
		}

		protected abstract void run(List<IJavaProject> projects);
	}

	@Override
	public void dispose() {
		// f_statusTree.dispose();
		super.dispose();
	}

	public void setFocus() {
		// f_statusTree.setFocus();
	}

	public void notify(final ConnectedServerManager manager) {
		asyncUpdateContents();
	}

	public void notify(final Projects p) {
		asyncUpdateContents();
	}

	/*
	 * private static boolean same(TableItem[] items, String[] labels) { if
	 * (items.length != labels.length) return false; for (int i = 0; i <
	 * labels.length; i++) { boolean same =
	 * items[i].getText().equals(labels[i]); if (!same) return false; } return
	 * true; }
	 */

	private static void openInBrowser(final ConnectedServer server) {
		if (server == null) {
			return;
		}
		final String name = "Sierra Server '" + server.getName() + "'";

		try {
			final IWebBrowser browser = PlatformUI.getWorkbench()
					.getBrowserSupport().createBrowser(
							IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.STATUS,
							server.getName(), name, name);
			final URL url = server.getLocation().createHomeURL();
			browser.openURL(url);
		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(26), e);
		}
	}

	public void setSortByServer(final boolean checked) {
		ServerStatusSort sort;
		if (checked) {
			sort = ServerStatusSort.BY_SERVER;
		} else {
			sort = ServerStatusSort.BY_PROJECT;
		}
		if (sort != PreferenceConstants.getServerStatusSort()) {
			PreferenceConstants.setServerStatusSort(sort);
			updateContentsInUI(projects, localFilters);
		}
	}

	/*
	 * Below this is the code to update the view from the database
	 */
	@Override
	public void changed() {
		asyncUpdateContents();
	}

	/*
	 * private enum UpdateType { ALL, }
	 */

	private void asyncUpdateContents() {
		asyncUpdateContentsForUI(new IViewUpdater() {
			public void updateContentsForUI() {
				// FIX switch to waiting if there's already stale data?
				if (f_view.getStatus() != IViewCallback.Status.DATA_READY) {
					f_view.setStatus(IViewCallback.Status.WAITING_FOR_DATA);
				}
			}
		});
		final long now = startingUpdate();

		final Job job = new DatabaseJob("Updating project status",
				Job.INTERACTIVE) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					if (continueUpdate(now)) {
						// This is the latest update
						updateContents(now);
					}
				} catch (final Exception e) {
					final int errNo = 58; // FIX
					final String msg = I18N.err(errNo);
					return SLEclipseStatusUtility.createErrorStatus(errNo, msg,
							e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateContents(final long now) throws Exception {
		final Connection c = Data.getInstance().transactionConnection();
		Exception exc = null;
		try {
			final ClientProjectManager cpm = ClientProjectManager
					.getInstance(c);
			final ClientFindingManager cfm = cpm.getFindingManager();
			final Query q = new ConnectionQuery(c);
			final Scans sm = new Scans(q);
			final List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
			final Map<NamedServer, List<ScanFilter>> filters;
			synchronized (projectMap) {
				for (final IJavaProject jp : JDTUtility.getJavaProjects()) {
					if (!continueUpdate(now)) {
						// This isn't the latest update, so stop
						c.rollback();
						return;
					}
					final String name = jp.getElementName();
					if (!Projects.getInstance().contains(name)) {
						continue; // Not scanned
					}

					// Check for new local audits
					final int numLocalAudits = cfm.countNewLocalAudits(name);

					// Check for server problems
					final ConnectedServer server = f_manager.getServer(name);
					final int numServerProblems = server == null ? -1
							: ConnectedServerManager.getInstance().getStats(
									server).getProblemCount();
					final int numProjectProblems = Projects.getInstance()
							.getProblemCount(name);

					// FIX Check for a full scan (later than what's on the
					// server?)
					final File scan = NewScan.getScanDocumentFile(name);
					final ScanInfo info = sm.getLatestScanInfo(name);
					final ProjectDO dbInfo = projectMap.get(name);
					final String filterName = SettingQueries
							.scanFilterNameForProject(name).perform(q);
					final ProjectStatus s = new ProjectStatus(jp, scan, info,
							numLocalAudits, numServerProblems,
							numProjectProblems, dbInfo, filterName);
					projects.add(s);
				}
				filters = SettingQueries.getLocalScanFilters().perform(q);
			}
			finishedUpdate(now);

			asyncUpdateContentsForUI(new IViewUpdater() {
				public void updateContentsForUI() {
					updateContentsInUI(projects, filters);
				}
			});
			c.commit();
		} catch (final Exception e) {
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

	public void updateContentsInUI(final List<ProjectStatus> projects,
			final Map<NamedServer, List<ScanFilter>> filters) {
		// No need to synchronize since only updated/viewed in UI thread?
		this.projects = projects;
		this.localFilters = filters;

		final List<ConnectedServer> servers = collectServers().indirect;
		final boolean onlyServer = servers.size() == 1;
		f_deleteServerAction.setEnabled(onlyServer);
		f_openInBrowserAction.setEnabled(onlyServer);

		final IStructuredSelection selection = (IStructuredSelection) f_statusTree
				.getSelection();
		final TreeInput input = createTreeInput();
		f_statusTree.setInput(input);
		f_statusTree.getTree().getParent().layout();
		f_statusTree.expandToLevel(3);
		f_statusTree.setSelection(input.translateSelection(selection));

		checkAutoSyncTrigger(projects);
	}

	/**
	 * @return true if triggered an auto-sync
	 */
	private boolean checkAutoSyncTrigger(final List<ProjectStatus> projects) {
		// if (!PreferenceConstants.getServerInteractionSetting()
		// .useAuditThreshold()) {
		// return false;
		// }
		final int auditThreshold = PreferenceConstants
				.getServerInteractionAuditThreshold();
		if (auditThreshold > 0) {
			final ConnectedServerManager manager = ConnectedServerManager
					.getInstance();
			int audits = 0;
			for (final ProjectStatus ps : projects) {
				final ConnectedServer server = manager.getServer(ps.name);
				if (server != null && server.getLocation().isAutoSync()) {
					audits += ps.numLocalAudits;
				}
			}
			// FIX should this be per-project?
			if (audits > auditThreshold) {
				SierraServersAutoSync.asyncSyncWithServer(
						ServerSyncType.BY_SERVER_SETTINGS, 2000);
				return true;
			}
		}
		return false;
	}

	static class TreeInput {
		final boolean byServer;
		final ServersViewContent[] content;

		TreeInput(final boolean server, final ServersViewContent[] c) {
			byServer = server;
			content = c;
		}

		private Map<Object, ServersViewContent> prepTranslation() {
			final Map<Object, ServersViewContent> map = new HashMap<Object, ServersViewContent>();
			for (final ServersViewContent c : content) {
				processContent(map, c);
			}
			return map;
		}

		private void processContent(final Map<Object, ServersViewContent> map,
				final ServersViewContent c) {
			if (c == null || c.getData() == null) {
				return;
			}
			map.put(createKey(c), c);

			final ServersViewContent[] children = c.getChildren();
			if (children != null) {
				for (final ServersViewContent child : children) {
					processContent(map, child);
				}
			}
		}

		private Object createKey(final ServersViewContent c) {
			final Object data = c.getData();
			if (data instanceof String) {
				return data + " for " + c.getParent();
			} else {
				return data;
			}
		}

		@SuppressWarnings("unchecked")
		ISelection translateSelection(final IStructuredSelection selection) {
			if (selection.isEmpty()) {
				return selection;
			}
			final Map<Object, ServersViewContent> map = prepTranslation();
			final List<ServersViewContent> selected = new ArrayList<ServersViewContent>();

			final Iterator it = selection.iterator();
			while (it.hasNext()) {
				final Object key = createKey((ServersViewContent) it.next());
				final ServersViewContent translation = map.get(key);
				if (translation != null) {
					selected.add(translation);
				}
			}
			return new IStructuredSelection() {
				public boolean isEmpty() {
					return selected.isEmpty();
				}

				public Object getFirstElement() {
					return selected.get(0);
				}

				public Iterator iterator() {
					return selected.iterator();
				}

				public int size() {
					return selected.size();
				}

				public Object[] toArray() {
					return selected.toArray();
				}

				public List toList() {
					return selected;
				}
			};
		}
	}

	private TreeInput createTreeInput() {
		final boolean someServers = !f_manager.isEmpty();
		final boolean someProjects = !projects.isEmpty();
		final boolean somethingToSee = someServers || someProjects;
		// f_statusTree.setVisible(somethingToSee);
		f_view.hasData(somethingToSee);

		if (!somethingToSee) {
			return new TreeInput(false, emptyChildren);
		} else if (!someServers) {
			return new TreeInput(false, createProjectItems());
		} else if (!someProjects) {
			return new TreeInput(true, createServerItems());
		} else {
			switch (PreferenceConstants.getServerStatusSort()) {
			case BY_PROJECT:
				return new TreeInput(false, createProjectItems());
			case BY_SERVER:
			default:
				return new TreeInput(true, createServerItems());
			}
		}
	}

	enum ServerStatus {
		OK, WARNING, ERROR;

		ServerStatus merge(final ServerStatus s) {
			switch (s) {
			case ERROR:
				return ERROR;
			case WARNING:
				return this == ERROR ? ERROR : WARNING;
			case OK:
			default:
				return this;
			}
		}
	}

	enum ChangeStatus {
		NONE() {
			@Override
			String getLabel() {
				return "";
			}
		},
		LOCAL() {
			@Override
			String getLabel() {
				return "> ";
			}
		},
		REMOTE() {
			@Override
			String getLabel() {
				return "< ";
			}
		},
		BOTH() {
			@Override
			String getLabel() {
				return "< ";
			}
		};
		abstract String getLabel();

		ChangeStatus merge(final ChangeStatus s) {
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

	private ServersViewContent[] createServerItems() {
		final List<ServersViewContent> content = new ArrayList<ServersViewContent>();
		for (final ConnectedServer server : f_manager.getServers()) {
			final ServersViewContent serverNode = createServerItem(server);
			content.add(serverNode);
		}
		createUnknownServers(content);
		createUnassociatedProjectItems(content);
		/*
		 * if (focused != null) { f_statusTree.setSelection(focused); }
		 */
		// createLocalScanFilterItems(content);
		return content.toArray(emptyChildren);
	}

	private void createUnknownServers(final List<ServersViewContent> content) {
		for (final Map.Entry<NamedServer, List<ScanFilter>> e : localFilters
				.entrySet()) {
			final NamedServer label = e.getKey();
			final ConnectedServer server = f_manager.getServerByUuid(label
					.getUuid());
			if (server != null) {
				// Already known, so already handled elsewhere
				continue;
			}
			final List<ScanFilter> filters = e.getValue();
			if (filters.isEmpty()) {
				continue;
			}
			final ServersViewContent serverRoot = new ServersViewContent(null,
					SLImages.getImage(CommonImages.IMG_SIERRA_SERVER_GRAY));
			final List<ServersViewContent> children = new ArrayList<ServersViewContent>();
			for (final ScanFilter f : e.getValue()) {
				final String name = f.getName();
				final ServersViewContent filter = createLabel(serverRoot,
						children, name, ChangeStatus.NONE);
				filter.setData(f);
			}
			serverRoot.setText(label.getName());
			serverRoot.setChildren(children.toArray(emptyChildren));
			serverRoot.setData(filters);
		}
	}

	private ServersViewContent createServerItem(final ConnectedServer server) {
		final List<ServersViewContent> serverContent = new ArrayList<ServersViewContent>();

		final ServersViewContent serverNode = new ServersViewContent(null,
				SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		serverNode.setData(server);

		final ServersViewContent scanFilters = createScanFilters(serverNode,
				server);
		if (scanFilters != null) {
			serverContent.add(scanFilters);
		}
		if (!f_manager.getProjectsConnectedTo(server).isEmpty()) {
			final ServersViewContent projects = new ServersViewContent(
					serverNode, SLImages.getImage(CommonImages.IMG_PROJECT));
			serverContent.add(projects);

			createProjectItems(projects, server);
			projects.setText(projects.getChangeStatus().getLabel()
					+ CONNECTED_PROJECTS);
		}

		serverNode.setChildren(serverContent.toArray(emptyChildren));
		final ChangeStatus status3 = serverNode.getChangeStatus();
		serverNode.setText(status3.getLabel() + server.getName() + " ["
				+ server.getLocation().createHomeURL() + ']');
		return serverNode;
	}

	private ServersViewContent createScanFilters(
			final ServersViewContent serverNode, final ConnectedServer server) {
		final List<ScanFilter> filters = localFilters.get(new NamedServer(
				server.getName(), server.getUuid()));
		if (!showFiltersOnServer || filters == null || filters.isEmpty()) {
			return null;
		}
		final ServersViewContent root = new ServersViewContent(serverNode,
				SLImages.getImage(CommonImages.IMG_FILTER));
		root.setText(SCAN_FILTERS);

		final ServersViewContent filterRoot = root;
		final List<ServersViewContent> children = new ArrayList<ServersViewContent>();
		for (final ScanFilter f : filters) {
			ServersViewContent filter;
			if (showFiltersOnServer) {
				filter = createLabel(filterRoot, children, f.getName(),
						ChangeStatus.NONE);
				filter.setData(f);
			}
		}
		filterRoot.setChildren(children.toArray(emptyChildren));
		return root;
	}

	private void createUnassociatedProjectItems(
			final List<ServersViewContent> content) {
		List<ServersViewContent> children = null;
		ServersViewContent parent = null;

		for (final ProjectStatus ps : projects) {
			final ConnectedServer server = f_manager.getServer(ps.name);
			if (server == null) {
				if (parent == null) {
					parent = new ServersViewContent(null, SLImages
							.getImage(CommonImages.IMG_CONSOLE));
					children = new ArrayList<ServersViewContent>();
				}
				final ServersViewContent project = new ServersViewContent(
						parent, SLImages.getImage(CommonImages.IMG_PROJECT));
				children.add(project);
				initProjectItem(project, server, ps);
			}
		}
		if (parent != null) {
			parent.setChildren(children.toArray(emptyChildren));
			parent.setText(parent.getChangeStatus().getLabel() + "Unconnected");
			content.add(parent);
		}
	}

	@SuppressWarnings("unused")
	private void createLocalScanFilterItems(
			final List<ServersViewContent> content) {
		final ServersViewContent filterRoot = new ServersViewContent(null,
				SLImages.getImage(CommonImages.IMG_FILTER));
		content.add(filterRoot);

		final List<ServersViewContent> children = new ArrayList<ServersViewContent>();
		for (final Map.Entry<NamedServer, List<ScanFilter>> e : localFilters
				.entrySet()) {
			final NamedServer server = e.getKey();
			for (final ScanFilter f : e.getValue()) {
				final String name = f.getName() + " (" + server.getName() + ")";
				final ServersViewContent filter = createLabel(filterRoot,
						children, name, ChangeStatus.NONE);
				filter.setData(f);
			}
		}
		filterRoot.setText("Known Scan Filters");
		filterRoot.setChildren(children.toArray(emptyChildren));
	}

	private void createProjectItems(final ServersViewContent parent,
			final ConnectedServer server) {
		final List<ServersViewContent> content = new ArrayList<ServersViewContent>();

		for (final String projectName : f_manager
				.getProjectsConnectedTo(server)) {
			ProjectStatus s = null;
			for (final ProjectStatus p : projects) {
				if (projectName.equals(p.name)) {
					s = p;
					break;
				}
			}
			if (s == null) {
				final IJavaProject jp = JDTUtility.getJavaProject(projectName);
				if (jp != null) {
					// No scan data?
					final ServersViewContent root = createProjectItem(parent,
							server, projectName);
					root.setData(new ProjectStatus(jp));
					content.add(root);

					createLabel(root, "Needs a local scan");
					continue;
				} else { // closed project?
					final IProject p = WorkspaceUtility.getProject(projectName);
					if ((p != null) && p.exists()) {
						if (p.isOpen()) {
							throw new IllegalStateException(
									"Not a Java project: " + projectName);
						} else { // closed
							final ServersViewContent root = createProjectItem(
									parent, server, projectName);
							content.add(root);

							createLabel(root, "Closed ... no info available");
							continue;
						}
					}
					throw new IllegalStateException("No such Java project: "
							+ projectName);
				}
			}
			final ServersViewContent root = new ServersViewContent(parent,
					SLImages.getImage(CommonImages.IMG_PROJECT));
			initProjectItem(root, server, s);
			content.add(root);
		}
		parent.setChildren(content.toArray(emptyChildren));
	}

	private ServersViewContent createLabel(final ServersViewContent parent,
			final String text) {
		return createLabel(parent, text, ChangeStatus.NONE);
	}

	private ServersViewContent createLabel(final ServersViewContent parent,
			final String text, final ChangeStatus delta) {
		final ServersViewContent[] contents = new ServersViewContent[1];
		final ServersViewContent c = new ServersViewContent(parent, null);
		c.setText(text);
		c.setChangeStatus(delta);
		contents[0] = c;
		parent.setChildren(contents);
		return c;
	}

	private ServersViewContent createLabel(final ServersViewContent parent,
			final List<ServersViewContent> children, final String text,
			final ChangeStatus delta) {
		final ServersViewContent c = createLabel(parent, children, text);
		c.setChangeStatus(delta);
		return c;
	}

	private ServersViewContent createLabel(final ServersViewContent parent,
			final List<ServersViewContent> children, final String text) {
		final ServersViewContent c = new ServersViewContent(parent, null);
		c.setText(text);
		children.add(c);
		return c;
	}

	private ServersViewContent createProjectItem(
			final ServersViewContent parent, final ConnectedServer server,
			final String projectName) {
		final ServersViewContent root = new ServersViewContent(parent, SLImages
				.getImage(CommonImages.IMG_PROJECT));
		root.setText(projectName + " [" + server.getName() + ']');
		return root;
	}

	private void initProjectItem(final ServersViewContent root,
			final ConnectedServer server, final ProjectStatus ps) {
		final List<ServersViewContent> contents = new ArrayList<ServersViewContent>();
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");

		if ((ps.scanDoc != null) && ps.scanDoc.exists()) {
			ServersViewContent scan;
			if (ps.scanInfo != null) {
				final Date lastScanTime = ps.scanInfo.getScanTime();

				if (ps.scanInfo.isPartial()) {
					// Latest is a re-scan
					scan = new ServersViewContent(root, SLImages
							.getImage(CommonImages.IMG_SIERRA_INVESTIGATE));
					scan.setText("Re-scan done locally on "
							+ dateFormat.format(lastScanTime)
							+ " ... click to start full scan");
				} else {
					scan = new ServersViewContent(root, SLImages
							.getImage(CommonImages.IMG_SIERRA_SCAN));
					scan.setText("Last full scan done locally on "
							+ dateFormat.format(lastScanTime));
				}
				scan.setData(ps.scanInfo);

			} else {
				final Date docModified = new Date(ps.scanDoc.lastModified());
				scan = new ServersViewContent(root, SLImages
						.getImage(CommonImages.IMG_SIERRA_SCAN));
				scan.setText("Last full scan done locally on "
						+ dateFormat.format(docModified));
				scan.setData(ps.scanDoc);
			}
			contents.add(scan);
			// status = status.merge(ChangeStatus.LOCAL);
		}

		if (ps.numLocalAudits > 0) {
			final ServersViewContent audits = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_SIERRA_STAMP));
			contents.add(audits);

			final List<ServersViewContent> auditContents = new ArrayList<ServersViewContent>();
			createAuditItems(audits, auditContents, ps.numLocalAudits);
			audits.setChildren(auditContents.toArray(emptyChildren));
			audits.setChangeStatus(ChangeStatus.LOCAL);
		}

		if (ps.numServerProblems > 0) {
			final ServersViewContent problems = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_WARNING));
			contents.add(problems);
			problems.setText(ps.numServerProblems + " consecutive failure"
					+ s(ps.numServerProblems) + " connecting to "
					+ server.getName());
			problems.setServerStatus(ServerStatus.WARNING);
		}
		if (server != null && ps.numProjectProblems > 0) {
			final ServersViewContent problems = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_WARNING));
			contents.add(problems);
			problems.setText(ps.numProjectProblems + " consecutive failure"
					+ s(ps.numProjectProblems) + " getting server info from "
					+ server.getName());
			problems.setServerStatus(ServerStatus.WARNING);
		}
		/*
		 * if (ps.localDBInfo != null) { final ServersViewContent scanFilter =
		 * new ServersViewContent(root,
		 * SLImages.getImage(CommonImages.IMG_FILTER));
		 * contents.add(scanFilter); scanFilter .setText("Scan filter: " +
		 * ps.localDBInfo.getScanFilter()); scanFilter.setData(ps.localDBInfo);
		 * }
		 */
		if (ps.filterName != null) {
			final ServersViewContent scanFilter = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_FILTER));
			contents.add(scanFilter);
			scanFilter.setText("Scan filter: " + ps.filterName);
			scanFilter.setData(ps.filterName);
		}

		// Also sets status
		root.setChildren(contents.toArray(emptyChildren));
		final String label = root.getChangeStatus().getLabel();
		if (server != null) {
			root.setText(label + ps.name + " [" + server.getName() + ']');
		} else {
			root.setText(label + ps.name);
		}
		// setAllDataIfNull(root, ps);

		root.setData(ps);
	}

	/*
	 * private void setAllDataIfNull(TreeItem root, ProjectStatus ps) { if (root
	 * != null) { if (root.getData() == null) { root.setData(ps); } for(TreeItem
	 * item : root.getItems()) { setAllDataIfNull(item, ps); } } }
	 */

	private ServersViewContent createAuditItems(
			final ServersViewContent audits,
			final List<ServersViewContent> contents, final int numAudits) {
		audits.setText("> " + numAudits + " local audit" + s(numAudits));
		return audits;
	}

	/**
	 * Show by Project
	 */
	private ServersViewContent[] createProjectItems() {
		final ServersViewContent[] content = new ServersViewContent[projects
				.size()];
		int i = 0;
		for (final ProjectStatus ps : projects) {
			final ConnectedServer server = f_manager.getServer(ps.name);
			content[i] = new ServersViewContent(null, SLImages
					.getImage(CommonImages.IMG_PROJECT));
			initProjectItem(content[i], server, ps);
			i++;
		}
		return content;
	}

	private static String s(final int num) {
		return num <= 1 ? "" : "s";
	}

	static final ServersViewContent[] emptyChildren = new ServersViewContent[0];

	void addNothingSelected_MenuItems(final Menu contextMenu) {
		final MenuItem newServerItem = AbstractSierraView.createMenuItem(
				contextMenu, "New...", SLImages
						.getImage(CommonImages.IMG_EDIT_NEW));
		newServerItem.addListener(SWT.Selection, f_newServerAction);
	}

	void addServerMenuItems(final Menu contextMenu,
			final AutoSyncType syncType, final boolean enableConnect,
			final boolean enableSendFilters) {

		final MenuItem browseServerItem = AbstractSierraView.createMenuItem(
				contextMenu, "Browse", SLImages
						.getImage(CommonImages.IMG_SIERRA_SERVER));

		final MenuItem deleteServerItem = AbstractSierraView.createMenuItem(
				contextMenu, "Delete", SLImages
						.getImage(CommonImages.IMG_EDIT_DELETE));

		browseServerItem.addListener(SWT.Selection, f_openInBrowserAction);
		deleteServerItem.addListener(SWT.Selection, f_deleteServerAction);

		new MenuItem(contextMenu, SWT.SEPARATOR);

		if (syncType.areAllSame()) {
			final MenuItem toggleAutosyncItem = new MenuItem(contextMenu,
					SWT.CHECK);
			toggleAutosyncItem.setText("Use Automatic Synchronization");
			toggleAutosyncItem.addListener(SWT.Selection,
					f_toggleAutoSyncAction);
			toggleAutosyncItem.setSelection(syncType == AutoSyncType.ON);
		}
		if (enableConnect) {
			final MenuItem serverConnectItem = AbstractSierraView
					.createMenuItem(contextMenu, "Connect Projects...",
							CommonImages.IMG_SIERRA_SERVER);
			serverConnectItem.addListener(SWT.Selection, f_serverConnectAction);
		}
		if (enableSendFilters) {
			addSendLocalFilter_MenuItem(contextMenu);
		}

		final MenuItem serverPropertiesItem = new MenuItem(contextMenu,
				SWT.PUSH);
		serverPropertiesItem.setText("Properties...");
		serverPropertiesItem.addListener(SWT.Selection,
				f_serverPropertiesAction);
	}

	void addSendLocalFilter_MenuItem(final Menu contextMenu) {
		final MenuItem sendResultFilters = AbstractSierraView.createMenuItem(
				contextMenu, "Send Local Scan Filter As...",
				CommonImages.IMG_FILTER);
		sendResultFilters.addListener(SWT.Selection, f_sendResultFiltersAction);
	}

	void addProjectMenuItems(final Menu contextMenu, final boolean allHasScans,
			final boolean allConnected) {
		if (!allConnected) {
			final MenuItem serverConnectItem = AbstractSierraView
					.createMenuItem(contextMenu, "Connect Projects...",
							CommonImages.IMG_SIERRA_SERVER);
			serverConnectItem.addListener(SWT.Selection, f_serverConnectAction);

			new MenuItem(contextMenu, SWT.SEPARATOR);
		}
		final MenuItem scanProjectItem = AbstractSierraView.createMenuItem(
				contextMenu, "Scan Project", CommonImages.IMG_SIERRA_SCAN);
		final MenuItem rescanProjectItem = AbstractSierraView.createMenuItem(
				contextMenu, "Re-Scan Changes in Project",
				CommonImages.IMG_SIERRA_SCAN_DELTA);
		if (allConnected) {
			final MenuItem synchProjects = AbstractSierraView.createMenuItem(
					contextMenu, "Synchronize Project",
					CommonImages.IMG_SIERRA_SYNC);
			synchProjects.addListener(SWT.Selection,
					f_synchConnectedProjectsAction);
		}
		scanProjectItem.addListener(SWT.Selection, f_scanProjectAction);
		rescanProjectItem.addListener(SWT.Selection, f_rescanProjectAction);

		if (allConnected) {
			new MenuItem(contextMenu, SWT.SEPARATOR);

			if (allHasScans) {
				final MenuItem publishScansItem = AbstractSierraView
						.createMenuItem(contextMenu, "Publish Latest Scan",
								CommonImages.IMG_SIERRA_PUBLISH);
				publishScansItem.addListener(SWT.Selection,
						f_publishScansAction);
			}
			final MenuItem disconnectProjectItem = AbstractSierraView
					.createMenuItem(contextMenu, "Disconnect",
							CommonImages.IMG_SIERRA_DISCONNECT);

			disconnectProjectItem.addListener(SWT.Selection,
					f_disconnectProjectAction);
		}
	}

	void addScanFilterMenuItems(final Menu contextMenu) {
		final MenuItem getResultFilters = AbstractSierraView.createMenuItem(
				contextMenu, "Copy to Local Scan Filter...", (Image) null);
		getResultFilters.addListener(SWT.Selection, f_getResultFiltersAction);
	}

	private class ContentProvider implements ITreeContentProvider {
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof ServersViewContent) {
				final ServersViewContent[] children = ((ServersViewContent) parentElement)
						.getChildren();
				return children != null ? children : emptyChildren;
			}
			return null;
		}

		public Object getParent(final Object element) {
			if (element instanceof ServersViewContent) {
				return ((ServersViewContent) element).parent;
			}
			return null;
		}

		public boolean hasChildren(final Object element) {
			if (element instanceof ServersViewContent) {
				final ServersViewContent[] children = ((ServersViewContent) element)
						.getChildren();
				return (children != null) && (children.length > 0);
			}
			return false;
		}

		public Object[] getElements(final Object inputElement) {
			final TreeInput input = (TreeInput) inputElement;
			return input.content;
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput) {
			// TODO Auto-generated method stub
		}
	}

	private class LabelProvider implements ILabelProvider {
		final ILabelDecorator decorator = PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator();

		public Image getImage(final Object element) {
			if (element instanceof ServersViewContent) {
				// System.out.println("Getting image for "+element);
				final Image i1 = ((ServersViewContent) element).getImage();
				final Image i2 = decorator.decorateImage(i1, element);
				return i2 == null ? i1 : i2;
			}
			return null;
		}

		public String getText(final Object element) {
			if (element instanceof ServersViewContent) {
				return ((ServersViewContent) element).getText();
			}
			return null;
		}

		public void addListener(final ILabelProviderListener listener) {
			decorator.addListener(listener);
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public boolean isLabelProperty(final Object element,
				final String property) {
			// TODO Auto-generated method stub
			return true;
		}

		public void removeListener(final ILabelProviderListener listener) {
			decorator.addListener(listener);
		}

		/*
		 * public void labelProviderChanged(LabelProviderChangedEvent event) {
		 * for(Object o : event.getElements()) { if (o instanceof
		 * ServersViewContent) { System.out.println("Label changed for "+o); } }
		 * //f_statusTree.refresh(); }
		 */
	}
}
