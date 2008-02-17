package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.GridPanel;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public class AdminUsers2Tab extends TabComposite {

	private final VerticalPanel usersPanel = new VerticalPanel();
	private final ActionPanel userActionsPanel = new ActionPanel();
	private final GridPanel usersGrid = new GridPanel(true);

	public AdminUsers2Tab() {
		super();

		usersPanel.setWidth("100%");

		userActionsPanel.addAction("Create a user", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub

			}
		});
		usersPanel.add(userActionsPanel);

		usersGrid.setHeaderColumn(0, "Name", "30%");
		usersGrid.setHeaderColumn(1, "Status", "70%");

		usersGrid.addGridAction("Delete selected", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub

			}
		});
		usersPanel.add(usersGrid);

		final DockPanel rootPanel = getRootPanel();
		rootPanel.add(usersPanel, DockPanel.CENTER);

		// load the users into the grid
		refreshUsers();
	}

	public String getName() {
		return "Admin Users 2";
	}

	private void refreshUsers() {
		ServiceHelper.getManageUserService().getUsers(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);

				// TODO tell the user
			}

			public void onSuccess(Object result) {
				// TODO load up the users list
				// waitPanel.clear();
				// usersList.clear();
				// List users = (List) result;
				// if (users.isEmpty()) {
				// usersList.addItem("No users found");
				// } else {
				// for (Iterator i = users.iterator(); i.hasNext();) {
				// final String userName = (String) i.next();
				// usersList.addItem(userName);
				// }
				// }
			}
		});
	}
}
