package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BlockPanel {
	private final HTML description = new HTML();
	private final VerticalPanel chart = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		description.addStyleName("padded");
		contentPanel.add(description);
		contentPanel.add(new Label("Found In:"));
		contentPanel.add(chart);
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

		chart.clear();
		if (findingType != null) {
			// FIXME throws an exception right now
			// chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
			// findingType.getUuid()).build());
		}
	}

}
