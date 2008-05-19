package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.EditableListenerAdapter;
import com.surelogic.sierra.gwt.client.util.UI;

public class CategoriesContent extends ContentComposite {
	private static final CategoriesContent instance = new CategoriesContent();
	private final CategoryCache categories = new CategoryCache();
	private final SearchBlock searchBlock = new SearchBlock(categories);
	private final CategoryBlock categoryBlock = new CategoryBlock();

	public static CategoriesContent getInstance() {
		return instance;
	}

	private CategoriesContent() {
		// singleton
	}

	protected void onInitialize(DockPanel rootPanel) {
		final Label title = UI.h2("Categories");
		rootPanel.add(title, DockPanel.NORTH);
		rootPanel.setCellHorizontalAlignment(title, DockPanel.ALIGN_LEFT);

		searchBlock.initialize();
		rootPanel.add(searchBlock, DockPanel.WEST);
		rootPanel.setCellWidth(searchBlock, "25%");

		categoryBlock.initialize();
		categoryBlock.addListener(new EditableListenerAdapter() {

			public void onSave(Widget sender, Object item) {
				categories.save((Category) item);
			}
		});
		rootPanel.add(categoryBlock, DockPanel.CENTER);
		rootPanel.setCellWidth(categoryBlock, "75%");

		categories.addListener(new CacheListenerAdapter() {

			public void onRefresh(Cache cache, Throwable failure) {
				searchBlock.refresh();
				refreshSelection(ContextManager.getContext());
			}

			public void onItemUpdate(Cache cache, Cacheable item,
					Status status, Throwable failure) {
				categories.refresh();

				if (failure == null && status.isSuccess()) {
					new CategoriesContext((Category) item).updateContext();
				} else if (!status.isSuccess()) {
					Window.alert("Save rejected: " + status.getMessage());
				} else if (failure != null) {
					Window.alert("Save failed: " + failure.getMessage());
				}
			}

		});
	}

	protected void onUpdate(Context context) {
		if (!isActive()) {
			categories.refresh();
		} else {
			refreshSelection(context);
		}
	}

	protected void onDeactivate() {
		searchBlock.clear();
	}

	private void refreshSelection(Context context) {
		final CategoriesContext rulesCtx = new CategoriesContext(context);
		final Category cat = (Category) categories.getItem(rulesCtx
				.getCategory());
		if (cat != null) {
			searchBlock.setSelection(cat);
			categoryBlock.setSelection(cat);
		} else if (categories.getItemCount() > 0) {
			new CategoriesContext((Category) categories.getItem(0))
					.updateContext();
		}
	}

}
