package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public final class FindingTypesContent extends ContentComposite {
	public static final String PARAM_FINDING = "finding";
	private static final FindingTypesContent instance = new FindingTypesContent();

	private final HTML name = new HTML();
	private final HTML summary = new HTML();
	private final HTML description = new HTML();
	private final VerticalPanel chart = new VerticalPanel();

	public static FindingTypesContent getInstance() {
		return instance;
	}

	private FindingTypesContent() {
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.add(new HTML("<h3>Finding Type</h3>"));
		panel.add(name);
		panel.add(new HTML("<h3>Summary</h3>"));
		panel.add(summary);
		panel.add(new HTML("<h3>Description</h3>"));
		panel.add(description);
		panel.add(new HTML("<h3>Found In</h3>"));
		panel.add(chart);
		getRootPanel().add(panel, DockPanel.CENTER);

	}

	@Override
	protected void onUpdate(Context context) {
		final String findingType = context.getParameter(PARAM_FINDING);
		if ((findingType == null) || (findingType.length() == 0)) {
			setEmpty();
		} else {
			ServiceHelper.getSettingsService().getFindingTypeInfo(findingType,
					new Callback<FindingTypeInfo>() {

						@Override
						protected void onFailure(String message,
								FindingTypeInfo result) {
							setEmpty();
						}

						@Override
						protected void onSuccess(String message,
								FindingTypeInfo result) {
							setFindingType(result);
						}

					});
		}
	}

	@Override
	protected void onDeactivate() {
		setEmpty();
	}

	protected void setEmpty() {
		name.setHTML("None");
		summary.setHTML(null);
		description.setHTML(null);
		chart.clear();
	}

	protected void setFindingType(FindingTypeInfo result) {
		name.setHTML(result.getName());
		summary.setHTML(result.getShortMessage());
		description.setHTML(result.getInfo());
		chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
				result.getUid()).build());
	}

}
