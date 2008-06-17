package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilterComparator;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ListBlock;
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

	private class FindingsView extends ListBlock<FindingTypeFilter> {

		public FindingsView() {
			super("Finding Types");
		}

		public void setCategory(Category cat) {
			clear();

			if (cat != null) {
				final List<FindingTypeFilter> visibleFindings = new ArrayList<FindingTypeFilter>();
				for (FindingTypeFilter finding : cat.getIncludedEntries()) {
					if (!visibleFindings.contains(finding)) {
						visibleFindings.add(finding);
					}
				}

				Collections.sort(visibleFindings,
						new FindingTypeFilterComparator());

				for (FindingTypeFilter finding : visibleFindings) {
					addItem(finding);
				}
			}
		}

		@Override
		protected ContentComposite getItemContent() {
			return FindingTypesContent.getInstance();
		}

		@Override
		protected String getItemText(FindingTypeFilter item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(FindingTypeFilter item) {
			return item.getShortMessage();
		}

	}
}
