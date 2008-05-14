package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.util.LangUtil;
import com.surelogic.sierra.gwt.client.util.UI;

public class CategoryContent extends ContentComposite {
	private static final CategoryContent instance = new CategoryContent();
	private final CategoryCache categories = new CategoryCache();
	private final SearchSection searchSection = new SearchSection(categories);

	private final VerticalPanel selectionPanel = new VerticalPanel();
	private final CategoryBlock categorySelection = new CategoryBlock(
			categories);
	private final FindingTypeBlock findingSelection = new FindingTypeBlock();

	public static CategoryContent getInstance() {
		return instance;
	}

	private CategoryContent() {
		// singleton
	}

	protected void onInitialize(DockPanel rootPanel) {
		final Label title = UI.h2("Categories");
		rootPanel.add(title, DockPanel.NORTH);
		rootPanel.setCellHorizontalAlignment(title, DockPanel.ALIGN_LEFT);

		selectionPanel.setWidth("100%");
		selectionPanel.add(new Label(""));

		rootPanel.add(searchSection, DockPanel.WEST);
		rootPanel.setCellWidth(searchSection, "25%");
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");

		categories.addListener(new CacheListener() {

			public void onStartRefresh(Cache cache) {
				// nothing to do
			}

			public void onRefresh(Cache cache, Throwable failure) {
				refreshSelection();
			}

			public void onItemUpdate(Cache cache, Cacheable item,
					Status status, Throwable failure) {
				refreshSelection();
			}

		});
	}

	private void refreshSelection() {
		final Context context = ContextManager.getContext();
		final CategoryContext rulesCtx = new CategoryContext(context);
		final String categoryUuid = rulesCtx.getCategory();
		final String findingUuid = rulesCtx.getFinding();
		boolean selectionMade = false;
		if (LangUtil.notEmpty(categoryUuid)) {
			if (selectionPanel.getWidgetIndex(categorySelection) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categorySelection);
			}
			categorySelection.update(context);

			selectionMade = true;
		} else if (LangUtil.notEmpty(findingUuid)) {
			if (selectionPanel.getWidgetIndex(findingSelection) == -1) {
				selectionPanel.clear();
				selectionPanel.add(findingSelection);
			}
			findingSelection.update(context);

			selectionMade = true;
		}

		if (!selectionMade && categories.getItemCount() > 0) {
			new CategoryContext((Category) categories.getItem(0))
					.updateContext();
		}
	}

	protected void onUpdate(Context context) {
		searchSection.update(context);

		if (!isActive()) {
			categories.refresh();
		} else {
			refreshSelection();
		}
	}

	protected void onDeactivate() {
		searchSection.deactivate();

		if (categorySelection.isActive()) {
			categorySelection.deactivate();
		}
		if (findingSelection.isActive()) {
			findingSelection.deactivate();
		}
	}

}
