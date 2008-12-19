package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.ui.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ListPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CategoryView extends BasicPanel {
	private final VerticalPanel categoryInfo = new VerticalPanel();
	private final Grid ownerGrid = new Grid(1, 3);
	private final Label description = new Label();
	private final FindingsView findingsView = new FindingsView();
	private final VerticalPanel chart = new VerticalPanel();
	private Category category;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		categoryInfo.setWidth("100%");
		categoryInfo.addStyleName("padded");
		ownerGrid.setCellPadding(0);
		ownerGrid.setCellSpacing(0);
		ownerGrid.addStyleName("padded-bottom");
		ownerGrid.setText(0, 0, "Owner:");
		ownerGrid.getColumnFormatter().setWidth(1, "5px");
		categoryInfo.add(ownerGrid);
		ownerGrid.setVisible(false);
		categoryInfo.add(description);
		contentPanel.add(categoryInfo);

		findingsView.initialize();
		findingsView.setSubsectionStyle(true);
		contentPanel.add(findingsView);
		contentPanel.add(chart);
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(final Category cat) {
		category = cat;

		if (cat != null) {
			setSummary(cat.getName());
		} else {
			setSummary("Select a Category");
		}
		if (cat == null || (cat != null && cat.isLocal())) {
			ownerGrid.setVisible(false);
		} else {
			ownerGrid.setVisible(true);
			final StringBuilder linkHtml = new StringBuilder();
			linkHtml.append("<a href=\"").append(cat.getOwnerURL()).append(
					"\">");
			linkHtml.append(cat.getOwnerLabel() == null ? "Unknown" : cat
					.getOwnerLabel());
			linkHtml.append("</a>");
			ownerGrid.setWidget(0, 2, new HTML(linkHtml.toString()));
		}

		final String catInfo = cat == null ? "" : cat.getInfo();
		if (LangUtil.notEmpty(catInfo)) {
			description.setText(catInfo);
			description.removeStyleName("font-italic");
		} else {
			description.setText("No summary information.");
			description.addStyleName("font-italic");
		}
		findingsView.setCategory(category);

		chart.clear();
		if (category != null) {
			chart.add(ChartBuilder.report(ReportCache.categoryCounts()).prop(
					"uuid", category.getUuid()).build());
		}
	}

	private class FindingsView extends ListPanel<FindingTypeFilter> {

		public FindingsView() {
			super("Finding Types");
		}

		public void setCategory(final Category cat) {
			clear();

			if (cat != null) {
				final List<FindingTypeFilter> visibleFindings = new ArrayList<FindingTypeFilter>();
				for (final FindingTypeFilter finding : cat.getIncludedEntries()) {
					if (!visibleFindings.contains(finding)) {
						visibleFindings.add(finding);
					}
				}

				Collections.sort(visibleFindings);

				for (final FindingTypeFilter finding : visibleFindings) {
					addItem(finding);
				}
			}
		}

		@Override
		protected ContentComposite getItemContent() {
			return FindingTypesContent.getInstance();
		}

		@Override
		protected String getItemText(final FindingTypeFilter item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(final FindingTypeFilter item) {
			return item.getShortMessage();
		}

	}
}
