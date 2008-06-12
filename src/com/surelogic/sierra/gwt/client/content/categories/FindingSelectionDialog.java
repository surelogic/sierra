package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilterComparator;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.FormDialog;
import com.surelogic.sierra.gwt.client.ui.ItemCheckBox;

public class FindingSelectionDialog extends FormDialog {
	private final Tree categoryTree = new Tree();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		setText("Select Categories and/or Findings");
		setWidth("100%");

		categoryTree.setWidth("500px");
		categoryTree.setHeight("425px");

		final ScrollPanel categoryScroller = new ScrollPanel(categoryTree);
		categoryScroller.setSize("100%", "auto");
		contentTable.setWidget(0, 0, categoryScroller);

	}

	@Override
	protected HasFocus getInitialFocus() {
		return categoryTree;
	}

	@Override
	protected void doOkClick() {
		setStatus(Status.success());
		hide();
	}

	public void setCategories(CategoryCache categories, Category currentCategory) {
		categoryTree.clear();
		for (Category cat : categories) {
			if (!hasCategory(currentCategory, cat, new ArrayList<Category>())) {
				ItemCheckBox<Category> catCheck = new ItemCheckBox<Category>(
						cat.getName(), cat);
				final TreeItem catItem = categoryTree.addItem(catCheck);
				catCheck.addClickListener(new CategoryCheckListener(catItem));
				List<FindingTypeFilter> sortedFindings = new ArrayList<FindingTypeFilter>(
						cat.getEntries());
				Collections.sort(sortedFindings, new FindingTypeFilterComparator());
				for (FindingTypeFilter finding : sortedFindings) {
					ItemCheckBox<FindingTypeFilter> findingCheck = new ItemCheckBox<FindingTypeFilter>(
							finding.getName(), finding);
					catItem.addItem(findingCheck);
				}
			}
		}
	}

	private boolean hasCategory(Category selectedCategory,
			Category testCategory, List<Category> checked) {
		// prevent infinite recursion
		if (checked.contains(selectedCategory)) {
			return false;
		} else {
			checked.add(selectedCategory);
		}

		if (selectedCategory.equals(testCategory)) {
			return true;
		}

		for (Category child : selectedCategory.getParents()) {
			if (hasCategory(child, testCategory, checked)) {
				return true;
			}
		}
		return false;
	}

	public Set<Category> getSelectedCategories() {
		Set<Category> cats = new HashSet<Category>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			TreeItem catItem = categoryTree.getItem(catIndex);

			boolean hasSelected = false;
			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				TreeItem findingItem = catItem.getChild(findingIndex);
				ItemCheckBox<?> findingCheck = (ItemCheckBox<?>) findingItem
						.getWidget();
				if (findingCheck.isChecked()) {
					hasSelected = true;
				}
			}
			if (hasSelected) {
				ItemCheckBox<?> catCheck = (ItemCheckBox<?>) catItem
						.getWidget();
				cats.add((Category) catCheck.getItem());
			}
		}
		return cats;
	}

	public Set<FindingTypeFilter> getExcludedFindings() {
		Set<FindingTypeFilter> excluded = new HashSet<FindingTypeFilter>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			TreeItem catItem = categoryTree.getItem(catIndex);
			Set<FindingTypeFilter> nonSelected = new HashSet<FindingTypeFilter>();
			boolean hasSelected = false;
			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				TreeItem findingItem = catItem.getChild(findingIndex);
				ItemCheckBox<?> findingCheck = (ItemCheckBox<?>) findingItem
						.getWidget();
				if (findingCheck.isChecked()) {
					hasSelected = true;
				} else {
					nonSelected.add((FindingTypeFilter) findingCheck.getItem());
				}
			}
			if (hasSelected) {
				excluded.addAll(nonSelected);
			}
		}
		return excluded;
	}

	private class CategoryCheckListener implements ClickListener {
		private final TreeItem categoryItem;

		public CategoryCheckListener(TreeItem categoryItem) {
			super();
			this.categoryItem = categoryItem;
		}

		public void onClick(Widget sender) {
			if (sender instanceof ItemCheckBox<?>) {
				ItemCheckBox<?> catCheckBox = (ItemCheckBox<?>) sender;
				final boolean checked = catCheckBox.isChecked();
				for (int filterIndex = 0; filterIndex < categoryItem
						.getChildCount(); filterIndex++) {
					TreeItem filterItem = categoryItem.getChild(filterIndex);
					((ItemCheckBox<?>) filterItem.getWidget())
							.setChecked(checked);
				}

			}

		}

	}

}
