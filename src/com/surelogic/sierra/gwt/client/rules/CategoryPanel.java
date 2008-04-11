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
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.SubsectionPanel;

public class CategoryPanel extends Composite {
	public static final String PRIMARY_STYLE = RulesContent.PRIMARY_STYLE;

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final SectionPanel categorySection = new SectionPanel("Category",
			"");
	private final Label categoryDescription = new Label();
	private final SubsectionPanel findingTypesPortlet = new SubsectionPanel(
			"Finding Types", "");
	private final VerticalPanel findingTypes = findingTypesPortlet
			.getContentPanel();

	public CategoryPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		final VerticalPanel catInfoContent = categorySection.getContentPanel();
		catInfoContent.add(new Label("Description:"));
		catInfoContent.add(categoryDescription);

		catInfoContent.add(findingTypesPortlet);

		rootPanel.add(categorySection);
	}

	public void setCategory(Category cat) {
		categorySection.getSectionInfo().setText(cat.getName());
		final String catInfo = cat.getInfo();

		if (catInfo == null || "".equals(catInfo)) {
			categoryDescription.setText("None");
			categoryDescription.addStyleName("font-italic");
		} else {
			categoryDescription.setText(catInfo);
			categoryDescription.removeStyleName("font-italic");
		}

		findingTypes.clear();
		for (final Iterator it = cat.getEntries().iterator(); it.hasNext();) {
			final FilterEntry finding = (FilterEntry) it.next();
			findingTypes.add(createDetailsRule(finding, !finding.isFiltered()));
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
			findingTypes.add(parentPanel);
		}
	}

	private CheckBox createDetailsRule(FilterEntry finding, boolean enabled) {
		final CheckBox rule = new CheckBox(finding.getName());
		rule.addStyleName(PRIMARY_STYLE + "-details-finding");
		rule.setChecked(enabled);
		return rule;
	}
}
