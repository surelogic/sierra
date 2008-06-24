package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilterComparator;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.FormDialog;
import com.surelogic.sierra.gwt.client.ui.ItemCheckBox;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingSelectionDialog extends FormDialog {
	private final Tree categoryTree = new Tree();
	private final TextBox searchBox = new TextBox();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		setText("Select Categories and/or Findings");
		setWidth("600px");

		final FlexTable searchTable = new FlexTable();
		searchTable.setWidth("50%");
		searchTable.setText(0, 0, "Search");
		searchBox.setWidth("100%");
		searchTable.setWidget(0, 1, searchBox);
		searchTable.getColumnFormatter().setWidth(0, "25%");
		searchTable.getColumnFormatter().setWidth(1, "75%");
		contentTable.setWidget(0, 0, searchTable);

		categoryTree.setWidth("100%");
		categoryTree.setHeight("425px");

		final ScrollPanel categoryScroller = new ScrollPanel(categoryTree);
		categoryScroller.setSize("100%", "auto");
		contentTable.setWidget(1, 0, categoryScroller);

		searchBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				search(searchBox.getText());
			}
		});
		searchBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search(searchBox.getText());
			}
		});

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
		for (final Category cat : categories) {
			if (!hasCategory(currentCategory, cat, new ArrayList<Category>())) {
				final ItemCheckBox<Category> catCheck = new ItemCheckBox<Category>(
						cat.getName(), cat);
				final TreeItem catItem = categoryTree.addItem(catCheck);
				catCheck.addClickListener(new CategoryCheckListener(catItem));
				final List<FindingTypeFilter> sortedFindings = new ArrayList<FindingTypeFilter>(
						cat.getEntries());
				Collections.sort(sortedFindings,
						new FindingTypeFilterComparator());
				for (final FindingTypeFilter finding : sortedFindings) {
					final ItemCheckBox<FindingTypeFilter> findingCheck = new ItemCheckBox<FindingTypeFilter>(
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

		for (final Category child : selectedCategory.getParents()) {
			if (hasCategory(child, testCategory, checked)) {
				return true;
			}
		}
		return false;
	}

	public Set<Category> getSelectedCategories() {
		final Set<Category> cats = new HashSet<Category>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			final TreeItem catItem = categoryTree.getItem(catIndex);

			boolean hasSelected = false;
			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				final TreeItem findingItem = catItem.getChild(findingIndex);
				final ItemCheckBox<?> findingCheck = (ItemCheckBox<?>) findingItem
						.getWidget();
				if (findingCheck.isChecked()) {
					hasSelected = true;
				}
			}
			if (hasSelected) {
				final ItemCheckBox<?> catCheck = (ItemCheckBox<?>) catItem
						.getWidget();
				cats.add((Category) catCheck.getItem());
			}
		}
		return cats;
	}

	public Set<FindingTypeFilter> getExcludedFindings() {
		final Set<FindingTypeFilter> excluded = new HashSet<FindingTypeFilter>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			final TreeItem catItem = categoryTree.getItem(catIndex);
			final Set<FindingTypeFilter> nonSelected = new HashSet<FindingTypeFilter>();
			boolean hasSelected = false;
			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				final TreeItem findingItem = catItem.getChild(findingIndex);
				final ItemCheckBox<?> findingCheck = (ItemCheckBox<?>) findingItem
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

	private void search(String query) {
		for (int i = 0; i < categoryTree.getItemCount(); i++) {
			updateSearchState(categoryTree.getItem(i), query);
		}
	}

	private boolean updateSearchState(TreeItem item, String query) {
		final boolean itemMatch = "".equals(query)
				|| LangUtil.containsIgnoreCase(item.getText(), query);

		boolean childContains = false;
		for (int i = 0; i < item.getChildCount(); i++) {
			if (updateSearchState(item.getChild(i), query)) {
				childContains = true;
			}
		}

		item.setVisible(itemMatch || childContains);
		item.setState(childContains);
		if (item.getChildCount() > 0 && childContains && !itemMatch) {
			item.getWidget().addStyleName("font-gray");
		} else {
			item.getWidget().removeStyleName("font-gray");
		}
		return itemMatch || childContains;
	}

	private class CategoryCheckListener implements ClickListener {
		private final TreeItem categoryItem;

		public CategoryCheckListener(TreeItem categoryItem) {
			super();
			this.categoryItem = categoryItem;
		}

		public void onClick(Widget sender) {
			if (sender instanceof ItemCheckBox<?>) {
				final ItemCheckBox<?> catCheckBox = (ItemCheckBox<?>) sender;
				final boolean checked = catCheckBox.isChecked();
				for (int filterIndex = 0; filterIndex < categoryItem
						.getChildCount(); filterIndex++) {
					final TreeItem filterItem = categoryItem
							.getChild(filterIndex);
					((ItemCheckBox<?>) filterItem.getWidget())
							.setChecked(checked);
				}

			}

		}

	}

}
