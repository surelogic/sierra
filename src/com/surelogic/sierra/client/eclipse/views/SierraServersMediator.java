package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;

public final class SierraServersMediator implements ISierraServerObserver {

	final File f_saveFile;
	final Table f_serverList;
	final ToolItem f_newServer;
	final ToolItem f_duplicateServer;
	final ToolItem f_deleteServer;
	final Button f_editServer;
	final Button f_openInBrowser;
	final Label f_serverURL;

	final SierraServerManager f_manager = new SierraServerManager();

	public SierraServersMediator(File saveFile, Table serverList,
			ToolItem newServer, ToolItem duplicateServer,
			ToolItem deleteServer, Button editServer, Button openInBrowser,
			Label serverURL) {
		f_saveFile = saveFile;
		f_serverList = serverList;
		f_newServer = newServer;
		f_duplicateServer = duplicateServer;
		f_deleteServer = deleteServer;
		f_editServer = editServer;
		f_openInBrowser = openInBrowser;
		f_serverURL = serverURL;
	}

	public void init() {
		f_manager.load(f_saveFile);
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
					final SierraServerModel server = f_manager
							.getOrCreate(label);
					f_manager.setFocus(server);
					System.out.println(server.toString());
				}
			}
		});

		f_newServer.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final SierraServerModel newServer = f_manager.create();
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
		});

		f_duplicateServer.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_manager.duplicate();
			}
		});

		f_deleteServer.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				SierraServerModel server = f_manager.getFocus();
				if (server != null) {
					f_manager.delete(server);
				}
			}
		});

		f_editServer.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final SierraServerModel server = f_manager.getFocus();
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
				final SierraServerModel server = f_manager.getFocus();
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
		f_manager.save(f_saveFile);
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
					}
				}
				SierraServerModel server = f_manager.getFocus();
				final boolean focusServer = server != null;
				f_duplicateServer.setEnabled(focusServer);
				f_deleteServer.setEnabled(focusServer);
				f_editServer.setEnabled(focusServer);
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

	private static void openInBrowser(SierraServerModel server) {
		final String url = server.toURLString();
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
