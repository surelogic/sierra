package com.surelogic.sierra.gwt.client.header;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.ContentRegistry;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextListener;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.UserListener;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public final class HeaderPanel extends Composite {
	private static final String PRIMARY_STYLE = "header";

	private final DockPanel rootPanel = new DockPanel();
	private HeaderComposite currentHeader;

	public static HeaderPanel getInstance() {
		return (HeaderPanel) RootPanel.get("header-pane").getWidget(0);
	}

	public HeaderPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");

		// Listen for user session changes
		ContextManager.addUserListener(new UserListener() {

			public void onLogin(UserAccount user) {
				updateUser(user);
			}

			public void onLoginFailure(String message) {
				updateUser(null);
			}

			public void onLogout(UserAccount user, String errorMessage) {
				updateUser(null);
			}

			public void onUpdate(UserAccount user) {
				updateUser(user);
			}
		});
		ContextManager.addContextListener(new ContextListener() {

			public void onChange(Context context) {
				updateContext(context);
			}

		});
		updateUser(ContextManager.getUser());
		updateContext(ContextManager.getContext());
	}

	private void updateHeader() {
		final UserAccount user = ContextManager.getUser();
		final Context context = ContextManager.getContext();

		Window.setTitle("Sierra Portal - "
				+ ContentRegistry.getContentTitle(context.getContent()));

		HeaderComposite newHeader = null;
		if (user == null) {
			newHeader = GuestHeader.getInstance();
		} else if (context != null) {
			newHeader = ContentRegistry.getContentHeader(context.getContent());
		}
		if (newHeader == null) {
			newHeader = UserHeader.getInstance();
		}

		if (currentHeader == newHeader) {
			return;
		}
		if (currentHeader != null) {
			currentHeader.deactivate();
		}
		currentHeader = newHeader;
		rootPanel.clear();
		rootPanel.add(currentHeader, DockPanel.CENTER);
		currentHeader.activate(ContextManager.getContext(), user);
	}

	private void updateContext(Context context) {
		updateHeader();
		if (currentHeader != null) {
			currentHeader.updateContext(context);
		}
	}

	private void updateUser(UserAccount user) {
		updateHeader();
		if (currentHeader != null) {
			currentHeader.updateUser(user);
		}
	}

}
