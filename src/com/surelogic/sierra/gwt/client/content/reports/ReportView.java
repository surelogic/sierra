package com.surelogic.sierra.gwt.client.content.reports;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ReportView extends BlockPanel {

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Report");

	}

	public void retrieveReport(Report selection,
			Map<String, List<String>> parameters) {
		setWaitStatus();

		StringBuffer str = new StringBuffer(selection.getName()).append(" (");
		for (Map.Entry<String, List<String>> param : parameters.entrySet()) {
			str.append(param.getKey()).append("=");
			List<String> values = param.getValue();
			if (values.size() == 1) {
				str.append(values.get(0));
			} else if (values.size() > 1) {
				str.append("[");
				for (String value : values) {
					str.append(value).append(",");
				}
				str.append("]");
			} else {
				str.append("none");
			}
			str.append(",");
		}
		str.append(")");
		Window.alert("Report: " + str.toString());

		// retrieve and display the report

	}

}
