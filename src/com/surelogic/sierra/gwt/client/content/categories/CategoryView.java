package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingTypeComparator;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CategoryView extends BlockPanel {
	private final VerticalPanel categoryInfo = new VerticalPanel();
	private final Label description = new Label();
	private final FindingsView findingsView = new FindingsView();
	private Category category;

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Category");

		categoryInfo.setWidth("100%");
		categoryInfo.addStyleName("padded");
		categoryInfo.add(description);
		contentPanel.add(categoryInfo);

		findingsView.initialize();
		findingsView.setSubsectionStyle(true);
		contentPanel.add(findingsView);
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category cat) {
		category = cat;

		if (cat != null) {
			setSummary(cat.getName());
		} else {
			setSummary("Select a Category");
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
	}

	private class FindingsView extends BlockPanel {
		private final SelectionTracker<ItemLabel<FindingType>> selectionTracker = new SelectionTracker<ItemLabel<FindingType>>();
		private final Map<Widget, FindingType> findings = new HashMap<Widget, FindingType>();

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle("Finding Types");
		}

		public void setCategory(Category cat) {
			final VerticalPanel findingTypes = getContentPanel();
			findingTypes.clear();
			findings.clear();

			if (cat != null) {
				final List<FindingType> visibleFindings = new ArrayList<FindingType>();
				for (FindingType finding : cat.getIncludedEntries()) {
					if (!visibleFindings.contains(finding)) {
						visibleFindings.add(finding);
					}
				}

				Collections.sort(visibleFindings, new FindingTypeComparator());

				ClickListener findingListener = new ClickListener() {

					public void onClick(Widget sender) {
						// TODO view the finding that was clicked
						// FilterEntry finding = (FilterEntry) ((ItemLabel)
						// sender).getItem();
						// CategoriesContext(finding).updateContext();
					}

				};

				for (FindingType finding : visibleFindings) {
					final ItemLabel<FindingType> rule = new ItemLabel<FindingType>(
							finding.getName(), finding, selectionTracker,
							findingListener);
					rule.setTitle(finding.getShortMessage());
					findingTypes.add(rule);
				}
			}
		}

	}
}
