package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ServerLocationCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class ServerManagementContent extends
		ListContentComposite<ServerLocation, ServerLocationCache> {
	private static final ServerManagementContent instance = new ServerManagementContent();
	private final ServerLocationView serverView = new ServerLocationView();
	private ServerLocation selection;
	private StatusBox statusBox;

	public static ServerManagementContent getInstance() {
		return instance;
	}

	private ServerManagementContent() {
		// singleton
		super(new ServerLocationCache());
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel,
			final VerticalPanel selectionPanel) {
		setCaption("Servers");
		statusBox = new StatusBox();
		serverView.initialize();
		serverView.addAction("New", new ClickListener() {

			public void onClick(final Widget sender) {
				final CreateServerDialog d = new CreateServerDialog();
				d.addPopupListener(new PopupListener() {

					public void onPopupClosed(final PopupPanel sender,
							final boolean autoClosed) {
						if ((d.getStatus() != null)
								&& d.getStatus().isSuccess()) {
							final String text = d.getLabelText();
							if (text.length() > 0) {
								// Make sure this label doesn't already exist
								for (final ServerLocation l : getCache()) {
									if (l.getLabel().equals(text)) {
										statusBox
												.setStatus(Status
														.failure("A server location with that label already exists."));
										return;
									}
								}
								final ServerLocation l = new ServerLocation(
										text);
								ServiceHelper.getSettingsService()
										.saveServerLocation(l,
												new StatusCallback() {

													@Override
													public void doStatus(
															final Status result) {
														getCache().refresh();
														Context.current()
																.setUuid(text)
																.submit();
													}
												});
							}
						}
					}
				});
				d.center();

			}
		});
		serverView.addAction("Save", new ClickListener() {
			public void onClick(final Widget sender) {
				final ServerLocation l = serverView.getSelection();
				// If we changed our label, make sure it doesn't conflict with
				// an existing one
				if (!selection.getLabel().equals(l.getLabel())) {
					for (final ServerLocation loc : getCache()) {
						if (l.getLabel().equals(loc.getLabel())) {
							statusBox
									.setStatus(Status
											.failure("A label with that name already exists."));
							return;
						}
					}
				}
				getCache().save(l);
			}
		});
		serverView.addAction("Revert", new ClickListener() {
			public void onClick(final Widget sender) {
				final ServerLocation loc = serverView.getSelection();
				for (final ServerLocation l : getCache()) {
					if (l.getLabel().equals(loc.getLabel())) {
						serverView.setSelection(l);
					}
				}
			}
		});
		serverView.addAction("Delete", new ClickListener() {
			public void onClick(final Widget sender) {
				final ServerLocation loc = serverView.getSelection();
				ServiceHelper.getSettingsService().deleteServerLocation(
						loc.getUuid(), new StatusCallback() {

							@Override
							public void doStatus(final Status result) {
								statusBox.setStatus(result);
								getCache().refresh();
								new Context(ServerManagementContent.this)
										.submit();
							}
						});
			}
		});
		selectionPanel.add(statusBox);
		selectionPanel.add(serverView);
	}

	@Override
	protected String getItemText(final ServerLocation item) {
		return item.getLabel();
	}

	@Override
	protected boolean isItemVisible(final ServerLocation item,
			final String query) {
		return LangUtil.containsIgnoreCase(item.getLabel(), query);
	}

	@Override
	protected void onSelectionChanged(final ServerLocation item) {
		selection = item;
		serverView.setSelection(item);
	}

}
