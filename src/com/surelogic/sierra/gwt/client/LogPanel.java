package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

public class LogPanel extends Composite {
	private static final String PRIMARY_STYLE = "log-panel";
	private static final String ITEM_STYLE = "log-item";
	private static final String ITEM_TEXT_STYLE = "log-item-text";
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final Image logIcon = ImageHelper.getImage("log-icon.png");
	private final Tree log = new Tree();

	public static LogPanel getInstance() {
		return (LogPanel) RootPanel.get("log-pane").getWidget(0);
	}

	public LogPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		log.addStyleName(PRIMARY_STYLE);

		logIcon.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				toggleLogVisible();
			}

		});
	}

	public void log(Throwable cause) {
		showLogIcon();
		appendThrowable(cause, null);
	}

	private void appendThrowable(Throwable cause, TreeItem parent) {
		final Label logItemLabel = new Label(buildLogText(cause));
		logItemLabel.addStyleName(ITEM_TEXT_STYLE);

		TreeItem logItem;
		if (parent == null) {
			logItem = log.addItem(logItemLabel);
		} else {
			logItem = parent.addItem(logItemLabel);
		}
		logItem.addStyleName(ITEM_STYLE);

		final StackTraceElement[] trace = cause.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			logItem.addItem(trace[i].toString());
		}

		final Throwable nestedCause = cause.getCause();
		if (nestedCause != null) {
			appendThrowable(nestedCause, logItem);
		}
	}

	private String buildLogText(Throwable cause) {
		StringBuffer log = new StringBuffer();
		log.append(cause.toString());

		final StackTraceElement[] trace = cause.getStackTrace();
		if (trace.length == 0) {
			log.append(" (no stack trace available)");
		}

		return log.toString();
	}

	private void showLogIcon() {
		if (rootPanel.getWidgetIndex(logIcon) == -1) {
			rootPanel.insert(logIcon, 0);
			rootPanel.setCellHorizontalAlignment(logIcon,
					VerticalPanel.ALIGN_RIGHT);
		}
	}

	private void toggleLogVisible() {
		if (rootPanel.getWidgetIndex(log) == -1) {
			rootPanel.add(log);
		} else {
			rootPanel.remove(log);
		}
	}
}
