package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ServerLocationCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class ServerManagementContent extends
		ListContentComposite<PortalServerLocation, ServerLocationCache> {
	private static final ServerManagementContent instance = new ServerManagementContent();
	private final ServerLocationView serverView = new ServerLocationView();
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
				Context.current().setUuid("new").submit();
			}
		});
		serverView.addAction("Save", new ClickListener() {
			public void onClick(final Widget sender) {
				getCache().save(serverView.getSelection());
			}
		});
		serverView.addAction("Revert", new ClickListener() {
			public void onClick(final Widget sender) {
				final PortalServerLocation loc = serverView.getSelection();
				for (final PortalServerLocation l : getCache()) {
					if (l.getUuid().equals(loc.getUuid())) {
						serverView.setSelection(l);
					}
				}
			}
		});
		serverView.addAction("Delete", new ClickListener() {
			public void onClick(final Widget sender) {
				final PortalServerLocation loc = serverView.getSelection();
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
	protected String getItemText(final PortalServerLocation item) {
		return item.getName();
	}

	@Override
	protected boolean isItemVisible(final PortalServerLocation item,
			final String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(final PortalServerLocation item) {
		serverView.setSelection(item);
	}

	@Override
	protected void onUpdate(final Context context) {
		if ("new".equals(context.getUuid())) {
			serverView.setSelection(new PortalServerLocation());
		} else {
			super.onUpdate(context);
		}
	}

}
