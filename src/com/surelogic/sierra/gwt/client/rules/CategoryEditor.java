package com.surelogic.sierra.gwt.client.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
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
		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");

		categoryInfo.setText(0, 0, "Name:");
		nameEditText.setWidth("100%");
		categoryInfo.setWidget(0, 1, nameEditText);

		categoryInfo.setText(1, 0, "Description:");
		description.setVisibleLines(5);
		categoryInfo.setWidget(2, 0, description);
		categoryInfo.getFlexCellFormatter().setColSpan(1, 0, 3);

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

	private class FindingsEditor extends BlockPanel {
		private static final String PRIMARY_STYLE = CategoryBlock.PRIMARY_STYLE;

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle("Finding Types");
		}

		public void setCategory(Category cat) {
			final VerticalPanel contentPanel = getContentPanel();
			contentPanel.clear();

			// add the findings that belong to the selected category
			final List<FilterEntry> findings = new ArrayList<FilterEntry>(cat
					.getEntries());
			final FilterEntryComparator filterComparator = new FilterEntryComparator();
			Collections.sort(findings, filterComparator);

			for (FilterEntry finding : findings) {
				contentPanel
						.add(createFindingUI(finding, finding.isFiltered()));
			}

			// add findings that belong to the parent categories of the selected
			// category
			final Set<FilterEntry> excluded = cat.getExcludedEntries();
			for (Category parent : cat.getParents()) {
				final DisclosurePanel parentPanel = new DisclosurePanel(
						"From: " + parent.getName());
				contentPanel.add(parentPanel);
				final VerticalPanel findingsPanel = new VerticalPanel();
				parentPanel.setContent(findingsPanel);
				parentPanel.setOpen(true);

				final List<FilterEntry> parentFindings = new ArrayList<FilterEntry>(
						parent.getIncludedEntries());
				Collections.sort(parentFindings, filterComparator);
				for (FilterEntry finding : parentFindings) {
					findingsPanel.add(createFindingUI(finding, excluded
							.contains(finding)));
				}
			}
		}

		private ItemCheckBox<FilterEntry> createFindingUI(FilterEntry finding,
				boolean filtered) {
			final ItemCheckBox<FilterEntry> findingUI = new ItemCheckBox<FilterEntry>(
					finding.getName(), finding);
			findingUI.setTitle(finding.getShortMessage());
			findingUI.setChecked(!filtered);
			findingUI.addStyleName(PRIMARY_STYLE + "-finding");

			return findingUI;
		}

		public void saveTo(Category cat) {
			for (Iterator it = findings.entrySet().iterator(); it.hasNext();) {
				Entry findingEntry = (Entry) it.next();
				CheckBox ui = (CheckBox) findingEntry.getKey();
				FilterEntry finding = (FilterEntry) findingEntry.getValue();
				updateFilterEntry(cat.getEntries(), finding, !ui.isChecked());
			}
		}

		private void updateFilterEntry(Set entries, FilterEntry finding,
				boolean filtered) {
			final String findingUuid = finding.getUuid();
			for (Iterator it = entries.iterator(); it.hasNext();) {
				FilterEntry nextFilter = (FilterEntry) it.next();
				if (findingUuid.equals(nextFilter.getUuid())) {
					nextFilter.setFiltered(filtered);
					break;
				}
			}
		}

		private void addCategory() {
			// TODO need a dialog or UI update to add categories + findings
		}

		private class FilterEntryComparator implements Comparator<FilterEntry> {

			public int compare(FilterEntry o1, FilterEntry o2) {
				return o1.getName().compareTo(o2.getName());
			}
		}
	}
}
