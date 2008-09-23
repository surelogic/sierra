package com.surelogic.sierra.gwt.client.content.findingtypes;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingType.ArtifactTypeInfo;
import com.surelogic.sierra.gwt.client.data.FindingType.CategoryInfo;
import com.surelogic.sierra.gwt.client.data.FindingType.ScanFilterInfo;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.ui.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ListPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BasicPanel {
	private final HTML description = new HTML();
	private final CategoryList categoriesIncluding = new CategoryList(
			"Categories including this type of finding");
	private final CategoryList categoriesExcluding = new CategoryList(
			"Categories excluding this type of finding");
	private final ScanFilterList scanFilters = new ScanFilterList();
	private final ArtifactTypeList artifactTypes = new ArtifactTypeList();
	private final VerticalPanel chart = new VerticalPanel();
	private FindingType selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		description.addStyleName("padded");
		contentPanel.add(description);

		categoriesIncluding.initialize();
		contentPanel.add(categoriesIncluding);

		categoriesExcluding.initialize();
		contentPanel.add(categoriesExcluding);

		scanFilters.initialize();
		contentPanel.add(scanFilters);

		artifactTypes.initialize();
		contentPanel.add(artifactTypes);

		contentPanel.add(chart);
	}

	public FindingType getSelection() {
		return selection;
	}

	public void setSelection(final FindingType findingType) {
		selection = findingType;

		if (findingType != null) {
			setSummary(findingType.getName());
			final String info = findingType.getInfo();
			if (LangUtil.notEmpty(info)) {
				description.setHTML(info);
				description.removeStyleName("font-italic");
			} else {
				description.setHTML(info);
				description.addStyleName("font-italic");
			}
		} else {
			setSummary("Select a finding type");
			description.setText(null);
		}

		categoriesIncluding.clear();
		categoriesExcluding.clear();
		scanFilters.clear();
		artifactTypes.clear();
		chart.clear();
		if (findingType != null) {
			categoriesIncluding.addItems(findingType.getCategoriesIncluding());
			categoriesExcluding.addItems(findingType.getCategoriesExcluding());
			scanFilters.addItems(findingType.getScanFiltersIncluding());
			artifactTypes.addItems(findingType.getArtifactTypes());
			chart.add(ChartBuilder.report(ReportCache.findingTypeCounts())
					.prop("uuid", findingType.getUuid()).build());
		}
	}

	public void addCategoriesIncludingAction(final String text,
			final ClickListener clickListener) {
		categoriesIncluding.addAction(text, clickListener);
	}

	public List<String> getCategoriesIncludingIds() {
		final List<String> categoryIds = new ArrayList<String>();
		for (int i = 0; i < categoriesIncluding.getItemCount(); i++) {
			categoryIds.add(categoriesIncluding.getItem(i).getUuid());
		}
		return categoryIds;
	}

	private class CategoryList extends ListPanel<CategoryInfo> {

		public CategoryList(final String title) {
			super(title);
		}

		@Override
		protected ContentComposite getItemContent() {
			return CategoriesContent.getInstance();
		}

		@Override
		protected String getItemText(final CategoryInfo item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(final CategoryInfo item) {
			return item.getDescription();
		}
	}

	private class ScanFilterList extends ListPanel<ScanFilterInfo> {

		public ScanFilterList() {
			super("Scan Filters that use this type of finding");
		}

		@Override
		protected ContentComposite getItemContent() {
			return ScanFiltersContent.getInstance();
		}

		@Override
		protected String getItemText(final ScanFilterInfo item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(final ScanFilterInfo item) {
			return item.getName();
		}

	}

	private class ArtifactTypeList extends BasicPanel {

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("Tools reporting this type of finding");
			setSubsectionStyle(true);
		}

		public void clear() {
			getContentPanel().clear();
		}

		public void addItems(final List<ArtifactTypeInfo> info) {
			for (final ArtifactTypeInfo art : info) {
				getContentPanel().add(
						new HTML(art.getTool() + ": " + art.getArtifactType()));
			}
		}
	}

}
