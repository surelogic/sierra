package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeBlock extends SectionPanel {
	private final HTML summary = new HTML();
	private final HTML description = new HTML();
	private final VerticalPanel chart = new VerticalPanel();

	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Finding Type");

		contentPanel.add(new HTML("<h3>Summary</h3>"));
		contentPanel.add(summary);
		contentPanel.add(new HTML("<h3>Description</h3>"));
		contentPanel.add(description);
		contentPanel.add(new HTML("<h3>Found In</h3>"));
		contentPanel.add(chart);
	}

	protected void onUpdate(Context context) {
		final RulesContext rulesCtx = new RulesContext(context);
		final String findingUuid = rulesCtx.getFinding();
		if (LangUtil.notEmpty(findingUuid)) {
			ServiceHelper.getSettingsService().getFindingTypeInfo(findingUuid,
					new Callback() {

						protected void onFailure(String message, Object result) {
							setEmpty();
						}

						protected void onSuccess(String message, Object result) {
							setFindingType((FindingTypeInfo) result);
						}

					});
		} else {
			setEmpty();
		}
	}

	protected void onDeactivate() {
		// nothing to do
	}

	protected void setEmpty() {
		setSummary("");
		summary.setHTML(null);
		description.setHTML(null);
		chart.clear();
	}

	protected void setFindingType(FindingTypeInfo result) {
		setSummary(result.getName());
		summary.setHTML(result.getShortMessage());
		description.setHTML(result.getInfo());
		chart.clear();
		chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
				result.getUid()).build());
	}
}
