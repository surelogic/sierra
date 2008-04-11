package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;

public class CategoryPanel extends Composite {
	public static final String PRIMARY_STYLE = RulesContent.PRIMARY_STYLE;

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final VerticalPanel categoryEntries = new VerticalPanel();
	private final Label categoryName = new Label();
	private final Label categoryDescription = new Label();

	public CategoryPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		final VerticalPanel categoryInfo = new VerticalPanel();
		categoryInfo.setWidth("100%");

		categoryName.addStyleName("sl-Section");
		categoryInfo.add(categoryName);
		categoryInfo.setCellHorizontalAlignment(categoryName,
				VerticalPanel.ALIGN_LEFT);

		categoryInfo.add(new Label("Description:"));
		categoryInfo.add(categoryDescription);

		rootPanel.add(categoryInfo);

		categoryEntries.setWidth("100%");
		final Label findingTypes = new Label("Finding Types");
		findingTypes.addStyleName("sl-Section");
		categoryEntries.add(findingTypes);

		rootPanel.add(categoryEntries);
	}

	public void setCategory(Category cat) {

		categoryName.setText(cat.getName());
		final String catInfo = cat.getInfo();

		if (catInfo == null || "".equals(catInfo)) {
			categoryDescription.setText("None");
			categoryDescription.addStyleName("font-italic");
		} else {
			categoryDescription.setText(catInfo);
			categoryDescription.removeStyleName("font-italic");
		}

		while (categoryEntries.getWidgetCount() > 1) {
			categoryEntries.remove(1);
		}
		for (final Iterator it = cat.getEntries().iterator(); it.hasNext();) {
			final FilterEntry finding = (FilterEntry) it.next();
			categoryEntries.add(createDetailsRule(finding, !finding
					.isFiltered()));
		}
		final Set excluded = cat.getExcludedEntries();
		for (final Iterator catIt = cat.getParents().iterator(); catIt
				.hasNext();) {
			final Category parent = (Category) catIt.next();
			final DisclosurePanel parentPanel = new DisclosurePanel("From: "
					+ parent.getName());
			final VerticalPanel parentFindingsPanel = new VerticalPanel();
			final Set parentFindings = parent.getIncludedEntries();
			for (final Iterator findingIt = parentFindings.iterator(); findingIt
					.hasNext();) {
				final FilterEntry finding = (FilterEntry) findingIt.next();
				parentFindingsPanel.add(createDetailsRule(finding, !excluded
						.contains(finding)));
			}
			parentPanel.setContent(parentFindingsPanel);
			categoryEntries.add(parentPanel);
		}
	}

	private CheckBox createDetailsRule(FilterEntry finding, boolean enabled) {
		final CheckBox rule = new CheckBox(finding.getName());
		rule.addStyleName(PRIMARY_STYLE + "-details-finding");
		rule.setChecked(enabled);
		return rule;
	}
}
