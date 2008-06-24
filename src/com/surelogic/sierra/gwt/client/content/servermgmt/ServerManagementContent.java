package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.cache.ServerCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ServerManagementContent extends
		ListContentComposite<ServerLocation, ServerCache> {
	private static final ServerManagementContent instance = new ServerManagementContent();
	private final ServerView serverView = new ServerView();

	public static ServerManagementContent getInstance() {
		return instance;
	}

	private ServerManagementContent() {
		// singleton
		super(new ServerCache());
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Servers");

		serverView.initialize();
		selectionPanel.add(serverView);
	}

	@Override
	protected String getItemText(ServerLocation item) {
		return item.getLabel();
	}

	@Override
	protected boolean isMatch(ServerLocation item, String query) {
		return LangUtil.containsIgnoreCase(item.getLabel(), query);
	}

	@Override
	protected void onSelectionChanged(ServerLocation item) {
		serverView.setSelection(item);
	}

}
