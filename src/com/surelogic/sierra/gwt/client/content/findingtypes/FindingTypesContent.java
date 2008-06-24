package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.FindingType;
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
}
