package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

public class LogPane extends Composite {
    private static final String PRIMARY_STYLE = "log-panel";
    private static final String ITEM_STYLE = "log-item";
    private static final String ITEM_TEXT_STYLE = "log-item-text";
    private static final int ITEM_COUNT = 100;

    private final VerticalPanel rootPanel = new VerticalPanel();
    private final Image logIcon = ImageHelper.getImage("log-icon.png");
    private final Tree log = new Tree();

    public static LogPane getInstance() {
        return (LogPane) RootPanel.get("log-pane").getWidget(0);
    }

    public LogPane() {
        super();
        initWidget(rootPanel);
        rootPanel.setWidth("100%");

        log.addStyleName(PRIMARY_STYLE);

        logIcon.addClickListener(new ClickListener() {

            public void onClick(final Widget sender) {
                toggleLogVisible();
            }

        });
    }

    public void log(final Throwable cause) {
        showLogIcon();
        appendThrowable(cause, null);
    }

    private TreeItem addLogItem(final Label label, final Tree tree) {
        if (tree.getItemCount() < ITEM_COUNT) {
            return tree.addItem(label);
        }
        return null;
    }

    private TreeItem addLogItem(final Label label, final TreeItem tree) {
        if (tree.getChildCount() < ITEM_COUNT) {
            return tree.addItem(label);
        }
        return null;
    }

    private void appendThrowable(final Throwable cause, final TreeItem parent) {
        final Label logItemLabel = new Label(buildLogText(cause));
        logItemLabel.addStyleName(ITEM_TEXT_STYLE);

        TreeItem logItem;
        if (parent == null) {
            logItem = addLogItem(logItemLabel, log);
        } else {
            logItem = addLogItem(logItemLabel, parent);
            parent.addItem(logItemLabel);
        }
        if (logItem != null) {
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
    }

    private String buildLogText(final Throwable cause) {
        final StringBuffer log = new StringBuffer();
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
                    HasHorizontalAlignment.ALIGN_RIGHT);
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
