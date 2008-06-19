package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Server;

public class ServerManagementContent extends
		ListContentComposite<Server, ServerCache> {
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
	protected String getItemText(Server item) {
		return item.getLabel();
	}

	@Override
	protected boolean isMatch(Server item, String query) {
		return item.getLabel().toLowerCase().contains(query.toLowerCase());
	}

	@Override
	protected void onSelectionChanged(Server item) {
		serverView.setSelection(item);
	}

}
