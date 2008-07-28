package com.surelogic.sierra.gwt.client.content.findingtypes;

import java.util.Set;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.content.common.CategorySelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.data.cache.FindingTypeCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class FindingTypesContent extends
		ListContentComposite<FindingType, FindingTypeCache> {
	public static final String PARAM_FINDING = "finding";
	private static final FindingTypesContent instance = new FindingTypesContent();
	private final FindingTypeView findingView = new FindingTypeView();

	public static FindingTypesContent getInstance() {
		return instance;
	}

	private FindingTypesContent() {
		// singleton
		super(new FindingTypeCache());
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Finding Types");

		findingView.initialize();
		findingView.addCategoriesIncludingAction("Add Category",
				new ClickListener() {

					public void onClick(Widget sender) {
						promptForCategories();
					}
				});
		selectionPanel.add(findingView);
	}

	@Override
	protected String getItemText(FindingType item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(FindingType item, String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(FindingType item) {
		findingView.setSelection(item);
	}

	private void promptForCategories() {
		final CategorySelectionDialog dialog = new CategorySelectionDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					final FindingType finding = findingView.getSelection();
					final CategoryCache categoryCache = CategoryCache
							.getInstance();
					final Set<Category> cats = dialog.getSelectedCategories();
					for (final Category cat : cats) {
						final FindingTypeFilter filter = new FindingTypeFilter();
						filter.setUuid(finding.getUuid());
						filter.setName(finding.getName());
						filter.setShortMessage(finding.getShortMessage());
						filter.setFiltered(false);
						cat.getEntries().add(filter);
						categoryCache.save(cat);
					}
					categoryCache.refresh();

					getCache().refresh();
				}
			}

		});
		dialog.center();
		dialog.setCategories(findingView.getCategoriesIncludingIds(), false);
	}
}
