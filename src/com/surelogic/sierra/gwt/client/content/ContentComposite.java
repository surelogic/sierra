package com.surelogic.sierra.gwt.client.content;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.surelogic.sierra.gwt.client.Context;

public abstract class ContentComposite extends Composite {
	public static final String PRIMARY_STYLE = "sl-ContentComposite";
	public static final String CAPTION_STYLE = PRIMARY_STYLE + "-title";
	private final DockPanel rootPanel = new DockPanel();
	private final HorizontalPanel titlePanel = new HorizontalPanel();
	private boolean initialized;
	private boolean active;

	public ContentComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

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
		final Label title = new Label(text);
		title.addStyleName(CAPTION_STYLE);
		titlePanel.add(title);
		titlePanel
				.setCellHorizontalAlignment(title, HorizontalPanel.ALIGN_LEFT);
	}

	protected abstract void onInitialize(DockPanel rootPanel);

	protected abstract void onUpdate(Context context);

	protected abstract void onDeactivate();
}
