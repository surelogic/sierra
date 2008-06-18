package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

public class BasicPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-Section";
	private static final String SUBSECTION_STYLE = "sl-Subsection";
	private final DockPanel rootPanel = new DockPanel();
	private final DockPanel titlePanel = new DockPanel();
	private final Label sectionTitle = new Label();
	private final Label sectionSummary = new Label();
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final VerticalPanel contentPanel = new VerticalPanel();
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
				HorizontalPanel.ALIGN_CENTER);

		actionPanel.addStyleName(PRIMARY_STYLE + "-actionpanel");
		titlePanel.add(actionPanel, DockPanel.EAST);
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HorizontalPanel.ALIGN_RIGHT);
		titlePanel.setCellVerticalAlignment(actionPanel,
				HorizontalPanel.ALIGN_MIDDLE);

		titlePanel.setCellWidth(sectionTitle, "25%");
		titlePanel.setCellWidth(sectionSummary, "50%");
		titlePanel.setCellWidth(actionPanel, "25%");

		updateHeader();

		contentPanel.addStyleName(PRIMARY_STYLE + "-contentpanel");
		rootPanel.add(contentPanel, DockPanel.CENTER);
	}

	@Override
	public final String getTitle() {
		return sectionTitle.getText();
	}

	@Override
	public final void setTitle(String text) {
		sectionTitle.setText(text);
		updateHeader();
	}

	public final String getSummary() {
		return sectionSummary.getText();
	}

	public final void setSummary(String text) {
		sectionSummary.setText(text);
		updateHeader();
	}

	public final void setSubsectionStyle(boolean enable) {
		if (enable) {
			getTitlePanel().addStyleName(SUBSECTION_STYLE + "-titlepanel");
			getContentPanel().addStyleName(SUBSECTION_STYLE + "-contentpanel");
		} else {
			getTitlePanel().removeStyleName(SUBSECTION_STYLE + "-titlepanel");
			getContentPanel().removeStyleName(
					SUBSECTION_STYLE + "-contentpanel");
		}
	}

	public final void setActionsVisible(boolean visible) {
		actionPanel.setVisible(visible);
	}

	public final void addAction(Widget w) {
		actionPanel.add(w);
		w.addStyleName("sl-Section-actionpanel-item");
		updateHeader();
	}

	public final void addAction(String text, ClickListener clickListener) {
		addAction(new ClickLabel(text, clickListener));
	}

	public final void removeAction(Widget w) {
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
		setStatus(ImageHelper.getWaitImage(16),
				HasHorizontalAlignment.ALIGN_CENTER);
	}

	public void setErrorStatus(String text) {
		final Label textLbl = new Label(text);
		textLbl.addStyleName("error");
		setStatus(textLbl, HasHorizontalAlignment.ALIGN_LEFT);
	}

	public void setSuccessStatus(String text) {
		final Label textLbl = new Label(text);
		textLbl.addStyleName("success");
		setStatus(textLbl, HasHorizontalAlignment.ALIGN_LEFT);
	}

	public void setStatus(Widget w, HorizontalAlignmentConstant align) {
		clearStatus();
		status = w;
		contentPanel.add(status);
		contentPanel.setCellHorizontalAlignment(status, align);
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
