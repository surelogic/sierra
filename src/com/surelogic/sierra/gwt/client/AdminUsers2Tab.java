package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.GridPanel;
import com.surelogic.sierra.gwt.client.ui.SelectableGrid;
import com.surelogic.sierra.gwt.client.ui.SelectableGridListener;
import com.surelogic.sierra.gwt.client.ui.TextBoxEditor;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public class AdminUsers2Tab extends TabComposite {

	private final VerticalPanel usersPanel = new VerticalPanel();
	private final ActionPanel userActionsPanel = new ActionPanel();
	private final GridPanel usersGridPanel = new GridPanel(true);
	private final SelectableGrid usersGrid = usersGridPanel.getGrid();

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

		usersGrid.setInplaceEditor(0, TextBoxEditor.getFactory());
		usersGrid.addListener(new SelectableGridListener() {

			public boolean onChange(Widget source, int row, int column,
					Object oldValue, Object newValue) {
				if (column == 0) {
					return changeUserName(row, (String) oldValue,
							(String) newValue);
				}
				return false;
			}

			public void onClick(Widget source, int row, int column,
					Object rowData) {
			}

			public void onHeaderClick(Widget source, int column) {
			}
		});

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

				usersGrid.setStatus("error", "Unable to retrieve user list");
			}

			public void onSuccess(Object result) {
				List users = (List) result;

				usersGrid.removeRows();
				if (users.isEmpty()) {
					usersGrid.setStatus("info", "No users found");
				} else {
					usersGrid.clearStatus();
					for (Iterator i = users.iterator(); i.hasNext();) {
						// need to convert the service return to UserAccount
						final UserAccount user = (UserAccount) i.next();
						int rowIndex = usersGrid.addRow();
						usersGrid.setText(rowIndex, 0, user.getUserName());
						if (user.isAdministrator()) {
							usersGrid.setText(rowIndex, 1, "Administrator");
						}
						usersGrid.setRowData(rowIndex, user);
					}
				}
			}
		});
	}

	private void createUser() {
		// TODO open UI to create a new user
		Window.alert("TODO: Create a new user");
	}

	private void deleteUsers() {
		if (usersGrid.hasSelected()) {
			if (Window.confirm("Delete all selected users?")) {
				// TODO delete all selected users
				Window.alert("TODO: Delete all selected users");
			}
		} else {
			Window.alert("No users selected");
		}
	}

	private boolean changeUserName(int row, String oldValue, String newValue) {
		Window.alert("TODO: Username changed: " + newValue);
		return true;
	}

}
