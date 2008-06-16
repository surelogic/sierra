package com.surelogic.sierra.gwt.client.content.findingtypes;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class FindingTypeView extends BlockPanel {
	private final HTML summary = new HTML();
	private final HTML description = new HTML();
	private final VerticalPanel chart = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(new HTML("<h3>Summary</h3>"));
		contentPanel.add(summary);
		contentPanel.add(new HTML("<h3>Description</h3>"));
		contentPanel.add(description);
		contentPanel.add(new HTML("<h3>Found In</h3>"));
		contentPanel.add(chart);
	}

	public void setSelection(FindingType findingType) {
		if (findingType != null) {
			setSummary(findingType.getName());

			setText(summary, findingType.getShortMessage(),
					"No summary information.");
			setText(description, findingType.getInfo(), "No description.");
		} else {
			setSummary("Select a finding type");
			summary.setText(null);
			description.setText(null);
		}

		chart.clear();
		if (findingType != null) {
			// FIXME throws an exception right now
			// chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
			// findingType.getUuid()).build());
		}
	}

	private void setText(HTML textUI, String text, String emptyText) {
		if (LangUtil.notEmpty(text)) {
			description.setHTML(text);
			description.removeStyleName("font-italic");
		} else {
			description.setHTML(emptyText);
			description.addStyleName("font-italic");
		}
	}

}
