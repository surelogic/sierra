package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class ScanFilterView extends BlockPanel {
	private final VerticalPanel importanceBlocks = new VerticalPanel();
	private ScanFilter selection;
	private boolean showCategories = true;
	private boolean refreshedCategories;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		importanceBlocks.setWidth("100%");
		contentPanel.add(importanceBlocks);
	}

	public ScanFilter getSelection() {
		return selection;
	}

	public void setSelection(final ScanFilter filter) {
		if (showCategories && !refreshedCategories) {
			refreshedCategories = true;
			refreshCategories(filter);
			return;
		}

		selection = filter;
		importanceBlocks.clear();

		if (selection != null) {
			setSummary(selection.getName());

			final Map<ImportanceView, FilterBlock> categoryImportanceBlocks = new HashMap<ImportanceView, FilterBlock>();
			final Set<ScanFilterEntry> categoryFindings = new HashSet<ScanFilterEntry>();
			if (showCategories) {
				final List<ScanFilterEntry> sortedCategories = new ArrayList<ScanFilterEntry>(
						filter.getCategories());
				Collections.sort(sortedCategories);
				for (final ScanFilterEntry category : sortedCategories) {
					final FilterBlock filterList = getFilterBlock(
							categoryImportanceBlocks, category.getImportance(),
							true);
					filterList.addFilterEntry(category);
				}
			} else {
				final List<ScanFilterEntry> filterCategories = new ArrayList<ScanFilterEntry>(
						filter.getCategories());
				final CategoryCache categories = CategoryCache.getInstance();
				for (final ScanFilterEntry filterCategory : filterCategories) {
					final Category cat = categories.getItem(filterCategory
							.getUuid());
					for (final FindingTypeFilter finding : cat
							.getIncludedEntries()) {
						categoryFindings.add(new ScanFilterEntry(finding,
								filterCategory.getImportance()));
					}
				}
			}

			final Map<ImportanceView, FilterBlock> findingImportanceBlocks = new HashMap<ImportanceView, FilterBlock>();
			final List<ScanFilterEntry> sortedFindings = new ArrayList<ScanFilterEntry>(
					filter.getTypes());
			if (!categoryFindings.isEmpty()) {
				sortedFindings.addAll(categoryFindings);
			}
			Collections.sort(sortedFindings);
			for (final ScanFilterEntry finding : sortedFindings) {
				final FilterBlock filterList = getFilterBlock(
						findingImportanceBlocks, finding.getImportance(), false);
				filterList.addFilterEntry(finding);
			}

			final ImportanceView[] importances = ImportanceView.values();

			for (int i = importances.length - 1; i >= 0; i--) {
				addBlocks(importances[i], categoryImportanceBlocks,
						findingImportanceBlocks);

			}
			addBlocks(null, categoryImportanceBlocks, findingImportanceBlocks);

			if (importanceBlocks.getWidgetCount() == 0) {
				importanceBlocks.add(StyleHelper.add(new Label(
						"No categories or findings in this Scan Filter."),
						Style.ITALICS));
			}
		} else {
			setSummary("Select a Scan Filter");
		}
	}

	public void toggleCategories() {
		showCategories = !showCategories;
		setSelection(selection);
	}

	public boolean isShowingCategories() {
		return showCategories;
	}

	private void refreshCategories(final ScanFilter filter) {
		CategoryCache.getInstance().addListener(
				new CacheListenerAdapter<Category>() {

					@Override
					public void onRefresh(final Cache<Category> cache,
							final Throwable failure) {
						setSelection(filter);
					}
				});
		CategoryCache.getInstance().refresh(false);
	}

	private void addBlocks(final ImportanceView importance,
			final Map<ImportanceView, FilterBlock> categoryBlocks,
			final Map<ImportanceView, FilterBlock> findingBlocks) {
		final FilterBlock categoryBlock = categoryBlocks.get(importance);
		final FilterBlock findingBlock = findingBlocks.get(importance);
		if (categoryBlock != null || findingBlock != null) {
			final String importanceTitle = importance == null ? "Default"
					: importance.getName();
			importanceBlocks.add(StyleHelper.add(new Label(importanceTitle
					+ " Priority", false), Style.STRONG));
		}
		if (categoryBlock != null) {
			importanceBlocks.add(categoryBlock);
		}

		if (findingBlock != null) {
			importanceBlocks.add(findingBlock);
		}
	}

	private FilterBlock getFilterBlock(
			final Map<ImportanceView, FilterBlock> blocksByImportance,
			final ImportanceView importance, final boolean isCategory) {
		FilterBlock block = blocksByImportance.get(importance);
		if (block == null) {
			block = new FilterBlock(isCategory);
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

		public FilterBlock(final boolean categories) {
			super();
			setTitle(categories ? "Categories" : "Findings");
			setSubsectionStyle(true);
		}

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			final Grid cfDivider = new Grid(2, 2);
			cfDivider.setWidth("100%");
			cfDivider.getColumnFormatter().setWidth(0, "50%");
			cfDivider.getColumnFormatter().setWidth(1, "50%");
			final CellFormatter cellF = cfDivider.getCellFormatter();
			cfDivider.setWidget(0, 0, categoriesLeft);
			cellF.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
			cfDivider.setWidget(0, 1, categoriesRight);
			cellF.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
			cfDivider.setWidget(1, 0, findingsLeft);
			cellF.setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
			cfDivider.setWidget(1, 1, findingsRight);
			cellF.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
			contentPanel.add(cfDivider);
		}

		public void addFilterEntry(final ScanFilterEntry entry) {
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

		private void addEntry(final VerticalPanel leftPanel,
				final VerticalPanel rightPanel, final ContentLink entryLink) {
			if (leftPanel.getWidgetCount() <= rightPanel.getWidgetCount()) {
				leftPanel.add(entryLink);
			} else {
				rightPanel.add(entryLink);
			}
		}
	}
}
