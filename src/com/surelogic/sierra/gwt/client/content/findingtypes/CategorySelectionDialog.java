package com.surelogic.sierra.gwt.client.content.findingtypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.ui.FormDialog;
import com.surelogic.sierra.gwt.client.ui.ItemCheckBox;

public class CategorySelectionDialog extends FormDialog {
	private final Tree categoryTree = new Tree();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		setText("Select Categories");
		setWidth("500px");

		categoryTree.setWidth("100%");
		categoryTree.setHeight("425px");

		contentTable.setWidget(0, 0, categoryTree);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return categoryTree;
	}

	public void setCategories(CategoryCache categoryCache,
			List<String> excludeCategoryIds) {
		categoryTree.clear();
		for (final Category cat : categoryCache) {
			if (cat.isLocal() && !excludeCategoryIds.contains(cat.getUuid())) {
				final ItemCheckBox<Category> catCheck = new ItemCheckBox<Category>(
						cat.getName(), cat);
				categoryTree.addItem(catCheck);
			}
		}

		if (categoryTree.getItemCount() == 0) {
			final TreeItem item = categoryTree.addItem("No categories to add");
			item.addStyleName("font-italic");
			setOkEnabled(false);
		} else {
			setOkEnabled(true);
		}
	}

	public Set<Category> getSelectedCategories() {
		final Set<Category> cats = new HashSet<Category>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			final TreeItem catItem = categoryTree.getItem(catIndex);
			final ItemCheckBox<?> catCheck = (ItemCheckBox<?>) catItem
					.getWidget();
			if (catCheck.isChecked()) {
				cats.add((Category) catCheck.getItem());
			}
		}
		return cats;
	}

	@Override
	protected void doOkClick() {
		setStatus(Status.success());
		hide();
	}

}
