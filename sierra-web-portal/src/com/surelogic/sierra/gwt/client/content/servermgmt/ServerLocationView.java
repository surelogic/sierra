package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation.Protocol;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;

public class ServerLocationView extends BasicPanel {

	private Grid g;

	private final Label name = new Label();
	private final TextBox host = new TextBox();
	private final TextBox port = new TextBox();
	private final TextBox context = new TextBox();
	private final RadioButton http = new RadioButton("protocol", "http");
	private final RadioButton https = new RadioButton("protocol", "https");
	private final TextBox user = new TextBox();
	private final PasswordTextBox password = new PasswordTextBox();

	private PortalServerLocation selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		g = new Grid(7, 2);
		g.setText(0, 0, "Name");
		g.setWidget(0, 1, name);
		g.setText(1, 0, "Host");
		g.setWidget(1, 1, host);
		g.setText(2, 0, "Port");
		g.setWidget(2, 1, port);
		g.setText(3, 0, "Context");
		g.setWidget(3, 1, context);
		g.setText(4, 0, "Protocol");
		final FlowPanel panel = new FlowPanel();
		panel.add(http);
		panel.add(https);
		g.setWidget(4, 1, panel);
		g.setText(5, 0, "User");
		g.setWidget(5, 1, user);
		g.setText(6, 0, "Password");
		g.setWidget(6, 1, password);
		contentPanel.add(g);
	}

	public PortalServerLocation getSelection() {
		final PortalServerLocation l = selection;
		l.setContext(context.getText());
		l.setHost(host.getText());
		l.setPass(password.getText());
		final String portStr = port.getText();
		if ((portStr != null) && (portStr.length() > 0)) {
			try {
				l.setPort(Integer.parseInt(portStr));
			} catch (final NumberFormatException e) {
				port.setText("13376");
			}
		}
		l.setProtocol(https.isChecked() ? Protocol.HTTPS : Protocol.HTTP);
		l.setUser(user.getText());
		return l;
	}

	public void setSelection(final PortalServerLocation item) {
		selection = item;
		name.setText(item.getName());
		host.setText(item.getHost());
		port.setText(Integer.toString(item.getPort()));
		context.setText(item.getContext());
		if (item.getProtocol() == Protocol.HTTP) {
			http.setChecked(true);
		} else {
			https.setChecked(true);
		}
		user.setText(item.getUser());
		password.setText(item.getPass());
	}
}
