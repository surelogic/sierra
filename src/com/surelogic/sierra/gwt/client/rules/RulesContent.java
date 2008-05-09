package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.util.LangUtil;
import com.surelogic.sierra.gwt.client.util.UI;

public class RulesContent extends ContentComposite {
	private static final RulesContent instance = new RulesContent();
	private final CategoryCache categories = new CategoryCache();

	private final SearchSection searchSection = new SearchSection(categories);

	private final VerticalPanel selectionPanel = new VerticalPanel();
	private final CategorySection categorySelection = new CategorySection(
			categories);

	public static RulesContent getInstance() {
		return instance;
	}

	private RulesContent() {
		// no instances
	}

	protected void onInitialize(DockPanel rootPanel) {
		final Label title = UI.h2("Rules");
		rootPanel.add(title, DockPanel.NORTH);
		rootPanel.setCellHorizontalAlignment(title, DockPanel.ALIGN_LEFT);

		selectionPanel.setWidth("100%");
		selectionPanel.add(new Label(""));

		rootPanel.add(searchSection, DockPanel.WEST);
		rootPanel.setCellWidth(searchSection, "25%");
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");
	}

	protected void onActivate(Context context) {
		categories.refresh();

		searchSection.activate(context);

		final RulesContext rulesCtx = new RulesContext(context);
		final String categoryUuid = rulesCtx.getCategory();
		if (LangUtil.notEmpty(categoryUuid)) {
			if (selectionPanel.getWidgetIndex(categorySelection) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categorySelection);
			}
			categorySelection.activate(context);
		}
	}

	protected void onUpdate(Context context) {
		searchSection.update(context);
		if (selectionPanel.getWidgetIndex(categorySelection) >= 0) {
			categorySelection.update(context);
		}
	}

	protected void onDeactivate() {
		searchSection.deactivate();
		categorySelection.deactivate();
	}

}
