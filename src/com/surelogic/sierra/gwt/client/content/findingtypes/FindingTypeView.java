package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingType.CategoryInfo;
import com.surelogic.sierra.gwt.client.data.FindingType.ScanFilterInfo;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ListBlock;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BlockPanel {
	private final HTML description = new HTML();
	private final CategoryList categoriesIncluding = new CategoryList(
			"Categories including this finding");
	private final CategoryList categoriesExcluding = new CategoryList(
			"Categories excluding this finding");
	private final ScanFilterList scanFilters = new ScanFilterList();
	private final VerticalPanel chart = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		description.addStyleName("padded");
		contentPanel.add(description);

		categoriesIncluding.initialize();
		contentPanel.add(categoriesIncluding);

		categoriesExcluding.initialize();
		contentPanel.add(categoriesExcluding);

		scanFilters.initialize();
		contentPanel.add(scanFilters);
		contentPanel.add(chart);
	}

	public void setSelection(FindingType findingType) {
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
		if (findingType != null) {
			for (final CategoryInfo catIncluding : findingType
					.getCategoriesIncluding()) {
				categoriesIncluding.addItem(catIncluding);
			}
			for (final CategoryInfo catExcluding : findingType
					.getCategoriesExcluding()) {
				categoriesExcluding.addItem(catExcluding);
			}
			for (final ScanFilterInfo scanIncluding : findingType
					.getScanFiltersIncluding()) {
				scanFilters.addItem(scanIncluding);
			}
			chart.clear();
			chart.add(ChartBuilder.name("FindingTypeCounts").height(400).width(
					400).prop("uuid", findingType.getUuid()).build());
		}
	}

	private class CategoryList extends ListBlock<CategoryInfo> {

		public CategoryList(String title) {
			super(title);
		}

		@Override
		protected ContentComposite getItemContent() {
			return CategoriesContent.getInstance();
		}

		@Override
		protected String getItemText(CategoryInfo item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(CategoryInfo item) {
			return item.getDescription();
		}
	}

	private class ScanFilterList extends ListBlock<ScanFilterInfo> {

		public ScanFilterList() {
			super("Scan Filters that use this finding type");
		}

		@Override
		protected ContentComposite getItemContent() {
			return ScanFiltersContent.getInstance();
		}

		@Override
		protected String getItemText(ScanFilterInfo item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(ScanFilterInfo item) {
			return item.getName();
		}

	}

}
