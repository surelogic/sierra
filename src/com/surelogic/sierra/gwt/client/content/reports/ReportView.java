package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportView extends BlockPanel {
	private final Label description = new Label("", true);
	private final FlexTable parametersTable = new FlexTable();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Report");

		contentPanel.add(description);

		parametersTable.setWidth("60%");
		contentPanel.add(parametersTable);

		contentPanel.add(new Button("Generate Report"));
	}

	public void setSelection(Report report) {
		if (report != null) {
			setSummary(report.getName());
		} else {
			setSummary("Select a report");
		}

		final String desc = report == null ? "" : report.getDescription();
		if (LangUtil.notEmpty(desc)) {
			description.setText(desc);
			description.removeStyleName("font-italic");
		} else {
			description.setText("No summary information.");
			description.addStyleName("font-italic");
		}

		while (parametersTable.getRowCount() > 0) {
			parametersTable.removeRow(0);
		}
		int rowIndex = 0;
		for (Report.Parameter param : report.getParameters()) {
			parametersTable.setText(rowIndex, 0, param.getTitle());
			parametersTable.setWidget(rowIndex, 1, getParameterUI(param));
			rowIndex++;
		}
		parametersTable.getColumnFormatter().setWidth(0, "30%");
		parametersTable.getColumnFormatter().setWidth(1, "70%");
	}

	private Widget getParameterUI(Parameter param) {
		if (param.getType() == Parameter.Type.List) {
			ListBox lb = new ListBox();
			lb.setWidth("100%");
			return lb;
		} else if (param.getType() == Parameter.Type.MultiList) {
			ListBox lb = new ListBox(true);
			lb.setWidth("100%");
			lb.setVisibleItemCount(4);
			return lb;
		}

		TextBox tb = new TextBox();
		tb.setWidth("100%");
		return tb;
	}

}
