package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.UI;

public class ServerLocationView extends BlockPanel {

	private VerticalPanel panel;

	private HTML context;
	private HTML host;
	private HTML label;
	private HTML pass;
	private HTML port;
	private HTML protocol;
	private HTML user;
	private ServerLocation item;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		panel = contentPanel;
		contentPanel.add(context = new HTML());
		contentPanel.add(host = new HTML());
		contentPanel.add(label = UI.h3(""));
		contentPanel.add(pass = new HTML());
		contentPanel.add(port = new HTML());
		contentPanel.add(protocol = new HTML());
		contentPanel.add(user = new HTML());
	}

	public ServerLocation getSelection() {
		return item;
	}

	public void setSelection(ServerLocation item) {
		panel.clear();
		panel.add(UI.h3(item.getLabel()));
		context.setText(item.getContext());
		host.setText(item.getHost());
		label.setText(item.getLabel());
		pass.setText(item.getPass() == null ? "" : item.getPass().replaceAll(
				".", "*"));
		port.setText(Integer.toString(item.getPort()));
		protocol.setText(item.getProtocol().toString());
		user.setText(item.getUser());
	}
}
