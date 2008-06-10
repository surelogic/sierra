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
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingTypeComparator;
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
		setTitle("Category");

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
			Set<FindingType> excludedFindings) {
		final Category updatedCat = category.copy();
		findingsEditor.saveTo(updatedCat);
		for (Category newCat : selectedCategories) {
			updatedCat.getParents().add(newCat.copy());
		}
		for (FindingType excludedFinding : excludedFindings) {
			FindingType newEntry = excludedFinding.copy();
			newEntry.setFiltered(true);
			updatedCat.getEntries().add(newEntry);
		}
		setCategory(updatedCat);
	}

	public FindingsEditor getFindingsEditor() {
		return findingsEditor;
	}

	public static class FindingsEditor extends BlockPanel {
		private final Map<FindingType, ItemCheckBox<FindingType>> findings = new HashMap<FindingType, ItemCheckBox<FindingType>>();
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
			final List<FindingType> catFindings = new ArrayList<FindingType>(
					category.getEntries());
			final FindingTypeComparator filterComparator = new FindingTypeComparator();
			Collections.sort(catFindings, filterComparator);

			for (FindingType finding : catFindings) {
				if (!category.parentContains(finding)) {
					contentPanel.add(createFindingUI(finding, finding
							.isFiltered()));
				}
			}

			// add findings that belong to the parent categories of the selected
			// category
			final Set<FindingType> excluded = category.getExcludedEntries();
			List<Category> sortedParents = new ArrayList<Category>(category
					.getParents());
			Collections.sort(sortedParents, new CategoryComparator());
			for (Category parent : sortedParents) {
				parentCategories.add(parent);

				final DisclosurePanel parentPanel = new DisclosurePanel(
						"From: " + parent.getName());
				contentPanel.add(parentPanel);
				final VerticalPanel findingsPanel = new VerticalPanel();
				parentPanel.setContent(findingsPanel);
				parentPanel.setOpen(true);

				final List<FindingType> parentFindings = new ArrayList<FindingType>(
						parent.getIncludedEntries());
				Collections.sort(parentFindings, filterComparator);
				for (FindingType finding : parentFindings) {
					findingsPanel.add(createFindingUI(finding, excluded
							.contains(finding)));
				}
			}
		}

		private ItemCheckBox<FindingType> createFindingUI(FindingType finding,
				boolean filtered) {
			final ItemCheckBox<FindingType> findingUI = new ItemCheckBox<FindingType>(
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
			for (Category parentCat : parentCategories) {
				targetParents.add(parentCat.copy());
			}

			// clone the selected category's entries
			final Set<FindingType> targetFindings = target.getEntries();
			targetFindings.clear();
			for (FindingType catFinding : category.getEntries()) {
				targetFindings.add(catFinding.copy());
			}

			// copy settings or clone the filter entries to the target
			final Set<FindingType> targetEntries = target.getEntries();
			for (Map.Entry<FindingType, ItemCheckBox<FindingType>> findingEntry : findings
					.entrySet()) {
				if (!findingEntry.getValue().isChecked()) {
					final FindingType uiFinding = findingEntry.getKey();
					FindingType targetFinding = findEntry(targetEntries,
							uiFinding.getUuid());
					if (targetFinding == null) {
						targetFinding = uiFinding.copy();
						targetEntries.add(targetFinding);
					}
					targetFinding.setFiltered(true);
				}
			}
		}

		private FindingType findEntry(Set<FindingType> targetEntries,
				String uuid) {
			for (FindingType finding : targetEntries) {
				if (finding.getUuid().equals(uuid)) {
					return finding;
				}
			}
			return null;
		}
	}

}
