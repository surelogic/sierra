package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ListBlock;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BlockPanel {
	private final HTML description = new HTML();
	private final CategoryList categoriesIncluding = new CategoryList(
			"Categories including this finding");
	private final CategoryList categoriesExcluding = new CategoryList(
			"Categories excluding this finding");
	private final ScanFilterList scanFilters = new ScanFilterList();

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
			// FIXME throws an exception right now
			// chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
			// findingType.getUuid()).build());
		}
	}

	private class CategoryList extends ListBlock<Category> {

		public CategoryList(String title) {
			super(title);
		}

		@Override
		protected ContentComposite getItemContent() {
			return CategoriesContent.getInstance();
		}

		@Override
		protected String getItemText(Category item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(Category item) {
			return item.getInfo();
		}
	}

	private class ScanFilterList extends ListBlock<ScanFilter> {

		public ScanFilterList() {
			super("Scan Filters that use this finding type");
		}

		@Override
		protected ContentComposite getItemContent() {
			return ScanFiltersContent.getInstance();
		}

		@Override
		protected String getItemText(ScanFilter item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(ScanFilter item) {
			StringBuffer projects = new StringBuffer("Projects: ");
			boolean firstItem = true;
			for (String project : item.getProjects()) {
				if (firstItem) {
					firstItem = false;
				} else {
					projects.append(", ");
				}
				projects.append(project);
			}
			return projects.toString();
		}

	}

}
