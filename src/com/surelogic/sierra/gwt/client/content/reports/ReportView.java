package com.surelogic.sierra.gwt.client.content.reports;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ReportView extends BlockPanel {

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		// nothing to do
	}

	public void retrieveReport(Report selection) {
		setWaitStatus();

		final StringBuffer str = new StringBuffer(selection.getName())
				.append(" ");
		str.append(" \"").append(selection.getTitle()).append("\" (");
		for (final Parameter param : selection.getParameters()) {
			str.append(param.getName()).append("=");
			final List<String> values = param.getValues();
			if (values.size() == 1) {
				str.append(values.get(0));
			} else if (values.size() > 1) {
				str.append("[");
				for (final String value : values) {
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
