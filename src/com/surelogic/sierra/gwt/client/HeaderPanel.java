package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
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
		ClientContext.addUserListener(new UserListener() {

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
		ClientContext.addContextListener(new ContextListener() {

			public void onChange(Context context) {
				updateContext(context);
			}

		});
		updateUser(ClientContext.getUser());
		updateContext(ClientContext.getContext());
	}

	private void updateHeader() {
		final UserAccount user = ClientContext.getUser();
		final Context context = ClientContext.getContext();

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
		currentHeader.activate(ClientContext.getContext(), user);
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
