package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ServerLocationView extends BlockPanel {

	private Grid g;
	private ServerLocation item;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		g = new Grid(7, 2);
		g.setText(0, 0, "Label");
		g.setText(1, 0, "Host");
		g.setText(2, 0, "Port");
		g.setText(3, 0, "Context");
		g.setText(4, 0, "Protocol");
		g.setText(5, 0, "User");
		g.setText(6, 0, "Password");
		contentPanel.add(g);
	}

	public ServerLocation getSelection() {
		return item;
	}

	public void setSelection(ServerLocation item) {
		this.item = item.copy(item);
		g.setText(0, 1, item.getLabel());
		g.setText(1, 1, item.getHost());
		g.setText(2, 1, Integer.toString(item.getPort()));
		g.setText(3, 1, item.getContext());
		g.setText(4, 1, item.getProtocol().toString());
		g.setText(5, 1, item.getUser());
		g.setText(6, 1, item.getPass() == null ? "" : item.getPass()
				.replaceAll(".", "*"));
	}
}
