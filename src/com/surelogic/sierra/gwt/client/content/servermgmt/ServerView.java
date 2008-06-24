package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.UI;

public class ServerView extends BlockPanel {

	private VerticalPanel panel;

	private HTML context;
	private HTML host;
	private HTML label;
	private HTML pass;
	private HTML port;
	private HTML protocol;
	private HTML user;
	private HTML uuid;
	private ServerLocation item;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		panel = contentPanel;
		label = UI.h3("");

		addAction("Edit", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub

			}
		});
		addAction("Delete", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void setSelection(ServerLocation item) {
		panel.clear();
		panel.add(UI.h3(item.getLabel()));
		item.getContext();
		item.getHost();
		item.getLabel();
		item.getPass();
		item.getPort();
		item.getProtocol();
		item.getUser();
		item.getUuid();

	}

}
