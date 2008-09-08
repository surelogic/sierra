package com.surelogic.sierra.gwt.client.content;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.HtmlHelper;

public abstract class ContentComposite extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private final HorizontalPanel titlePanel = new HorizontalPanel();
	private boolean initialized;
	private boolean active;

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName("sl-ContentComposite");

		rootPanel.add(titlePanel, DockPanel.NORTH);
	}

	public final void show() {
		new Context(this).submit();
	}

	public final void update(final Context context) {
		if (!initialized) {
			initialized = true;
			onInitialize();
		}

		onUpdate(context);
		active = true;
	}

	public final void deactivate() {
		active = false;
		onDeactivate();
	}

	public final boolean isInitialized() {
		return initialized;
	}

	public final boolean isActive() {
		return active;
	}

	protected final void onInitialize() {
		onInitialize(rootPanel);
	}

	protected final DockPanel getRootPanel() {
		return rootPanel;
	}

	protected final void setCaption(final String text) {
		final HTML title = HtmlHelper.h2(text);
		titlePanel.add(title);
		titlePanel
				.setCellHorizontalAlignment(title, HorizontalPanel.ALIGN_LEFT);
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onUpdate(Context context);

	protected abstract void onDeactivate();
}
