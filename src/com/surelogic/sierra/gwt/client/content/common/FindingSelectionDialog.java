package com.surelogic.sierra.gwt.client.content.common;

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
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.ItemCheckBox;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel.SearchListener;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingSelectionDialog extends FormDialog {
	private final Tree categoryTree = new Tree();
	private final SearchInputPanel searchPanel = new SearchInputPanel();

	public FindingSelectionDialog(final String title) {
		super(title, "600px");
	}

	@Override
	protected final void doInitialize(final FlexTable contentTable) {
		searchPanel.setWidth("50%");
		contentTable.setWidget(0, 0, searchPanel);

		categoryTree.setWidth("100%");
		categoryTree.setHeight("425px");

		final ScrollPanel categoryScroller = new ScrollPanel(categoryTree);
		categoryScroller.setSize("100%", "auto");
		contentTable.setWidget(1, 0, categoryScroller);

		searchPanel.addListener(new SearchListener() {

			public void onSearch(final SearchInputPanel sender, final String text) {
				search(text);
			}
		});
	}

	@Override
	protected final HasFocus getInitialFocus() {
		return categoryTree;
	}

	public final void clearFindings() {
		categoryTree.clear();
	}

	public final void addWaitStatus() {
		categoryTree.addItem(ImageHelper.getWaitImage(16));
	}

	public final void addCategory(final Category cat,
			final Set<String> excludeFindings) {
		final ItemCheckBox<Category> catCheck = new ItemCheckBox<Category>(cat
				.getName(), cat);
		final TreeItem catItem = categoryTree.addItem(catCheck);
		catCheck.addClickListener(new CategoryCheckListener(catItem));

		final List<FindingTypeFilter> sortedFindings = new ArrayList<FindingTypeFilter>(
				cat.getEntries());
		Collections.sort(sortedFindings);

		for (final FindingTypeFilter finding : sortedFindings) {
			if (excludeFindings == null
					|| !isExcluded(finding.getUuid(), excludeFindings)) {
				final ItemCheckBox<FindingTypeFilter> findingCheck = new ItemCheckBox<FindingTypeFilter>(
						finding.getName(), finding);
				catItem.addItem(findingCheck);
			}
		}

		// don't show empty categories
		if (catItem.getChildCount() == 0) {
			catItem.remove();
		}
	}

	public final Set<Category> getSelectedCategories() {
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

	public final Set<FindingTypeFilter> getSelectedFindings() {
		final Set<FindingTypeFilter> selected = new HashSet<FindingTypeFilter>();
		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			final TreeItem catItem = categoryTree.getItem(catIndex);

			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				final TreeItem findingItem = catItem.getChild(findingIndex);
				final ItemCheckBox<?> findingCheck = (ItemCheckBox<?>) findingItem
						.getWidget();
				if (findingCheck.isChecked()) {
					selected.add((FindingTypeFilter) findingCheck.getItem());
				}
			}
		}
		return selected;
	}

	public final Set<FindingTypeFilter> getExcludedFindings() {
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

	protected final boolean isExcluded(final String uuid,
			final Set<String> excludedSet) {
		return excludedSet.contains(uuid);
	}

	private void search(final String query) {
		for (int i = 0; i < categoryTree.getItemCount(); i++) {
			updateSearchState(categoryTree.getItem(i), query);
		}
	}

	private boolean updateSearchState(final TreeItem item, final String query) {
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

		public CategoryCheckListener(final TreeItem categoryItem) {
			super();
			this.categoryItem = categoryItem;
		}

		public void onClick(final Widget sender) {
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
