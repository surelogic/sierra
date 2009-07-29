package com.surelogic.sierra.gwt.client.content.extensions;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Extension;
import com.surelogic.sierra.gwt.client.data.cache.ExtensionCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ExtensionsContent extends
		ListContentComposite<Extension, ExtensionCache> {

	private static final ExtensionsContent instance = new ExtensionsContent();

	private final ExtensionView view = new ExtensionView();

	protected ExtensionsContent() {
		super(ExtensionCache.getInstance());
	}

	@Override
	protected String getItemText(final Extension item) {
		return item.getName() + " &ndash; " + item.getVersion();
	}

	@Override
	protected boolean isItemVisible(final Extension item,
			final String searchText) {
		return LangUtil.containsIgnoreCase(item.getName(), searchText)
				|| LangUtil.containsIgnoreCase(item.getVersion(), searchText);
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel,
			final VerticalPanel selectionPanel) {
		view.initialize();
		selectionPanel.add(view);
	}

	@Override
	protected void onSelectionChanged(final Extension item) {
		view.setExtension(item);
	}

	public static ExtensionsContent getInstance() {
		return instance;
	}

}
