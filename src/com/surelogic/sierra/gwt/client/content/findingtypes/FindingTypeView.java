package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BlockPanel {
	private final HTML description = new HTML();
	private final FindingTypeList categoriesIncluding = new FindingTypeList(
			"Categories including this finding:");
	private final FindingTypeList categoriesExcluding = new FindingTypeList(
			"Categories excluding this finding");

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		description.addStyleName("padded");
		contentPanel.add(description);
		contentPanel.add(categoriesIncluding);
		contentPanel.add(categoriesExcluding);

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
		if (findingType != null) {
			// FIXME throws an exception right now
			// chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
			// findingType.getUuid()).build());
		}
	}

	private class FindingTypeList extends BlockPanel {
		private final String title;
		private final SelectionTracker<ItemLabel<Category>> selectionTracker = new SelectionTracker<ItemLabel<Category>>();
		final Label none = new Label("None", false);
		private ClickListener categoryListener;

		public FindingTypeList(String title) {
			super();
			this.title = title;
		}

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle(title);
			setSubsectionStyle(true);

			none.addStyleName("font-italic");

			categoryListener = new ClickListener() {

				public void onClick(Widget sender) {
					final ItemLabel<?> categoryUI = (ItemLabel<?>) sender;
					final Category cat = (Category) categoryUI.getItem();
					Context.createWithUuid(CategoriesContent.getInstance(),
							cat.getUuid()).submit();
				}

			};
		}

		public void clear() {
			final VerticalPanel content = getContentPanel();
			content.clear();
			content.add(none);
		}

		public void addCategory(Category cat) {
			final ItemLabel<Category> catUI = new ItemLabel<Category>(cat
					.getName(), cat, selectionTracker, categoryListener);
			catUI.setTitle(cat.getInfo());

			final VerticalPanel content = getContentPanel();
			content.remove(none);
			content.add(catUI);
		}
	}

}
