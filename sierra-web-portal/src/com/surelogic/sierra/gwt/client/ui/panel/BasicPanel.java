package com.surelogic.sierra.gwt.client.ui.panel;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.type.Status;
import com.surelogic.sierra.gwt.client.ui.type.Status.State;

public abstract class BasicPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-Section";
	private static final String SUBSECTION_STYLE = "sl-Subsection";
	private final DockPanel rootPanel = new DockPanel();
	private final DockPanel titlePanel = new DockPanel();
	private final Label sectionTitle = new Label();
	private final HTML sectionSummary = new HTML();
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final VerticalPanel contentPanel = new VerticalPanel();
	private boolean initialized;
	private Widget status;

	public BasicPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("auto");

		titlePanel.addStyleName(PRIMARY_STYLE + "-titlepanel");

		sectionTitle.addStyleName(PRIMARY_STYLE + "-title");
		sectionTitle.setWordWrap(false);
		titlePanel.add(sectionTitle, DockPanel.WEST);

		sectionSummary.addStyleName(PRIMARY_STYLE + "-info");
		sectionSummary.setWordWrap(false);
		titlePanel.add(sectionSummary, DockPanel.CENTER);
		titlePanel.setCellHorizontalAlignment(sectionSummary,
				HasHorizontalAlignment.ALIGN_CENTER);

		actionPanel.addStyleName(PRIMARY_STYLE + "-actionpanel");
		titlePanel.add(actionPanel, DockPanel.EAST);
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		titlePanel.setCellVerticalAlignment(actionPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);

		titlePanel.setCellWidth(sectionTitle, "25%");
		titlePanel.setCellWidth(sectionSummary, "50%");
		titlePanel.setCellWidth(actionPanel, "25%");

		updateHeader();

		contentPanel.addStyleName(PRIMARY_STYLE + "-contentpanel");
		rootPanel.add(contentPanel, DockPanel.CENTER);
	}

	public final void initialize() {
		if (initialized) {
			return;
		}
		onInitialize(getContentPanel());
		initialized = true;
	}

	protected abstract void onInitialize(VerticalPanel contentPanel);

	@Override
	public final String getTitle() {
		return sectionTitle.getText();
	}

	@Override
	public final void setTitle(final String text) {
		sectionTitle.setText(text);
		updateHeader();
	}

	public final String getSummary() {
		return sectionSummary.getText();
	}

	public final void setSummary(final String text) {
		sectionSummary.setHTML(text);
		updateHeader();
	}

	public final void setSubsectionStyle(final boolean enable) {
		if (enable) {
			getTitlePanel().addStyleName(SUBSECTION_STYLE + "-titlepanel");
			getContentPanel().addStyleName(SUBSECTION_STYLE + "-contentpanel");
		} else {
			getTitlePanel().removeStyleName(SUBSECTION_STYLE + "-titlepanel");
			getContentPanel().removeStyleName(
					SUBSECTION_STYLE + "-contentpanel");
		}
	}

	public final void setActionVisible(final Widget action,
			final boolean visible) {
		if (actionPanel.getWidgetIndex(action) == -1) {
			return;
		}
		action.setVisible(visible);
	}

	public final void setActionsVisible(final boolean visible) {
		actionPanel.setVisible(visible);
	}

	public final Widget addAction(final Widget w) {
		actionPanel.add(w);
		w.addStyleName("sl-Section-actionpanel-item");
		updateHeader();
		return w;
	}

	public final Widget addAction(final String text,
			final ClickListener clickListener) {
		final Label action = StyleHelper.add(new Label(text, false),
				Style.CLICKABLE);
		action.addClickListener(clickListener);
		return addAction(action);
	}

	public final void removeAction(final Widget w) {
		actionPanel.remove(w);
		updateHeader();
	}

	public final void removeActions() {
		while (actionPanel.getWidgetCount() > 0) {
			actionPanel.remove(0);
		}
		updateHeader();
	}

	public void setWaitStatus() {
		setWaitStatus(null);
	}

	public void setWaitStatus(final String text) {
		if (text != null) {
			final HorizontalPanel waitPanel = new HorizontalPanel();
			waitPanel.add(ImageHelper.getWaitImage(16));
			waitPanel.add(StyleHelper.add(new Label(text), Style.ITALICS));
			setStatus(waitPanel, HasHorizontalAlignment.ALIGN_CENTER);
		} else {
			setStatus(ImageHelper.getWaitImage(16),
					HasHorizontalAlignment.ALIGN_CENTER);
		}
	}

	public void setErrorStatus(final String text) {
		final Label textLbl = new Label(text);
		textLbl.addStyleName("error");
		setStatus(textLbl, HasHorizontalAlignment.ALIGN_LEFT);
	}

	public void setSuccessStatus(final String text) {
		final Label textLbl = new Label(text);
		textLbl.addStyleName("success");
		setStatus(textLbl, HasHorizontalAlignment.ALIGN_LEFT);
	}

	public void setStatus(final Widget w,
			final HorizontalAlignmentConstant align) {
		clearStatus();
		status = w;
		contentPanel.add(status);
		contentPanel.setCellHorizontalAlignment(status, align);
	}

	public void setStatus(final Status status) {
		if (status == null) {
			clearStatus();
		} else {
			final State state = status.getState();
			if (state == State.WAIT) {
				setWaitStatus(status.getMessage());
			} else if (state == State.FAILURE) {
				setErrorStatus(status.getMessage());
			} else if (state == State.SUCCESS) {
				setSuccessStatus(status.getMessage());
			} else {
				clearStatus();
			}
		}
	}

	public void clearStatus() {
		if (status != null) {
			contentPanel.remove(status);
		}
		status = null;
	}

	public DockPanel getTitlePanel() {
		return titlePanel;
	}

	public VerticalPanel getContentPanel() {
		return contentPanel;
	}

	private void updateHeader() {
		if ("".equals(sectionTitle.getText())
				&& "".equals(sectionSummary.getText())
				&& actionPanel.getWidgetCount() == 0) {
			rootPanel.remove(titlePanel);
		} else if (rootPanel.getWidgetIndex(titlePanel) == -1) {
			rootPanel.add(titlePanel, DockPanel.NORTH);
		}
	}
}
