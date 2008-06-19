package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Server;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.UI;

public class ServerView extends BlockPanel {

	private final VerticalPanel panel = new VerticalPanel();;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(panel);
	}

	public void setSelection(Server item) {
		panel.clear();
		UI.h3("Server");
	}

}
