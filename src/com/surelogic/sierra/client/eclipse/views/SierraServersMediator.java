package com.surelogic.sierra.client.eclipse.views;

import java.net.URL;
import java.util.logging.Level;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
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
	final MenuItem f_serverPropertiesItem;
	final Button f_openInBrowser;
	final Label f_serverURL;
	final Table f_projectList;

	final Listener f_newServerAction = new Listener() {
		public void handleEvent(Event event) {
			final SierraServer newServer = f_manager.create();
			final ServerLocationDialog dialog = new ServerLocationDialog(
					f_serverList.getShell(), newServer, true);
			if (dialog.open() == Window.CANCEL) {
				/*
				 * If the user cancels input of information about the new
				 * server, we'll assume that they don't want it.
				 */
				f_manager.delete(newServer);
			}
		}
	};

	final Listener f_duplicateServerAction = new Listener() {
		public void handleEvent(Event event) {
			f_manager.duplicate();
		}
	};

	final Listener f_deleteServerAction = new Listener() {
		public void handleEvent(Event event) {
			SierraServer server = f_manager.getFocus();
			if (server != null) {
				f_manager.delete(server);
			}
		}
	};

	final SierraServerManager f_manager = SierraServerManager.getInstance();

	public SierraServersMediator(Table serverList, ToolItem newServer,
			ToolItem duplicateServer, ToolItem deleteServer,
			MenuItem newServerItem, MenuItem duplicateServerItem,
			MenuItem deleteServerItem, MenuItem serverPropertiesItem,
			Button openInBrowser, Label serverURL, Table projectList) {
		f_serverList = serverList;
		f_newServer = newServer;
		f_duplicateServer = duplicateServer;
		f_deleteServer = deleteServer;
		f_newServerItem = newServerItem;
		f_duplicateServerItem = duplicateServerItem;
		f_deleteServerItem = deleteServerItem;
		f_serverPropertiesItem = serverPropertiesItem;
		f_openInBrowser = openInBrowser;
		f_serverURL = serverURL;
		f_projectList = projectList;
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
					System.out.println(server.toString());
				}
			}
		});

		f_newServer.addListener(SWT.Selection, f_newServerAction);
		f_newServerItem.addListener(SWT.Selection, f_newServerAction);

		f_duplicateServer.addListener(SWT.Selection, f_duplicateServerAction);
		f_duplicateServerItem.addListener(SWT.Selection,
				f_duplicateServerAction);

		f_deleteServer.addListener(SWT.Selection, f_deleteServerAction);
		f_deleteServerItem.addListener(SWT.Selection, f_deleteServerAction);

		f_serverPropertiesItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final SierraServer server = f_manager.getFocus();
				if (server == null) {
					SLLogger.getLogger().log(Level.WARNING,
							"Edit server pressed with no server focus.");
					return;
				}
				final ServerLocationDialog dialog = new ServerLocationDialog(
						f_serverList.getShell(), server, false);
				dialog.open();
			}
		});

		f_openInBrowser.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final SierraServer server = f_manager.getFocus();
				if (server == null) {
					SLLogger.getLogger().log(Level.WARNING,
							"Edit server pressed with no server focus.");
					return;
				}
				openInBrowser(server);
			}
		});
	}

	public void dispose() {
		// TODO
	}

	public void setFocus() {
		// TODO something reasonable.
	}

	public void notify(SierraServerManager manager) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
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
				f_serverPropertiesItem.setEnabled(focusServer);
				f_openInBrowser.setEnabled(focusServer);
				if (focusServer) {
					items = f_serverList.getItems();
					for (int i = 0; i < items.length; i++) {
						if (items[i].getText().equals(server.getLabel())) {
							f_serverList.select(i);
							break;
						}
					}
					f_serverURL.setText(server.toURLString());
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
					f_serverURL.setText("No server selected");
				}
				f_serverList.setRedraw(true);
			}
		});
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
		final String url = server.toURLString() + "/sierra";
		final String name = "Sierra Server '" + server.getLabel() + "'";

		try {
			final IWebBrowser browser = PlatformUI.getWorkbench()
					.getBrowserSupport().createBrowser(
							IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.STATUS,
							server.getLabel(), name, name);
			browser.openURL(new URL(url));
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Exception occurred when opening URL: " + url);
		}
	}
}
