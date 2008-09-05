package com.surelogic.sierra.gwt.client.content.usermgmt;

import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.ui.grid.SelectableGrid;
import com.surelogic.sierra.gwt.client.ui.grid.SelectableGridListener;
import com.surelogic.sierra.gwt.client.ui.grid.TextBoxEditor;
import com.surelogic.sierra.gwt.client.ui.panel.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.panel.GridPanel;

public final class UserManagementContent extends ContentComposite {

	private static final UserManagementContent instance = new UserManagementContent();

	private static final String ADMIN = "Administrator";
	private static final String USER = "User";
	private static final String ENABLED = "Enabled";
	private static final String DISABLED = "Disabled";

	private final VerticalPanel usersPanel = new VerticalPanel();
	private final ActionPanel userActionsPanel = new ActionPanel();
	private final GridPanel usersGridPanel = new GridPanel(false);
	private final SelectableGrid usersGrid = usersGridPanel.getGrid();
	private final StatusBox status = new StatusBox();

	private boolean showDisabled = true;

	public static UserManagementContent getInstance() {
		return instance;
	}

	private UserManagementContent() {
		// singleton
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		usersPanel.setWidth("100%");
		final boolean isAdmin = SessionManager.getUser().isAdministrator();
		if (isAdmin) {
			userActionsPanel.addAction("Create a new user",
					new ClickListener() {
						public void onClick(final Widget sender) {
							usersGrid.clearStatus();
							createUser();
						}

					});
		}
		usersPanel.add(userActionsPanel);
		usersGridPanel.addGridOption("Show disabled users",
				new ClickListener() {
					public void onClick(final Widget sender) {
						showDisabled = !showDisabled;
						refreshUsers();
					}
				}, true);
		usersGrid.setColumn(0, "Name", "25%");
		usersGrid.setColumn(1, "Role", "25%");
		usersGrid.setColumn(2, "Status", "25%");
		usersGrid.setColumn(3, "Password", "25%");
		usersPanel.add(usersGridPanel);
		rootPanel.add(usersPanel, DockPanel.CENTER);
		if (isAdmin) {
			usersGrid.setInplaceEditor(0, TextBoxEditor.getFactory());
		}
		usersGrid.addListener(new SelectableGridListener() {

			public Object onChange(final Widget source, final int row,
					final int column, final Object oldValue,
					final Object newValue) {
				if (column == 0) {
					return changeUserName(row, (String) oldValue,
							(String) newValue);
				}
				return newValue;
			}

			public void onClick(final Widget source, final int row,
					final int column, final Object rowData) {
			}

			public void onHeaderClick(final Widget source, final int column) {
			}

			public void onSelect(final int row, final Object rowData) {
			}

		});
		usersPanel.add(status);
	}

	@Override
	protected void onUpdate(final Context context) {
		// load the users into the grid
		usersGrid.setWaitStatus();
		refreshUsers();
	}

	@Override
	protected void onDeactivate() {
		// clear the grid to free up some resources
		usersGrid.removeRows();
	}

	private void refreshUsers() {
		ServiceHelper.getManageUserService().getUsers(
				new StandardCallback<List<UserAccount>>() {

					@Override
					protected void doSuccess(final List<UserAccount> result) {
						final UserAccount currentUser = SessionManager
								.getUser();

						usersGrid.removeRows();
						if (result.isEmpty()) {
							usersGrid.setStatus("info", "No users found");
						} else {
							usersGrid.clearStatus();
							for (final UserAccount user : result) {
								// need to convert the service return to
								// UserAccount
								if (user.isActive() || showDisabled) {
									final int row = usersGrid.addRow();
									updateRow(row, user, currentUser);
								}
							}
						}
					}

				});
	}

	private void updateRow(final int row, final UserAccount user,
			final UserAccount currentUser) {
		usersGrid.setText(row, 0, user.getUserName());
		if (currentUser.isAdministrator()) {
			usersGrid.setWidget(row, 1, createUserRoleChoice(row, user));
			usersGrid.setWidget(row, 2, createUserStatusChoice(row, user));
			usersGrid.setWidget(row, 3, createPasswordChanger(row, user));
		} else {
			usersGrid.setText(row, 1, user.isAdministrator() ? ADMIN : USER);
			usersGrid.setText(row, 2, user.isActive() ? ENABLED : DISABLED);
			if (currentUser.getUserName().equals(user.getUserName())) {
				usersGrid.setWidget(row, 3, createPasswordChanger(row, user));
			} else {
				usersGrid.setText(row, 3, "");
			}
		}
		usersGrid.setRowData(row, user);
	}

	private HTML createPasswordChanger(final int row, final UserAccount user) {
		final HTML html = new HTML("Change");
		html.setStyleName("clickable");
		html.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				usersGrid.clearStatus();
				final ChangePasswordDialog dialog = new ChangePasswordDialog();
				dialog.update(user);
				dialog.center();
			}
		});
		return html;
	}

	private ListBox createUserStatusChoice(final int row, final UserAccount user) {
		final ListBox box = new ListBox();
		box.addItem(DISABLED);
		box.addItem(ENABLED);
		box.setSelectedIndex(user.isActive() ? 1 : 0);
		box.addChangeListener(new ChangeListener() {

			public void onChange(final Widget sender) {
				user.setActive(ENABLED.equals(box.getItemText(box
						.getSelectedIndex())));
				ServiceHelper.getManageUserService().updateUser(user,
						new ResultCallback<UserAccount>() {

							@Override
							protected void doFailure(final String message,
									final UserAccount result) {
								usersGrid.setStatus("error",
										"Error updating the role of "
												+ user.getUserName() + ": "
												+ message);
							}

							@Override
							protected void doSuccess(final String message,
									final UserAccount result) {
								updateRow(row, result, SessionManager.getUser());
								status.setStatus(Status.success(message));
							}
						});
			}
		});
		return box;
	}

	private ListBox createUserRoleChoice(final int row, final UserAccount user) {
		final ListBox box = new ListBox();
		box.addItem(USER);
		box.addItem(ADMIN);
		box.setSelectedIndex(user.isAdministrator() ? 1 : 0);
		box.addChangeListener(new ChangeListener() {

			public void onChange(final Widget sender) {
				user.setAdministrator(ADMIN.equals(box.getItemText(box
						.getSelectedIndex())));
				ServiceHelper.getManageUserService().updateUser(user,
						new ResultCallback<UserAccount>() {

							@Override
							protected void doFailure(final String message,
									final UserAccount result) {
								usersGrid.setStatus("error",
										"Error updating the role of "
												+ user.getUserName() + ": "
												+ message);
							}

							@Override
							protected void doSuccess(final String message,
									final UserAccount result) {
								updateRow(row, result, SessionManager.getUser());
								status.setStatus(Status.success(message));
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

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				status.setStatus(dialog.getStatus());
				refreshUsers();
			}
		});
		dialog.center();
	}

	private String changeUserName(final int row, final String oldValue,
			final String newValue) {
		usersGrid.clearStatus();
		final UserAccount account = (UserAccount) usersGrid.getRowData(row);
		if (account != null) {
			account.setUserName(newValue);
			ServiceHelper.getManageUserService().updateUser(account,
					new ResultCallback<UserAccount>() {

						@Override
						protected void doFailure(final String message,
								final UserAccount result) {
							usersGrid.setStatus("error",
									"Unable to change user name: " + message);
							updateRow(row, result, SessionManager.getUser());
							status.setStatus(Status.failure(message));
						}

						@Override
						protected void doSuccess(final String message,
								final UserAccount result) {
							final UserAccount current = SessionManager
									.getUser();
							if (result.getId() == current.getId()) {
								SessionManager.updateUser(result);
							}
							updateRow(row, result, SessionManager.getUser());
							status.setStatus(Status.success(message));

						}
					});
		}
		return newValue;
	}
}
