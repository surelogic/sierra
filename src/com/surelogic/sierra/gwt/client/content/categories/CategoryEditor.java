package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.CategoryComparator;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilterComparator;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemCheckBox;

public class CategoryEditor extends BlockPanel {
	public static final String PRIMARY_STYLE = "categories-category";
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	private final FindingsEditor findingsEditor = new FindingsEditor();

	private Category category;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		categoryInfo.setWidth("100%");

		categoryInfo.setText(0, 0, "Name:");
		nameEditText.setWidth("100%");
		categoryInfo.setWidget(0, 1, nameEditText);

		categoryInfo.setText(1, 0, "Description:");
		description.setVisibleLines(5);
		categoryInfo.setWidget(2, 0, description);

		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.getFlexCellFormatter().setColSpan(2, 0, 3);

		contentPanel.add(categoryInfo);

		findingsEditor.setSubsectionStyle(true);
		findingsEditor.initialize();
		contentPanel.add(findingsEditor);

	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category cat) {
		category = cat;
		setSummary(category.getName());
		nameEditText.setText(category.getName());
		String catInfo = category.getInfo();
		if (catInfo == null) {
			catInfo = "";
		}
		description.setText(catInfo);
		findingsEditor.setCategory(category);
	}

	public Category getUpdatedCategory() {
		final Category cat = category.copy();

		cat.setName(nameEditText.getText());
		cat.setInfo(description.getText());

		findingsEditor.saveTo(cat);

		return cat;
	}

	public void addFindings(Set<Category> selectedCategories,
			Set<FindingTypeFilter> excludedFindings) {
		final Category updatedCat = getUpdatedCategory();

		for (final Category newCat : selectedCategories) {
			updatedCat.getParents().add(newCat.copy());
		}
		for (final FindingTypeFilter excludedFinding : excludedFindings) {
			final FindingTypeFilter newEntry = excludedFinding.copy();
			newEntry.setFiltered(true);
			updatedCat.getEntries().add(newEntry);
		}
		setCategory(updatedCat);
	}

	public FindingsEditor getFindingsEditor() {
		return findingsEditor;
	}

	public static class FindingsEditor extends BlockPanel {
		private final Map<FindingTypeFilter, ItemCheckBox<FindingTypeFilter>> findings = new HashMap<FindingTypeFilter, ItemCheckBox<FindingTypeFilter>>();
		private final List<Category> parentCategories = new ArrayList<Category>();
		private Category category;

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle("Finding Types");
		}

		public void setCategory(Category cat) {
			this.category = cat;

			final VerticalPanel contentPanel = getContentPanel();
			contentPanel.clear();
			findings.clear();
			parentCategories.clear();

			// add the findings that belong to the selected category
			final List<FindingTypeFilter> catFindings = new ArrayList<FindingTypeFilter>(
					category.getEntries());
			final FindingTypeFilterComparator filterComparator = new FindingTypeFilterComparator();
			Collections.sort(catFindings, filterComparator);

			for (final FindingTypeFilter finding : catFindings) {
				if (!category.parentContains(finding)) {
					contentPanel.add(createFindingUI(finding, finding
							.isFiltered()));
				}
			}

			// add findings that belong to the parent categories of the selected
			// category
			final Set<FindingTypeFilter> excluded = category
					.getExcludedEntries();
			final List<Category> sortedParents = new ArrayList<Category>(
					category.getParents());
			Collections.sort(sortedParents, new CategoryComparator());
			for (final Category parent : sortedParents) {
				parentCategories.add(parent);

				final DisclosurePanel parentPanel = new DisclosurePanel(
						"From: " + parent.getName());
				contentPanel.add(parentPanel);
				final VerticalPanel findingsPanel = new VerticalPanel();
				parentPanel.setContent(findingsPanel);
				parentPanel.setOpen(true);

				final List<FindingTypeFilter> parentFindings = new ArrayList<FindingTypeFilter>(
						parent.getIncludedEntries());
				Collections.sort(parentFindings, filterComparator);
				for (final FindingTypeFilter finding : parentFindings) {
					findingsPanel.add(createFindingUI(finding, excluded
							.contains(finding)));
				}
			}
		}

		private ItemCheckBox<FindingTypeFilter> createFindingUI(
				FindingTypeFilter finding, boolean filtered) {
			final ItemCheckBox<FindingTypeFilter> findingUI = new ItemCheckBox<FindingTypeFilter>(
					finding.getName(), finding);
			findingUI.setTitle(finding.getShortMessage());
			findingUI.setChecked(!filtered);
			findingUI.addStyleName(PRIMARY_STYLE + "-finding");
			findings.put(finding, findingUI);
			return findingUI;
		}

		public void saveTo(Category target) {
			// clone the parent categories from the UI into the target
			final Set<Category> targetParents = target.getParents();
			targetParents.clear();
			for (final Category parentCat : parentCategories) {
				targetParents.add(parentCat.copy());
			}

			// clone the selected category's entries
			final Set<FindingTypeFilter> targetFindings = target.getEntries();
			targetFindings.clear();
			for (final FindingTypeFilter catFinding : category.getEntries()) {
				targetFindings.add(catFinding.copy());
			}

			// copy settings or clone the filter entries to the target
			final Set<FindingTypeFilter> targetEntries = target.getEntries();
			for (final Map.Entry<FindingTypeFilter, ItemCheckBox<FindingTypeFilter>> findingEntry : findings
					.entrySet()) {
				if (!findingEntry.getValue().isChecked()) {
					final FindingTypeFilter uiFinding = findingEntry.getKey();
					FindingTypeFilter targetFinding = findEntry(targetEntries,
							uiFinding.getUuid());
					if (targetFinding == null) {
						targetFinding = uiFinding.copy();
						targetEntries.add(targetFinding);
					}
					targetFinding.setFiltered(true);
				}
			}
		}

		private FindingTypeFilter findEntry(
				Set<FindingTypeFilter> targetEntries, String uuid) {
			for (final FindingTypeFilter finding : targetEntries) {
				if (finding.getUuid().equals(uuid)) {
					return finding;
				}
			}
			return null;
		}
	}

}
