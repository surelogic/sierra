package com.surelogic.sierra.client.eclipse.views;

import java.io.File;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolItem;

import com.surelogic.sierra.client.eclipse.model.ISierraServerObserver;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class SierraServersMediator implements ISierraServerObserver {

	final File f_saveFile;
	final Table f_serverList;
	final ToolItem f_newServer;
	final ToolItem f_duplicateServer;
	final ToolItem f_deleteServer;
	final Button f_openInBrowser;
	final Label f_serverURL;

	final SierraServerManager f_manager = new SierraServerManager();

	public SierraServersMediator(File saveFile, Table serverList,
			ToolItem newServer, ToolItem duplicateServer,
			ToolItem deleteServer, Button openInBrowser, Label serverURL) {
		f_saveFile = saveFile;
		f_serverList = serverList;
		f_newServer = newServer;
		f_duplicateServer = duplicateServer;
		f_deleteServer = deleteServer;
		f_openInBrowser = openInBrowser;
		f_serverURL = serverURL;
	}

	public void init() {
		f_manager.load(f_saveFile);
	}

	public void dispose() {
		f_manager.save(f_saveFile);

	}

	public void setFocus() {
		// TODO something reasonable.
	}

	public void notify(SierraServerManager manager) {
		// TODO Auto-generated method stub

	}
}
