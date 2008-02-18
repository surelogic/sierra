package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.GridPanel;
import com.surelogic.sierra.gwt.client.ui.SLGrid;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public class AdminUsers2Tab extends TabComposite {

	private final VerticalPanel usersPanel = new VerticalPanel();
	private final ActionPanel userActionsPanel = new ActionPanel();
	private final GridPanel usersGridPanel = new GridPanel(true);
	private final SLGrid usersGrid = usersGridPanel.getGrid();

	public AdminUsers2Tab() {
		super();

		usersPanel.setWidth("100%");

		userActionsPanel.addAction("Create a user", new ClickListener() {

			public void onClick(Widget sender) {
				createUser();
			}

		});
		usersPanel.add(userActionsPanel);

		usersGridPanel.addGridAction("Delete selected", new ClickListener() {

			public void onClick(Widget sender) {
				deleteUsers();
			}
		});

		usersGrid.setColumn(0, "Name", "30%");
		usersGrid.setColumn(1, "Status", "70%");

		usersPanel.add(usersGridPanel);

		final DockPanel rootPanel = getRootPanel();
		rootPanel.add(usersPanel, DockPanel.CENTER);

		// load the users into the grid
		usersGrid.setWaitStatus();
		refreshUsers();
	}

	public String getName() {
		return "Admin Users 2";
	}

	private void refreshUsers() {
		ServiceHelper.getManageUserService().getUsers(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);

				usersGrid.setErrorMessage("Unable to retrieve user list");
			}

			public void onSuccess(Object result) {

				// TODO load up the results
				// List users = (List) result;
				//
				// usersGrid.usersList.clear();
				//
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

	private void createUser() {
		// TODO Auto-generated method stub

	}

	private void deleteUsers() {
		// TODO delete all selected users
	}
}
