package com.surelogic.sierra.gwt.client.content.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.ItemWidget;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel.SearchListener;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingSelectionDialog extends FormDialog {
	private final List<CategoryEntry> categories = new ArrayList<CategoryEntry>();
	private final Tree categoryTree = new Tree();
	private final HorizontalPanel filterPanel = new HorizontalPanel();
	private final SearchInputPanel searchPanel = new SearchInputPanel();
	private final CheckBox showCategories = new CheckBox("Show Categories");

	public FindingSelectionDialog(final String title) {
		super(title, "600px");
	}

	@Override
	protected final void doInitialize(final FlexTable contentTable) {
		filterPanel.setWidth("100%");

		searchPanel.setWidth("100%");
		filterPanel.add(searchPanel);
		filterPanel.setCellWidth(searchPanel, "50%");

		showCategories.addStyleName("padded");
		showCategories.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				refreshUI();
			}

		});

		filterPanel.add(showCategories);
		filterPanel.setCellWidth(showCategories, "50%");
		filterPanel.setCellHorizontalAlignment(showCategories,
				HasHorizontalAlignment.ALIGN_RIGHT);
		filterPanel.setCellVerticalAlignment(showCategories,
				HasVerticalAlignment.ALIGN_MIDDLE);

		contentTable.setWidget(0, 0, filterPanel);

		categoryTree.setWidth("100%");
		categoryTree.setHeight("425px");

		final ScrollPanel categoryScroller = new ScrollPanel(categoryTree);
		categoryScroller.addStyleName("outlined-widget");
		categoryScroller.setSize("100%", "auto");
		contentTable.setWidget(1, 0, categoryScroller);

		searchPanel.addListener(new SearchListener() {

			public void onSearch(final SearchInputPanel sender,
					final String text) {
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
		categories.add(new CategoryEntry(cat, excludeFindings));
	}

	public void refreshUI() {
		categoryTree.clear();
		if (showCategories.isChecked()) {
			refreshWithCategories();
		} else {
			refreshNoCategories();
		}
	}

	private void refreshWithCategories() {
		for (final CategoryEntry entry : categories) {
			final Category cat = entry.getCategory();
			final Set<String> excludeFindings = entry.getExcludeFindings();

			final CategoryCheckBox catCheck = new CategoryCheckBox(cat);
			catCheck.setTitle(cat.getInfo());
			final TreeItem catItem = categoryTree.addItem(catCheck);
			catCheck.getUI().addClickListener(
					new CategoryCheckListener(catItem));

			final List<FindingTypeFilter> sortedFindings = new ArrayList<FindingTypeFilter>(
					cat.getEntries());
			Collections.sort(sortedFindings);

			for (final FindingTypeFilter finding : sortedFindings) {
				if (excludeFindings == null
						|| !isExcluded(finding.getUuid(), excludeFindings)) {
					final FindingCheckBox findingCheck = new FindingCheckBox(
							finding, cat);
					findingCheck.setTitle(finding.getShortMessage());
					catItem.addItem(findingCheck);
				}
			}

			// don't show empty categories
			if (catItem.getChildCount() == 0) {
				catItem.remove();
			}
		}
	}

	private void refreshNoCategories() {
		final List<FindingCheckBox> findings = new ArrayList<FindingCheckBox>();

		for (final CategoryEntry entry : categories) {
			final Category cat = entry.getCategory();
			final Set<String> excludeFindings = entry.getExcludeFindings();

			for (final FindingTypeFilter finding : cat.getEntries()) {
				if (excludeFindings == null
						|| !isExcluded(finding.getUuid(), excludeFindings)) {
					final FindingCheckBox findingCheck = new FindingCheckBox(
							finding, cat);
					findingCheck.setTitle(finding.getShortMessage());
					findings.add(findingCheck);
				}
			}
		}
		Collections.sort(findings, new Comparator<FindingCheckBox>() {

			public int compare(final FindingCheckBox o1,
					final FindingCheckBox o2) {
				return o1.getItem().compareTo(o2.getItem());
			}
		});

		for (final FindingCheckBox finding : findings) {
			categoryTree.addItem(finding);
		}
	}

	public final Set<Category> getSelectedCategories() {
		final Set<Category> cats = new HashSet<Category>();

		for (int catIndex = 0; catIndex < categoryTree.getItemCount(); catIndex++) {
			final TreeItem catItem = categoryTree.getItem(catIndex);

			boolean hasSelected = false;
			for (int findingIndex = 0; findingIndex < catItem.getChildCount(); findingIndex++) {
				final TreeItem findingItem = catItem.getChild(findingIndex);
				final FindingCheckBox findingCheck = (FindingCheckBox) findingItem
						.getWidget();
				if (findingCheck.getUI().isChecked()) {
					hasSelected = true;
				}
			}
			if (hasSelected) {
				final Category cat = ((CategoryCheckBox) catItem.getWidget())
						.getItem();
				cats.add(cat);
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
				final FindingCheckBox findingCheckBox = (FindingCheckBox) findingItem
						.getWidget();
				if (findingCheckBox.getUI().isChecked()) {
					selected.add(findingCheckBox.getItem());
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
				final FindingCheckBox findingCheckBox = (FindingCheckBox) findingItem
						.getWidget();
				if (findingCheckBox.getUI().isChecked()) {
					hasSelected = true;
				} else {
					nonSelected.add(findingCheckBox.getItem());
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
			StyleHelper.add(item.getWidget(), Style.GRAY);
		} else {
			StyleHelper.remove(item.getWidget(), Style.GRAY);
		}
		return itemMatch || childContains;
	}

	private class CategoryEntry {
		private final Category category;
		private final Set<String> excludeFindings;

		public CategoryEntry(final Category category,
				final Set<String> excludeFindings) {
			super();
			this.category = category;
			this.excludeFindings = excludeFindings;
		}

		public Category getCategory() {
			return category;
		}

		public Set<String> getExcludeFindings() {
			return excludeFindings;
		}
	}

	private class CategoryCheckListener implements ClickListener {
		private final TreeItem categoryItem;

		public CategoryCheckListener(final TreeItem categoryItem) {
			super();
			this.categoryItem = categoryItem;
		}

		public void onClick(final Widget sender) {
			if (sender instanceof CheckBox) {
				final CheckBox catCheckBox = (CheckBox) sender;
				final boolean checked = catCheckBox.isChecked();
				for (int filterIndex = 0; filterIndex < categoryItem
						.getChildCount(); filterIndex++) {
					final TreeItem filterItem = categoryItem
							.getChild(filterIndex);
					((FindingCheckBox) filterItem.getWidget()).getUI()
							.setChecked(checked);
				}

			}

		}
	}

	private class CategoryCheckBox extends ItemWidget<CheckBox, Category> {

		public CategoryCheckBox(final Category item) {
			super(new CheckBox(item.getName()), item);
		}

	}

	private class FindingCheckBox extends
			ItemWidget<CheckBox, FindingTypeFilter> {
		private final Category category;

		public FindingCheckBox(final FindingTypeFilter item,
				final Category category) {
			super(new CheckBox(item.getName()), item);
			this.category = category;
		}

		public Category getCategory() {
			return category;
		}
	}
}
