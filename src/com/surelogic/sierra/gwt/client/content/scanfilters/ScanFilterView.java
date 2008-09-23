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
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;

public class ScanFilterView extends BasicPanel {
	private final VerticalPanel importancePanels = new VerticalPanel();
	private ScanFilter selection;
	private boolean showCategories = true;
	private boolean refreshedCategories;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		importancePanels.setWidth("100%");
		contentPanel.add(importancePanels);
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
		importancePanels.clear();

		if (selection != null) {
			setSummary(selection.getName());

			final Map<ImportanceView, FilterPanel> categoryImportancePanels = new HashMap<ImportanceView, FilterPanel>();
			final Set<ScanFilterEntry> categoryFindings = new HashSet<ScanFilterEntry>();
			if (showCategories) {
				final List<ScanFilterEntry> sortedCategories = new ArrayList<ScanFilterEntry>(
						filter.getCategories());
				Collections.sort(sortedCategories);
				for (final ScanFilterEntry category : sortedCategories) {
					final FilterPanel filterList = getFilterPanel(
							categoryImportancePanels, category.getImportance(),
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

			final Map<ImportanceView, FilterPanel> findingImportancePanels = new HashMap<ImportanceView, FilterPanel>();
			final List<ScanFilterEntry> sortedFindings = new ArrayList<ScanFilterEntry>(
					filter.getTypes());
			if (!categoryFindings.isEmpty()) {
				sortedFindings.addAll(categoryFindings);
			}
			Collections.sort(sortedFindings);
			for (final ScanFilterEntry finding : sortedFindings) {
				final FilterPanel filterList = getFilterPanel(
						findingImportancePanels, finding.getImportance(), false);
				filterList.addFilterEntry(finding);
			}

			final ImportanceView[] importances = ImportanceView.values();

			for (int i = importances.length - 1; i >= 0; i--) {
				addFilterPanels(importances[i], categoryImportancePanels,
						findingImportancePanels);

			}
			addFilterPanels(null, categoryImportancePanels,
					findingImportancePanels);

			if (importancePanels.getWidgetCount() == 0) {
				importancePanels.add(StyleHelper.add(new Label(
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

	private void addFilterPanels(final ImportanceView importance,
			final Map<ImportanceView, FilterPanel> categoryPanels,
			final Map<ImportanceView, FilterPanel> findingPanels) {
		final FilterPanel categoryPanel = categoryPanels.get(importance);
		final FilterPanel findingPanel = findingPanels.get(importance);
		if (categoryPanel != null || findingPanel != null) {
			final String importanceTitle = importance == null ? "Default"
					: importance.getName();
			importancePanels.add(StyleHelper.add(new Label(importanceTitle
					+ " Priority", false), Style.STRONG));
		}
		if (categoryPanel != null) {
			importancePanels.add(categoryPanel);
		}

		if (findingPanel != null) {
			importancePanels.add(findingPanel);
		}
	}

	private FilterPanel getFilterPanel(
			final Map<ImportanceView, FilterPanel> filtersByImportance,
			final ImportanceView importance, final boolean isCategory) {
		FilterPanel panel = filtersByImportance.get(importance);
		if (panel == null) {
			panel = new FilterPanel(isCategory);
			panel.initialize();
			filtersByImportance.put(importance, panel);
		}
		return panel;
	}

	private static class FilterPanel extends BasicPanel {
		private final VerticalPanel categoriesLeft = new VerticalPanel();
		private final VerticalPanel categoriesRight = new VerticalPanel();
		private final VerticalPanel findingsLeft = new VerticalPanel();
		private final VerticalPanel findingsRight = new VerticalPanel();

		public FilterPanel(final boolean categories) {
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
