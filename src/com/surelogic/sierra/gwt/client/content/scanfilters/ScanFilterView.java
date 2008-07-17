package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ContentLink;
import com.surelogic.sierra.gwt.client.ui.ItalicLabel;

public class ScanFilterView extends BlockPanel {
	private final VerticalPanel importanceBlocks = new VerticalPanel();
	private ScanFilter selection;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		importanceBlocks.setWidth("100%");
		contentPanel.add(importanceBlocks);
	}

	public ScanFilter getSelection() {
		return selection;
	}

	public void setSelection(ScanFilter filter) {
		selection = filter;
		importanceBlocks.clear();

		if (selection != null) {
			setSummary(selection.getName());

			final Map<ImportanceView, FilterBlock> blocksByImportance = new HashMap<ImportanceView, FilterBlock>();

			final Comparator<ScanFilterEntry> filterCompare = new Comparator<ScanFilterEntry>() {

				public int compare(ScanFilterEntry o1, ScanFilterEntry o2) {
					return o1.getName().toLowerCase().compareTo(
							o2.getName().toLowerCase());
				}
			};
			final List<ScanFilterEntry> sortedCategories = new ArrayList<ScanFilterEntry>(
					filter.getCategories());
			Collections.sort(sortedCategories, filterCompare);
			for (final ScanFilterEntry category : sortedCategories) {
				final FilterBlock filterList = getFilterBlock(
						blocksByImportance, category.getImportance());
				filterList.addFilterEntry(category);
			}

			final List<ScanFilterEntry> sortedFindings = new ArrayList<ScanFilterEntry>(
					filter.getTypes());
			Collections.sort(sortedFindings, filterCompare);
			for (final ScanFilterEntry category : sortedFindings) {
				final FilterBlock filterList = getFilterBlock(
						blocksByImportance, category.getImportance());
				filterList.addFilterEntry(category);
			}

			final ImportanceView[] importances = ImportanceView.values();

			for (int i = importances.length - 1; i >= 0; i--) {
				final FilterBlock block = blocksByImportance
						.get(importances[i]);
				if (block != null) {
					importanceBlocks.add(block);
				}
			}
			final FilterBlock defaultBlock = blocksByImportance.get(null);
			if (defaultBlock != null) {
				importanceBlocks.add(defaultBlock);
			}
			if (importanceBlocks.getWidgetCount() == 0) {
				importanceBlocks.add(new ItalicLabel("None"));
			}
		} else {
			setSummary("Select a Scan Filter");
		}
	}

	private FilterBlock getFilterBlock(
			Map<ImportanceView, FilterBlock> blocksByImportance,
			ImportanceView importance) {
		FilterBlock block = blocksByImportance.get(importance);
		if (block == null) {
			final String importanceTitle = importance == null ? "Default"
					: importance.getName();
			block = new FilterBlock(importanceTitle);
			block.initialize();
			blocksByImportance.put(importance, block);
		}
		return block;
	}

	private static class FilterBlock extends BlockPanel {
		private final VerticalPanel categoriesLeft = new VerticalPanel();
		private final VerticalPanel categoriesRight = new VerticalPanel();
		private final VerticalPanel findingsLeft = new VerticalPanel();
		private final VerticalPanel findingsRight = new VerticalPanel();

		public FilterBlock(String priorityName) {
			super();
			setTitle("Priority");
			setSummary(priorityName);
			setSubsectionStyle(true);
		}

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			final Grid cfDivider = new Grid(2, 2);
			cfDivider.setWidth("100%");
			cfDivider.getColumnFormatter().setWidth(0, "50%");
			cfDivider.getColumnFormatter().setWidth(1, "50%");
			cfDivider.setWidget(0, 0, categoriesLeft);
			cfDivider.setWidget(0, 1, categoriesRight);
			cfDivider.setWidget(1, 0, findingsLeft);
			cfDivider.setWidget(1, 1, findingsRight);
			contentPanel.add(cfDivider);
		}

		public void addFilterEntry(ScanFilterEntry entry) {
			ContentComposite content;
			if (entry.isCategory()) {
				content = CategoriesContent.getInstance();
			} else {
				content = FindingTypesContent.getInstance();
			}
			final ContentLink entryLink = new ContentLink(entry.getName(),
					content, entry.getUuid());
			entryLink.setTitle(entry.getShortMessage());
			if (entry.isCategory()) {
				addEntry(categoriesLeft, categoriesRight, entryLink);
			} else {
				addEntry(findingsLeft, findingsRight, entryLink);
			}

		}

		private void addEntry(VerticalPanel leftPanel,
				VerticalPanel rightPanel, ContentLink entryLink) {
			if (leftPanel.getWidgetCount() <= rightPanel.getWidgetCount()) {
				leftPanel.add(entryLink);
			} else {
				rightPanel.add(entryLink);
			}
		}
	}
}
