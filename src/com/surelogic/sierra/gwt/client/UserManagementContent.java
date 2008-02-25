package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
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

// TODO add change password functionality
public class UserManagementContent extends ContentComposite {
	private static final UserManagementContent instance = new UserManagementContent();

	private static final String ADMIN = "Administrator";
	private static final String USER = "User";

	private final VerticalPanel usersPanel = new VerticalPanel();
	private final ActionPanel userActionsPanel = new ActionPanel();
	private final GridPanel usersGridPanel = new GridPanel(true);
	private final SelectableGrid usersGrid = usersGridPanel.getGrid();

	public static UserManagementContent getInstance() {
		return instance;
	}

	private UserManagementContent() {
		super();
	}

	public String getContextName() {
		return "UserManagement";
	}

	protected void onInitialize(DockPanel rootPanel) {
		usersPanel.setWidth("100%");

		userActionsPanel.addAction("Create a user", new ClickListener() {

			public void onClick(Widget sender) {
				createUser();
			}

		});
		usersPanel.add(userActionsPanel);
		usersGridPanel.setEnabled(false);
		usersGridPanel.addGridAction("Disable selected", new ClickListener() {

			public void onClick(Widget sender) {
				changeUsersStatus(false);
			}
		});
		usersGridPanel.addGridAction("Enable selected", new ClickListener() {

			public void onClick(Widget sender) {
				changeUsersStatus(true);
			}
		});
		usersGrid.setColumn(0, "Name", "25%");
		usersGrid.setColumn(1, "Role", "25%");
		usersGrid.setColumn(2, "Status", "25%");
		usersGrid.setColumn(3, "Password", "25%");
		usersPanel.add(usersGridPanel);
		rootPanel.add(usersPanel, DockPanel.CENTER);

		usersGrid.setInplaceEditor(0, TextBoxEditor.getFactory());
		usersGrid.addListener(new SelectableGridListener() {

			public Object onChange(Widget source, int row, int column,
					Object oldValue, Object newValue) {
				if (column == 0) {
					return changeUserName(row, (String) oldValue,
							(String) newValue);
				}
				return newValue;
			}

			public void onClick(Widget source, int row, int column,
					Object rowData) {
			}

			public void onHeaderClick(Widget source, int column) {
			}

			public void onSelect(int row, Object rowData) {
				usersGridPanel.setEnabled(false);
				for (int i = 0; i < usersGrid.getRowCount(); i++) {
					if (usersGrid.isSelected(i)) {
						usersGridPanel.setEnabled(true);
					}
				}
			}

		});

	}

	protected void onActivate() {
		// load the users into the grid
		usersGrid.setWaitStatus();
		refreshUsers();
	}

	protected boolean onDeactivate() {
		// clear the grid to free up some resources
		usersGrid.removeRows();
		return true;
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
						final int row = usersGrid.addRow();
						updateRow(row, user);
					}
				}
			}

		});
	}

	private void updateRow(int row, UserAccount user) {
		usersGrid.setText(row, 0, user.getUserName());
		usersGrid.setWidget(row, 1, createUserListBox(row, user));
		usersGrid.setText(row, 2, user.isActive() ? "Enabled" : "Disabled");
		usersGrid.setWidget(row, 3, createPasswordChanger(row, user));
		usersGrid.setRowData(row, user);
	}

	private HTML createPasswordChanger(final int row, final UserAccount user) {
		HTML html = new HTML("Change");
		html.setStyleName("clickable");
		html.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				usersGrid.clearStatus();
				final ChangePasswordDialog dialog = new ChangePasswordDialog(
						user);
				dialog.center();
			}
		});
		return html;
	}

	private ListBox createUserListBox(final int row, final UserAccount user) {
		ListBox box = new ListBox();
		box.addItem(USER);
		box.addItem(ADMIN);
		box.setSelectedIndex(user.isAdministrator() ? 1 : 0);
		box.addChangeListener(new ChangeListener() {

			public void onChange(Widget sender) {
				final ListBox box = (ListBox) sender;
				user.setAdministrator(ADMIN.equals(box.getItemText(box
						.getSelectedIndex())));
				ServiceHelper.getManageUserService().updateUser(user, null,
						new AsyncCallback() {

							public void onFailure(Throwable caught) {
								usersGrid.setStatus("error",
										"Error updating the role of "
												+ user.getUserName() + ".");
							}

							public void onSuccess(Object result) {
								usersGrid.setRowData(row, result);
							}
						});

			}
		});
		return box;
	}

	private void createUser() {
		usersGrid.clearStatus();
		final CreateUserDialog dialog = new CreateUserDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				if (dialog.isSuccessful()) {
					refreshUsers();
				}
			}
		});
		dialog.center();
	}

	private void changeUsersStatus(final boolean activate) {
		usersGrid.clearStatus();
		if (usersGrid.hasSelected()) {
			if (Window.confirm((activate ? "Enable" : "Disable")
					+ " all selected users?")) {
				final List names = new ArrayList();
				for (int i = 0; i < usersGrid.getRowCount(); i++) {
					if (usersGrid.isSelected(i)) {
						UserAccount account = (UserAccount) usersGrid
								.getRowData(i);
						if (activate != account.isActive()) {
							account.setActive(activate);
							names.add(account.getUserName());
						}
					}
				}
				ServiceHelper.getManageUserService().updateUsersStatus(names,
						activate, new AsyncCallback() {

							public void onFailure(Throwable caught) {
								usersGrid.setStatus("error", "Could not "
										+ (activate ? "enable" : "disable")
										+ " users.");
							}

							public void onSuccess(Object result) {
								refreshUsers();
							}
						});
			}
		} else {
			Window.alert("No users selected");
		}
	}

	private String changeUserName(final int row, String oldValue,
			String newValue) {
		usersGrid.clearStatus();
		final UserAccount account = (UserAccount) usersGrid.getRowData(row);
		if (account != null) {
			account.setUserName(newValue);
			ServiceHelper.getManageUserService().updateUser(account, null,
					new AsyncCallback() {

						public void onFailure(Throwable caught) {
							ExceptionTracker.logException(caught);

							// TODO all error handling needs a cleaning pass
							usersGrid.setStatus("error", "Server unreachable");
						}

						public void onSuccess(Object result) {
							updateRow(row, (UserAccount) result);
						}
					});
		}
		return newValue;
	}
}
