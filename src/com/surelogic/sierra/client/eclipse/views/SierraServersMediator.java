package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.ui.dialogs.PreferencesUtil;

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
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.NewScan;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;
import com.surelogic.sierra.client.eclipse.actions.PublishScanAction;
import com.surelogic.sierra.client.eclipse.actions.ScanChangedProjectsAction;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeAllProjectsAction;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectAction;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootException;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongServer;
import com.surelogic.sierra.client.eclipse.dialogs.ConnectProjectsDialog;
import com.surelogic.sierra.client.eclipse.dialogs.PromptForFilterNameDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
import com.surelogic.sierra.client.eclipse.jobs.AbstractServerProjectJob;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.jobs.OverwriteLocalScanFilterJob;
import com.surelogic.sierra.client.eclipse.jobs.SendScanFiltersJob;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.client.eclipse.wizards.ServerExportWizard;
import com.surelogic.sierra.client.eclipse.wizards.ServerImportWizard;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingAudits;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.Scans;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterView;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.ServerScanFilterInfo;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.ListScanFilterRequest;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.message.SyncTrailResponse;

public final class SierraServersMediator extends AbstractSierraViewMediator
		implements ISierraServerObserver, IProjectsObserver {
	private static final boolean showFiltersOnServer = true;
	
	static final String SCAN_FILTERS = "Scan Filters";

	static final String CATEGORIES = "Categories";

	static final String CONNECTED_PROJECTS = "Connected Projects";

	private static final String NO_SERVER_DATA = "Needs to grab from server";

	/**
	 * This should only be changed in the UI thread
	 */
	private List<ProjectStatus> projects = Collections.emptyList();
	
	/**
	 * This should only be changed in the UI thread
	 */
	private Map<String, List<ScanFilter>> localFilters = Collections.emptyMap();

	/**
	 * This should only be changed in the UI thread
	 */
	private Map<ConnectedServer, ServerUpdateStatus> serverUpdates = Collections
			.emptyMap();

	/**
	 * A map from a project to the server response
	 * 
	 * Protected by itself This should only be accessed in a database job
	 * (possibly from multiple threads)
	 */
	private final Map<String, List<SyncTrailResponse>> responseMap = new HashMap<String, List<SyncTrailResponse>>();

	private final Map<String, ProjectDO> projectMap = new HashMap<String, ProjectDO>();

	/**
	 * Used in a similar way as responseMap
	 */
	private final Map<ConnectedServer, ServerUpdateStatus> serverResponseMap = new HashMap<ConnectedServer, ServerUpdateStatus>();

	private final TreeViewer f_statusTree;
	private final Menu f_contextMenu;
	private final ActionListener f_buglinkSyncAction;
	private final ActionListener f_serverSyncAction;
	private final ActionListener f_toggleAutoSyncAction;
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
			final List<ConnectedServer> servers = collectServers().indirect;
			if (servers.size() == 1) {
				handleEventOnServer(servers.get(0));
				/*
			}
			final ConnectedServer server = f_manager.getFocus();
			if (server != null) {
				handleEventOnServer(server);
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
		ScanFilterActionListener(String tooltip) {
			super((Image) null, tooltip);
		}

		@Override
		public final void run() {
			for(ScanFilter f : collectScanFilters()) {
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
			run(server, ConnectedServerManager.getInstance().getProjectsConnectedTo(server));
		}

		protected abstract void run(ConnectedServer server,
				List<String> projectNames);
	}

	private final ConnectedServerManager f_manager = ConnectedServerManager
			.getInstance();

	public SierraServersMediator(final SierraServersView view,
			final TreeViewer statusTree, final Menu contextMenu,
			final MenuItem newServerItem, final MenuItem browseServerItem,
			final MenuItem duplicateServerItem,
			final MenuItem deleteServerItem, final MenuItem serverConnectItem,
			final MenuItem synchConnectedProjects,
			final MenuItem sendResultFilters, final MenuItem getResultFilters,
			final MenuItem serverPropertiesItem,
			final MenuItem scanProjectItem, final MenuItem rescanProjectItem,
			final MenuItem publishScansItem,
			final MenuItem disconnectProjectItem) {
		super(view);

		f_statusTree = statusTree;
		f_statusTree.setContentProvider(new ContentProvider());
		f_statusTree.setLabelProvider(new LabelProvider());

		f_contextMenu = contextMenu;
		f_toggleAutoSyncAction = new ServerActionListener("Toggle Auto-sync",
				"Toggle whether the server(s) automatically synchronize",
				"No server to toggle") {
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				ConnectedServerManager.getInstance().setAutoSyncFor(server, !server.getLocation().isAutoSync());
				ConnectedServerManager.getInstance().notifyObservers();
			}
		};		
		
		f_buglinkSyncAction = new ActionListener("Synchronize All BugLink Data",
		"Synchronize BugLink servers") {
			@Override
			public void run() {
				SierraServersAutoSync.asyncSyncWithServer(ServerSyncType.BUGLINK);
			}
		};
		f_serverSyncAction = new ActionListener("Synchronize All Connected Projects",
				"Synchronize servers and connected projects") {
			@Override
			public void run() {
				SierraServersAutoSync.asyncSyncWithServer(ServerSyncType.ALL);
			}
		};
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
		f_openInBrowserAction.setEnabled(false);

		f_duplicateServerAction = new ServerActionListener(SLImages
				.getImage(CommonImages.IMG_EDIT_COPY),
				"Duplicates the selected team server location",
				"No server to duplicate") {
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				// FIX no duplicate?
				// f_manager.duplicate();
			}
		};
		f_duplicateServerAction.setEnabled(false);

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

		f_deleteServerAction.setEnabled(false);

		/*
		 * ActionListener registerAction = new
		 * ActionListener(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO),
		 * "Register your copy of SLIC") { @Override public void run() {
		 * SierraServerLocation loc = f_manager.getFocus().getServer();
		 * Registration r = RegistrationClient.create(loc);
		 * ProductRegistrationInfo info = new ProductRegistrationInfo();
		 * info.setName("SLIC"); info.setVersion("2.2");
		 * info.setFirstName("Edwin"); info.setLastName("Chan");
		 * 
		 * RegistrationResponse rr = r.register(info);
		 * System.out.println(rr.getMessage()); BalloonUtility.showMessage("A
		 * message from SureLogic", rr.getMessage()); } };
		 * view.addToActionBar(registerAction);
		 */
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
		final Action importAction = new Action("Import Locations...") {
			@Override
			public void run() {
				final ServerImportWizard wizard = new ServerImportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				final WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		f_view.addToViewMenu(importAction);
		final Action exportAction = new Action("Export Locations...") {
			@Override
			public void run() {
				final ServerExportWizard wizard = new ServerExportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				final WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		f_view.addToViewMenu(exportAction);
		f_view.addToViewMenu(new Separator());

		final Action serverInteractionAction = new Action(
				"Server Interaction Preferences ...", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				final PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(null,
								PreferencesAction.SERVER_INTERACTION_ID,
								PreferencesAction.FILTER, null);
				dialog.open();
			}
		};
		f_view.addToViewMenu(serverInteractionAction);
		f_view.addToViewMenu(f_serverSyncAction);
		//f_view.addToViewMenu(f_buglinkSyncAction);
		//f_view.addToViewMenu(f_serverUpdateAction);
		f_view.addToViewMenu(f_toggleAutoSyncAction);
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

		// f_serverList.addListener(SWT.MouseDoubleClick, openInBrowserAction);

		f_newServerItem.addListener(SWT.Selection, f_newServerAction);
		f_browseServerItem.addListener(SWT.Selection, f_openInBrowserAction);
		f_duplicateServerItem.addListener(SWT.Selection,
				f_duplicateServerAction);
		f_deleteServerItem.addListener(SWT.Selection, f_deleteServerAction);

		final Listener connectAction = 
			new ServerActionListener("No server to connect to, or no project to connect") {
			boolean f_syncAfterConnect = true;
			
			private List<IJavaProject> collectProjects() {
				ProjectStatusCollector<IJavaProject> projects = new ProjectStatusCollector<IJavaProject>() {
					@Override IJavaProject getSelectedInfo(ProjectStatus s) {
						return s.project;
					}
				};
				return projects.collectSelectedProjects((IStructuredSelection) f_statusTree.getSelection());
			}
			private void doConnect(final ConnectedServer server, List<IJavaProject> projects) {
				for (IJavaProject project : projects) {
					if (f_manager.isConnected(project.getElementName())) {
						continue;
					}
					f_manager.connect(project.getElementName(), server);
				}
			}			
			@Override
			protected void handleEventOnServer(final ConnectedServer server) {
				List<IJavaProject> projects = collectProjects();
				if (!projects.isEmpty()) {
					boolean syncAfterConnect = true;
					final MessageDialog dialog = 
						new MessageDialog(f_statusTree.getTree().getShell(),
							              "Synchronize Project(s) on Connect", null,
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
				List<IJavaProject> projects = collectProjects();
				if (projects.isEmpty()) {
					super.handleEventWithoutServer();
				} else {
					ServerSelectionDialog dialog = new ServerSelectionDialog(
							f_statusTree.getTree().getShell(), projects.get(0).getElementName()) {
						@Override
						protected void addToEntryPanel(Composite entryPanel) {
							super.addToEntryPanel(entryPanel);
							
							final Button syncToggle = new Button(entryPanel, SWT.CHECK);
							syncToggle.setText("Synchronize newly connected projects on finish");
							syncToggle.setSelection(true);
							syncToggle.addListener(SWT.Selection, new Listener() {
								public void handleEvent(Event event) {
									f_syncAfterConnect = syncToggle.getSelection();
								}
							});
						}
					};
					dialog.setUseForAllUnconnectedProjects(true);
					if (dialog.open() == Window.CANCEL) {
						return;
					}
					ConnectedServer server = dialog.getServer();
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

		f_serverConnectItem.addListener(SWT.Selection, connectAction);

		f_synchConnectedProjects.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(final List<IJavaProject> projects) {
						new SynchronizeProjectAction().run(projects);
					}

				});

		f_sendResultFilters
				.addListener(
						SWT.Selection,
						new ServerActionListener(
								"Send local scan filter  pressed with no server focus.") {

							@Override
							protected void handleEventOnServer(
									final ConnectedServer server) {
								final String msg = "What do you want to call your scan filter "
										+ "on the Sierra server '"
										+ server.getName() + "'";
								final PromptForFilterNameDialog dialog = new PromptForFilterNameDialog(
										f_statusTree.getTree().getShell(), msg);
								if (dialog.open() == 0) {
									/*
									 * Yes was selected, so send the local scan
									 * filters to the server.
									 */
									if (SendScanFiltersJob.ENABLED) {
										final Job job = new SendScanFiltersJob(
												ServerFailureReport.SHOW_DIALOG,
												server, dialog.getText());
										job.schedule();
									}
								}
							}
						});

		f_getResultFilters
				.addListener(
						SWT.Selection,
						new ScanFilterActionListener("Overwrite local scan filter") {
							@Override
							protected void handleEventOnFilter(final ScanFilter f) {
								final String msg = "Do you want to overwrite your local scan filter with"
										+ " the scan filter '" + f.getName() + "'?";
								final MessageDialog dialog = new MessageDialog(
										f_statusTree.getTree().getShell(),
										"Overwrite Local Scan Filter", null,
										msg, MessageDialog.QUESTION,
										new String[] { "Yes", "No" }, 0);
								if (dialog.open() == 0) {
									/*
									 * Yes was selected, so get the result
									 * filters from the server.
									 */

									final Job job = new OverwriteLocalScanFilterJob(f);
									job.schedule();
								}
							}
						});

		f_serverPropertiesItem.addListener(SWT.Selection,
				new ServerActionListener(
						"Edit server pressed with no server focus.") {
					@Override
					protected void handleEventOnServer(final ConnectedServer server) {
						ServerLocationDialog.editServer(f_statusTree.getTree()
								.getShell(), server);
					}
				});

		f_scanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(final List<IJavaProject> projects) {
						new NewScan().scan(projects);
					}
				});
		f_rescanProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(final List<IJavaProject> projects) {
						new ScanChangedProjectsAction().run(projects);
					}
				});
		f_publishScansItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(final List<IJavaProject> projects) {
						// FIX check for projects w/o scans?
						new PublishScanAction().run(projects);
					}
				});
		f_disconnectProjectItem.addListener(SWT.Selection,
				new ProjectsActionListener() {
					@Override
					protected void run(final List<IJavaProject> projects) {
						final List<String> projectNames = new ArrayList<String>();
						for (final IJavaProject p : projects) {
							projectNames.add(p.getElementName());
						}
						DeleteProjectDataJob.utility(projectNames, null, true);
					}
				});

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

		f_contextMenu.addListener(SWT.Show, new Listener() {
			public void handleEvent(final Event event) {
				f_newServerItem.setEnabled(true);

				final SelectedServers servers = collectServers();
				final boolean onlyServer = servers.indirect.size() == 1;
				final boolean onlyTeamServer;
				final boolean onlyBugLink;
				if (onlyServer) {
					final ConnectedServer focus = servers.indirect.get(0);
					onlyTeamServer = focus.isTeamServer();
					onlyBugLink = focus != null;
				} else {
					onlyTeamServer = onlyBugLink = false;
				}			
				f_duplicateServerAction.setEnabled(onlyServer);
				f_deleteServerAction.setEnabled(onlyServer);
				f_openInBrowserAction.setEnabled(onlyServer);
				f_browseServerItem.setEnabled(onlyServer);
				f_duplicateServerItem.setEnabled(onlyServer);
				f_deleteServerItem.setEnabled(onlyServer);
				if (SendScanFiltersJob.ENABLED) {
					f_sendResultFilters.setEnabled(onlyBugLink && onlyServer);
				} else {
					f_sendResultFilters.setEnabled(false);
				}
				f_serverPropertiesItem.setEnabled(onlyServer);

				final List<ScanFilter> filters = collectScanFilters();
				final boolean onlyScanFilter = filters.size() == 1;
				f_getResultFilters.setEnabled(onlyScanFilter);

				final List<ProjectStatus> status = collectSelectedProjectStatus();
				final boolean someProjects = !status.isEmpty();
				boolean allConnected = someProjects;
				boolean allHasScans = someProjects;
				if (someProjects) {
					// try {
					for (final ProjectStatus ps : status) {
						if ((f_manager == null) || (ps == null)) {
							//LOG.severe("Null project status");
							continue;
						}
						if (!f_manager.isConnected(ps.name)) {
							allConnected = false;
						}
						if (ps.scanDoc == null || !ps.scanDoc.exists()) {
							allHasScans = false;
						} else if (ps.scanInfo != null
								&& ps.scanInfo.isPartial()) {
							allHasScans = false;
						}
					}
					/*
					 * } catch (Throwable t) { t.printStackTrace(); }
					 */
				}
				f_serverConnectItem.setEnabled((onlyTeamServer && !servers.direct.isEmpty()) || 
						                       (someProjects && !allConnected));
				f_scanProjectItem.setEnabled(someProjects);
				f_rescanProjectItem.setEnabled(someProjects);
				f_synchConnectedProjects.setEnabled(someProjects);
				f_publishScansItem.setEnabled(allHasScans);
				f_disconnectProjectItem.setEnabled(allConnected);
			}
		});

		/*
		final AutoJob doServerAutoUpdate = new AutoJob("Server auto-update",
				lastServerUpdateTime) {
			@Override
			protected boolean isEnabled() {
				return PreferenceConstants.getServerInteractionSetting()
						.doServerAutoUpdate();
			}

			@Override
			protected long getDelay() {
				return PreferenceConstants
						.getServerInteractionPeriodInMinutes() * 60000;
			}

			@Override
			protected void run() {
				asyncUpdateServerInfo();
			}
		};
		doServerAutoUpdate.schedule(doServerAutoUpdate.getDelay());
        */

//		final IPreferenceStore store = Activator.getDefault()
//				.getPreferenceStore();
//		store.addPropertyChangeListener(new IPropertyChangeListener() {
//			public void propertyChange(final PropertyChangeEvent event) {
//				if (event.getProperty() == PreferenceConstants.P_SERVER_INTERACTION_SETTING) {
//					if (event.getNewValue() != event.getOldValue()) {
//						final ServerInteractionSetting s = ServerInteractionSetting
//								.valueOf((String) event.getNewValue());
//						final Job job = new DatabaseJob(
//								"Switching server interaction") {
//							@Override
//							protected IStatus run(final IProgressMonitor monitor) {
//								synchronized (responseMap) {
//									if (s.useAuditThreshold()
//											&& checkAutoSyncTrigger(projects)) {
//										asyncSyncWithServer(ServerSyncType.BY_SERVER_SETTINGS);
//									} else if (s.doServerAutoSync()) {
//										asyncSyncWithServer(ServerSyncType.BY_SERVER_SETTINGS);
//									} else if (s.doServerAutoUpdate()) {
//										asyncUpdateServerInfo();
//									}
//								}
//								return Status.OK_STATUS;
//							}
//						};
//						job.schedule();
//					}
//				}
//			}
//
//		});
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
		
		SelectedServers(boolean allocate) {
			if (allocate) {
				direct = new ArrayList<ConnectedServer>();
				indirect = new ArrayList<ConnectedServer>();
			} else {
				direct = indirect = Collections.emptyList();
			}
		}
	}
	
	SelectedServers collectServers() {
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		if (si.size() == 0) {
			return new SelectedServers(false);
		}
		final SelectedServers servers = new SelectedServers(true);
		@SuppressWarnings("unchecked")
		final Iterator it = si.iterator();
		while (it.hasNext()) {
			final ServersViewContent item = (ServersViewContent) it.next();
			if (item.getData() instanceof ConnectedServer) {
				ConnectedServer s = (ConnectedServer) item.getData();
				if (servers.indirect.contains(s)) {
					continue;
				}
				servers.direct.add(s);
				servers.indirect.add(s);
			} else {
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
			Object data = item.getData();
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
			if (data == null && SCAN_FILTERS.equals(item.getText())) {
				// Not a server
				return null; 
			}
			*/
			item = item.getParent();
		}
		return null;
	}

	private List<ProjectStatus> collectSelectedProjectStatus() {
		final IStructuredSelection si = (IStructuredSelection) f_statusTree
				.getSelection();
		ProjectStatusCollector<ProjectStatus> collector = new ProjectStatusCollector<ProjectStatus>() {
			@Override
			ProjectStatus getSelectedInfo(ProjectStatus s) {
				return s;
			}
		};
		return collector.collectSelectedProjects(si);
	}

	private abstract class ProjectStatusCollector<T> {
		private void add(List<T> projects, ProjectStatus s) {
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
				} else if ("Unconnected".equals(item.getText())) {
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
				final ServersViewContent parent, boolean server) {
			final ServersViewContent projectItems;
			if (server) {
				if (parent.getChildren().length == 0) {
					return; // No children
				}
				// look for CONNECTED_PROJECTS (if a server)
				ServersViewContent temp = parent.getChildren()[0];
				for(ServersViewContent c : parent.getChildren()) {
					if (c.getText().endsWith(CONNECTED_PROJECTS)) {
						temp = c;
						break;
					}
				}				
				projectItems = temp;
			} else {
				projectItems = parent;
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
		IJavaProject getSelectedInfo(ProjectStatus s) {
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
			final URL url = server.getLocation().toAuthorizedURL();
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
			updateContentsInUI(projects, serverUpdates, localFilters);
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
	void asyncUpdateServerInfo() {
		final long now = System.currentTimeMillis();
		lastServerUpdateTime.set(now);
		System.out.println("Update at: " + now);

		final Job lastJob = lastUpdateJob.get();
		if ((lastJob == null) || (lastJob.getResult() != null)) {
			final Job job = new DatabaseJob("Updating server status") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask("Updating server info",
							IProgressMonitor.UNKNOWN);
					try {
						updateServerInfo();
					} catch (final Exception e) {
						final int errNo = 58; // FIX
						final String msg = I18N.err(errNo);
						return SLEclipseStatusUtility.createErrorStatus(errNo,
								msg, e);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			lastUpdateJob.set(job);
			job.schedule();
		}
	}
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
		/*
		final Job infoJob = new Job("Getting server info") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final int threshold = PreferenceConstants
						.getServerInteractionRetryThreshold();
				final ConnectedServerManager mgr = ConnectedServerManager
						.getInstance();
				for (final ConnectedServer s : mgr.getServers()) {
					if (mgr.getStats(s).getProblemCount() <= threshold) {
						s.updateServerInfo();
					}
				}
				return Status.OK_STATUS;
			}

		};
		infoJob.setSystem(true);
		infoJob.schedule();
		*/
		
		final Job job = new DatabaseJob("Updating project status") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
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

	private static boolean isFailedServer(Set<ConnectedServer> failedServers,
			ConnectedServer server, int numProblems) {
		if ((failedServers != null) && failedServers.contains(server)) {
			return true;
		}
		final int threshold = PreferenceConstants
				.getServerInteractionRetryThreshold();
		final int numServerProbs = 
			ConnectedServerManager.getInstance().getStats(server).getProblemCount();
		if (numServerProbs > threshold) {
			failedServers = markAsFailedServer(failedServers, server);
			return true;
		}
		if (numServerProbs + numProblems > threshold) {
			return true;
		}
		return false;
	}

	private class ServerHandler {
		final Connection c;
		final ClientProjectManager cpm;
		Set<ConnectedServer> failedServers = null;
		Set<ConnectedServer> connectedServers = new HashSet<ConnectedServer>();

		List<SyncTrailResponse> responses;
		ServerUpdateStatus serverResponse;

		ServerHandler(Connection c, ClientProjectManager cpm) {
			this.c = c;
			this.cpm = cpm;
		}

		private void init(ConnectedServer server) {
			responses = null;
			serverResponse = serverResponseMap.get(server);
		}

		void queryForProjects(ConnectedServerManager manager) {
			final int threshold = PreferenceConstants
					.getServerInteractionRetryThreshold();
			for (final IJavaProject jp : JDTUtility.getJavaProjects()) {
				final String name = jp.getElementName();
				final int numProblems = Projects.getInstance().getProblemCount(
						name);
				if (!Projects.getInstance().contains(name)
						|| (numProblems > threshold)) {
					continue; // Not scanned
				}

				// Check for new remote audits
				final ConnectedServer server = manager.getServer(name);
				init(server);
				queryServerForProject(server, name, numProblems);
			}
		}

		void queryServers(ConnectedServerManager manager) {
			for (ConnectedServer server : manager.getServers()) {
				if (connectedServers.contains(server)) {
					return; // Already handled
				}
				init(server);
				queryServer(server, null, 0, true);
			}
		}

		void queryServerForProject(ConnectedServer server, String name,
				int numProblems) {
			queryServer(server, name, numProblems, false);
		}

		void queryServer(ConnectedServer server, String name, int numProblems,
				boolean onlyServer) {
			if (server != null) {
				connectedServers.add(server);

				if (isFailedServer(failedServers, server, numProblems)) {
					return;
				}
				final SLProgressMonitor monitor = new NullSLProgressMonitor();

				// Try to distinguish server failure/disconnection and
				// RPC failure
				final ServerFailureReport method = PreferenceConstants
						.getServerFailureReporting();
				TroubleshootConnection tc;
				try {
					final ServerLocation loc = server.getLocation();
					if (!onlyServer) {
						responses = cpm.getProjectUpdates(server, name, monitor);
					}
					serverResponse = checkForBugLinkUpdates(c, serverResponse, loc);
				} catch (final ServerMismatchException e) {
					tc = new TroubleshootWrongServer(method, server, name);
					failedServers = handleServerProblem(failedServers, server,
							tc, e);
				} catch (final SierraServiceClientException e) {
					tc = AbstractServerProjectJob.getTroubleshootConnection(
							method, server, name, e);
					failedServers = handleServerProblem(failedServers, server,
							tc, e);
				} catch (final Exception e) {
					tc = new TroubleshootException(method, server, name, e,
							e instanceof SQLException);
					failedServers = handleServerProblem(failedServers, server,
							tc, e);
				}
			}
			if (!onlyServer && responses != null) {
				responseMap.put(name, responses);
				handleServerSuccess(server, name);
			}
			if (serverResponse != null) {
				serverResponseMap.put(server, serverResponse);
				ConnectedServerManager.getInstance().getStats(server).markAsConnected();
			}
		}
	}

	private void updateServerInfo() throws Exception {
		final Connection c = Data.getInstance().transactionConnection();
		Exception exc = null;
		try {
			final ClientProjectManager cpm = ClientProjectManager
					.getInstance(c);
			final ServerHandler handler = new ServerHandler(c, cpm);
			synchronized (responseMap) {
				responseMap.clear();
				serverResponseMap.clear();
				projectMap.clear();

				handler.queryForProjects(f_manager);
				handler.queryServers(f_manager);

				com.surelogic.sierra.jdbc.project.Projects projects = 
					new com.surelogic.sierra.jdbc.project.Projects(c);
				ScanFilters filters = new ScanFilters(c);
				// FIX to include server's label
				for (ProjectDO p : projects.listProjects()) {
					// Update to use scan filter's name, not uid										
					ScanFilterDO filter = filters.getScanFilter(p
							.getScanFilter());			
					if (filter != null) {
						p.setScanFilter(filter.getName());
					}
					projectMap.put(p.getName(), p);
				}
			}
			c.commit();

			asyncUpdateContents();
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

	private ServerUpdateStatus checkForBugLinkUpdates(final Connection c,
			ServerUpdateStatus serverResponse, final ServerLocation loc) {
		// See if we need to pick up BugLink data
		if (serverResponse == null) {
			final Query q = new ConnectionQuery(c);
			final ListCategoryResponse cr = SettingQueries.getNewCategories(
					loc, SettingQueries.categoryRequest().perform(q))
					.perform(q);
			final ServerScanFilterInfo sfr = SettingQueries.getNewScanFilters(
					loc, new ListScanFilterRequest()) // Need all
					.perform(q);
			serverResponse = new ServerUpdateStatus(cr, sfr);
		} else {
			// No need to update it again
			serverResponse = null;
		}
		return serverResponse;
	}

	private Set<ConnectedServer> handleServerProblem(
			Set<ConnectedServer> failedServers, final ConnectedServer server,
			final TroubleshootConnection tc, final Exception e) {
		if (handleServerProblem(tc, e)) {
			failedServers = markAsFailedServer(failedServers, server);
		}
		return failedServers;
	}

	private static Set<ConnectedServer> markAsFailedServer(
			Set<ConnectedServer> failedServers, final ConnectedServer server) {
		if (failedServers == null) {
			failedServers = new HashSet<ConnectedServer>();
		}
		failedServers.add(server);
		return failedServers;
	}

	/**
	 * Protected by responseMap
	 * 
	 * @param project
	 */
	private void handleServerSuccess(final ConnectedServer server,
			final String project) {
		// Contact was successful, so reset counts
		ConnectedServerManager.getInstance().getStats(server).markAsConnected();
		Projects.getInstance().markAsConnected(project);
	}

	/**
	 * Protected by responseMap
	 * 
	 * @return true if consider the server failed
	 */
	private boolean handleServerProblem(final TroubleshootConnection tc,
			final Exception e) {
		tc.fix();
		return tc.failServer();
	}

	private void updateContents() throws Exception {
		final Connection c = Data.getInstance().transactionConnection();
		Exception exc = null;
		try {
			final ClientProjectManager cpm = ClientProjectManager
					.getInstance(c);
			final ClientFindingManager cfm = cpm.getFindingManager();
			final Query q = new ConnectionQuery(c);
			final Scans sm = new Scans(q);
			final List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
			final Map<ConnectedServer, ServerUpdateStatus> serverUpdates;
			final Map<String, List<ScanFilter>> filters;
			synchronized (responseMap) {
				for (final IJavaProject jp : JDTUtility.getJavaProjects()) {
					final String name = jp.getElementName();
					if (!Projects.getInstance().contains(name)) {
						continue; // Not scanned
					}

					// Check for new local audits
					final List<FindingAudits> findings = cfm
							.getNewLocalAudits(name);

					// Check for new remote audits
					final List<SyncTrailResponse> responses = responseMap
							.get(name);
					final ConnectedServer server = f_manager.getServer(name);
					final int numServerProblems = server == null ? -1 : 
						ConnectedServerManager.getInstance().getStats(server).getProblemCount();
					final int numProjectProblems = Projects.getInstance()
							.getProblemCount(name);

					// FIX Check for a full scan (later than what's on the
					// server?)
					final File scan = NewScan.getScanDocumentFile(name);
					final ScanInfo info = sm.getLatestScanInfo(name);
					final ProjectDO dbInfo = projectMap.get(name);
					final ScanFilterView filter = SettingQueries.scanFilterForProject(name).perform(q);
					final ProjectStatus s = new ProjectStatus(jp, scan, info,
							findings, responses, numServerProblems,
							numProjectProblems, dbInfo, filter);
					projects.add(s);
				}
				serverUpdates = new HashMap<ConnectedServer, ServerUpdateStatus>(
						serverResponseMap);
				filters = SettingQueries.getLocalScanFilters().perform(q);
			}
			asyncUpdateContentsForUI(new IViewUpdater() {
				public void updateContentsForUI() {
					updateContentsInUI(projects, serverUpdates, filters);
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
			final Map<ConnectedServer, ServerUpdateStatus> serverUpdates,
			final Map<String, List<ScanFilter>> filters) {
		// No need to synchronize since only updated/viewed in UI thread?
		this.projects = projects;
		this.serverUpdates = serverUpdates;
		this.localFilters = filters;

		/*
		 * if (f_statusTree.isDisposed()) return;
		 * 
		 * f_statusTree.setRedraw(false);
		 */
		final List<ConnectedServer> servers = collectServers().indirect;
		final boolean onlyServer = servers.size() == 1;
		f_duplicateServerAction.setEnabled(onlyServer);
		f_deleteServerAction.setEnabled(onlyServer);
		f_openInBrowserAction.setEnabled(onlyServer);

		final TreeInput input = createTreeInput();
		f_statusTree.setInput(input);
		f_statusTree.getTree().getParent().layout();
		f_statusTree.expandToLevel(3);
		/*
		 * for(TreeItem item : f_statusTree.getItems()) {
		 * item.setExpanded(true); if (byServer) { for(TreeItem item2 :
		 * item.getItems()) { if (item2.getText().endsWith("Connected
		 * Projects")) { item2.setExpanded(true); // Expand projects
		 * for(TreeItem item3 : item2.getItems()) { item3.setExpanded(true); } }
		 * } } }
		 */
		// f_statusTree.setRedraw(true);
		checkAutoSyncTrigger(projects);
	}

	/**
	 * @return true if triggered an auto-sync
	 */
	private boolean checkAutoSyncTrigger(final List<ProjectStatus> projects) {
//		if (!PreferenceConstants.getServerInteractionSetting()
//				.useAuditThreshold()) {
//			return false;
//		}
		final int auditThreshold = PreferenceConstants
				.getServerInteractionAuditThreshold();
		if (auditThreshold > 0) {
			ConnectedServerManager manager = ConnectedServerManager.getInstance();
			int audits = 0;
			for (final ProjectStatus ps : projects) {
				ConnectedServer server = manager.getServer(ps.name);
				if (server != null && server.getLocation().isAutoSync()) {
					audits += ps.numLocalAudits + ps.numServerAudits;
				}
			}
			// FIX should this be per-project?
			if (audits > auditThreshold) {
				SierraServersAutoSync.asyncSyncWithServer(ServerSyncType.BY_SERVER_SETTINGS);
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
		//createLocalScanFilterItems(content);
		return content.toArray(emptyChildren);
	}

	private void createUnknownServers(List<ServersViewContent> content) {
	    for(Map.Entry<String, List<ScanFilter>> e : localFilters.entrySet()) {
	        final String label = e.getKey();	        
	        final ConnectedServer server = f_manager.getServerById(id);
	        if (server != null) {
	            // Already known, so already handled elsewhere
	            continue;
	        }	        
	        final List<ScanFilter> filters = e.getValue();
	        if (filters.isEmpty()) {
	            continue;
	        }
	        ServersViewContent serverRoot = 
	            new ServersViewContent(null, SLImages.getImage(CommonImages.IMG_SIERRA_SERVER_GRAY));	        
	        List<ServersViewContent> children = new ArrayList<ServersViewContent>();	   
	        for(ScanFilter f : e.getValue()) {
	            final String name = f.getName();
	            ServersViewContent filter = createLabel(serverRoot, children, name, ChangeStatus.NONE);
	            filter.setData(f);          
	        }	        	        
	        serverRoot.setText(label);
	        serverRoot.setChildren(children.toArray(emptyChildren));
	        serverRoot.setData(filters);
	    }
	}
	
	private ServersViewContent createServerItem(final ConnectedServer server) {
		final List<ServersViewContent> serverContent = new ArrayList<ServersViewContent>();

		final ServersViewContent serverNode = new ServersViewContent(null,
				SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		serverNode.setData(server);
		/*
		 * if (focus != null && label.equals(focus.getLabel())) { focused =
		 * item; }
		 */
		final ServersViewContent categories = createCategories(serverNode,
				server);
		if (categories != null) {
			serverContent.add(categories);
		}
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
		serverNode.setText(status3.getLabel() + server + " ["
				+ server.toURLWithContextPath() + ']');
		return serverNode;
	}

	private static final String delta = ChangeStatus.REMOTE.getLabel();

	private ServersViewContent createCategories(
			final ServersViewContent serverNode, final ConnectedServer server) {
		final ServerUpdateStatus update = serverUpdates.get(server);
		final int numCategories = update == null ? 0 : update
				.getNumUpdatedFilterSets();
		if (numCategories > 0) {
			final ServersViewContent root = new ServersViewContent(serverNode,
					SLImages.getImage(CommonImages.IMG_FILTER));
			root.setText(delta + CATEGORIES);
			ServersViewContent label;
			if (numCategories > 1) {
				label = createLabel(root, delta + numCategories
						+ " categories to update", ChangeStatus.REMOTE);
			} else {
				label = createLabel(root, delta + "1 category to update",
						ChangeStatus.REMOTE);
			}
			label.setChangeStatus(ChangeStatus.REMOTE);
			createChangedCategories(update, label);
			return root;
		}
		return null;
	}

	private void createChangedCategories(ServerUpdateStatus update,
			ServersViewContent parent) {
		List<ServersViewContent> children = new ArrayList<ServersViewContent>();
		for (FilterSet f : update.getUpdatedCategories()) {
			createLabel(parent, children, delta + f.getName(),
					ChangeStatus.REMOTE);
		}
		parent.setChildren(children.toArray(emptyChildren));
	}

	private ServersViewContent createScanFilters(
			final ServersViewContent serverNode, final ConnectedServer server) {
		final ServerUpdateStatus update = serverUpdates.get(server);
		final List<ScanFilter> filters = localFilters.get(server.getName());
		if ((update == null || update.getNumScanFilters() == 0) && 
			(!showFiltersOnServer || filters == null || filters.isEmpty())) {
			return null;
		}
		final int num = update == null ? 0 : update.getNumUpdatedScanFilters();
		final boolean changed = num > 0;
		if (!(changed || showFiltersOnServer)) {
			return null;
		}
		final ServersViewContent root = new ServersViewContent(serverNode,
					SLImages.getImage(CommonImages.IMG_FILTER));
		root.setText(changed ? delta + SCAN_FILTERS : SCAN_FILTERS);
		
		final ServersViewContent filterRoot;				
		if (changed) {
			filterRoot = createLabel(root, delta+num+" scan filter"+s(num)+" to update",
					ChangeStatus.REMOTE);			
		} else {
			filterRoot = root;
		}		
		List<ServersViewContent> children = new ArrayList<ServersViewContent>();
		for (ScanFilter f : update != null ? update.getScanFilters() : filters) {
			ServersViewContent filter;
			if (update != null && update.isChanged(f)) {
				filter = createLabel(filterRoot, children, delta + f.getName(),
						ChangeStatus.REMOTE);
				//filter.setData(f);
			} else if (showFiltersOnServer) {
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
	private void createLocalScanFilterItems(final List<ServersViewContent> content) {
		ServersViewContent filterRoot = 
			new ServersViewContent(null, SLImages.getImage(CommonImages.IMG_FILTER));
		content.add(filterRoot);

		List<ServersViewContent> children = new ArrayList<ServersViewContent>();
		for(Map.Entry<String, List<ScanFilter>> e : localFilters.entrySet()) {
			final String server = e.getKey();
			for(ScanFilter f : e.getValue()) {
				final String name = f.getName()+" ("+server+")";
				ServersViewContent filter = createLabel(filterRoot, children, name, ChangeStatus.NONE);
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
		ServersViewContent c = createLabel(parent, children, text);
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
		if (!ps.localFindings.isEmpty()) {
			final ServersViewContent audits = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_SIERRA_STAMP));
			contents.add(audits);

			final List<ServersViewContent> auditContents = new ArrayList<ServersViewContent>();
			createAuditItems(audits, auditContents, false, ps.numLocalAudits,
					ps.localFindings.size(), ps.earliestLocalAudit,
					ps.latestLocalAudit);
			createLocalAuditDetails(audits, auditContents, ps.localFindings);
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
		if (ps.localDBInfo != null) {
			final ServersViewContent scanFilter = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_FILTER));
			contents.add(scanFilter);
			scanFilter
					.setText("Scan filter: " + ps.localDBInfo.getScanFilter());
			scanFilter.setData(ps.localDBInfo);
		}
		*/
		if (ps.filter != null) {
			final ServersViewContent scanFilter = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_FILTER));
			contents.add(scanFilter);
			scanFilter
					.setText("Scan filter: " + ps.filter.getName());
			scanFilter.setData(ps.filter);
		}
		if (ps.serverData == null) {
			if (server != null) {
				final ServersViewContent noServer = new ServersViewContent(
						root, null);
				contents.add(noServer);
				noServer
						.setText("No server info available ... click to update");
				noServer.setData(NO_SERVER_DATA);
			}
		} else if (!ps.serverData.isEmpty()) {
			final ServersViewContent audits = new ServersViewContent(root,
					SLImages.getImage(CommonImages.IMG_SIERRA_STAMP));
			contents.add(audits);

			final List<ServersViewContent> auditContents = new ArrayList<ServersViewContent>();
			createAuditItems(audits, auditContents, true, ps.numServerAudits,
					ps.serverData.size(), ps.earliestServerAudit,
					ps.latestServerAudit);
			createServerAuditDetails(ps, audits, auditContents);
			audits.setChildren(auditContents.toArray(emptyChildren));
			audits.setChangeStatus(ChangeStatus.REMOTE);
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
			final List<ServersViewContent> contents, final boolean server,
			final int numAudits, final int findings, final Date earliestA,
			final Date latestA) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");

		if (server) {
			audits.setText("< " + numAudits + " audit" + s(numAudits) + " on "
					+ findings + " finding" + s(findings) + " on the server");
		} else {
			audits.setText("> " + numAudits + " audit" + s(numAudits) + " on "
					+ findings + " finding" + s(findings));
		}

		if (earliestA != null) {
			createLabel(audits, contents, "Earliest on "
					+ dateFormat.format(earliestA));
		}
		if ((latestA != null) && (earliestA != latestA)) {
			createLabel(audits, contents, "Latest on "
					+ dateFormat.format(latestA));
		}
		return audits;
	}

	private void createLocalAuditDetails(final ServersViewContent audits,
			final List<ServersViewContent> contents,
			final List<FindingAudits> findings) {
		for (final FindingAudits f : findings) {
			final ServersViewContent item = new ServersViewContent(audits,
					SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_50));
			final int num = f.getAudits().size();
			item.setText(num + " audit" + s(num) + " on finding "
					+ f.getFindingId());
			item.setData(f);
			contents.add(item);
		}
	}

	private void createServerAuditDetails(final ProjectStatus ps,
			final ServersViewContent audits,
			final List<ServersViewContent> contents) {
		if (ps.comments > 0) {
			createLabel(audits, contents, ps.comments + " comment"
					+ s(ps.comments));
		}
		if (ps.importance > 0) {
			createLabel(audits, contents, ps.importance + " change"
					+ s(ps.importance) + " to the importance");
		}
		if (ps.summary > 0) {
			createLabel(audits, contents, ps.summary + " change"
					+ s(ps.summary) + " to the summary");
		}
		if (ps.read > 0) {
			createLabel(audits, contents, ps.read + " other finding"
					+ s(ps.read) + " examined");
		}
		for (final Map.Entry<String, Integer> e : ps.userCount.entrySet()) {
			if (e.getValue() != null) {
				final int count = e.getValue().intValue();
				if (count > 0) {
					createLabel(audits, contents, count + " audit" + s(count)
							+ " by " + e.getKey());
				}
			}
		}
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
