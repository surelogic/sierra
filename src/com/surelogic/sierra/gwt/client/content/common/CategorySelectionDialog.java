package com.surelogic.sierra.gwt.client.content.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.ItemWidget;
import com.surelogic.sierra.gwt.client.ui.LabelHelper;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class CategorySelectionDialog extends FormDialog {
	private final VerticalPanel categoryPanel = new VerticalPanel();

	public CategorySelectionDialog() {
		super("Select Categories", "500px");
	}

	@Override
	protected void doInitialize(final FlexTable contentTable) {
		categoryPanel.setWidth("100%");

		final ScrollPanel categoryScroller = new ScrollPanel(categoryPanel);
		categoryScroller.setWidth("100%");
		categoryScroller.setAlwaysShowScrollBars(true);
		categoryScroller.setHeight("425px");

		contentTable.setWidget(0, 0, categoryScroller);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return null;
	}

	public void update(final List<String> excludeCategoryIds,
			final boolean showLocal) {
		final CategoryCache categories = CategoryCache.getInstance();

		final CacheListener<Category> cacheListener = new CacheListenerAdapter<Category>() {
			@Override
			public void onStartRefresh(final Cache<Category> cache) {
				categoryPanel.add(ImageHelper.getWaitImage(16));
			}

			@Override
			public void onRefresh(final Cache<Category> cache,
					final Throwable failure) {
				categories.removeListener(this);

				categoryPanel.clear();
				for (final Category cat : categories) {
					if ((cat.isLocal() || showLocal)
							&& !excludeCategoryIds.contains(cat.getUuid())) {
						final CategoryCheckBox catCheck = new CategoryCheckBox(
								cat);
						categoryPanel.add(catCheck);
					}
				}

				if (categoryPanel.getWidgetCount() == 0) {
					categoryPanel.add(LabelHelper.italics(new Label(
							"No categories to add")));
					setOkEnabled(false);
				} else {
					setOkEnabled(true);
				}
			}

		};

		categories.addListener(cacheListener);
		categories.refresh();
	}

	public Set<Category> getSelectedCategories() {
		final Set<Category> cats = new HashSet<Category>();
		for (int catIndex = 0; catIndex < categoryPanel.getWidgetCount(); catIndex++) {
			final CategoryCheckBox catCheck = (CategoryCheckBox) categoryPanel
					.getWidget(catIndex);
			if (catCheck.getUI().isChecked()) {
				cats.add(catCheck.getItem());
			}
		}
		return cats;
	}

	private class CategoryCheckBox extends ItemWidget<CheckBox, Category> {

		public CategoryCheckBox(final Category item) {
			super(new CheckBox(item.getName()), item);
		}

	}
}
