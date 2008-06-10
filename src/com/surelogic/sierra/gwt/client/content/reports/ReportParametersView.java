package com.surelogic.sierra.gwt.client.content.reports;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportParametersView extends BlockPanel {
	private final Label description = new Label("", true);
	private final FlexTable parametersTable = new FlexTable();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Report Parameters");

		description.addStyleName("padded");
		contentPanel.add(description);

		parametersTable.setWidth("50%");
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
			parametersTable.setText(rowIndex, 0, param.getTitle() + ":");
			parametersTable.getCellFormatter().setVerticalAlignment(rowIndex,
					0, HasVerticalAlignment.ALIGN_TOP);
			parametersTable.setWidget(rowIndex, 1, getParameterUI(param));
			rowIndex++;
		}
		parametersTable.getColumnFormatter().setWidth(0, "10%");
		parametersTable.getColumnFormatter().setWidth(1, "90%");
	}

	private Widget getParameterUI(Parameter param) {
		if (param.getType() == Parameter.Type.PROJECTS) {
			final ListBox lb = createListBox(4, "100%");
			ServiceHelper.getSettingsService().searchProjects("*", -1,
					new StandardCallback<List<String>>() {

						@Override
						protected void doSuccess(List<String> result) {
							if (result.isEmpty()) {
								lb.setEnabled(false);
								lb.addItem("No Projects");
							} else {
								lb.setEnabled(true);
								for (String project : result) {
									lb.addItem(project);
								}
							}
						}
					});
			return lb;
		} else if (param.getType() == Parameter.Type.PRIORITY) {
			return createListBox(5, "50%", "Critical", "High", "Medium", "Low",
					"Irrelevant");
		}

		TextBox tb = new TextBox();
		tb.setWidth("100%");
		return tb;
	}

	private ListBox createListBox(int visibleItemCount, String width,
			String... items) {
		ListBox lb = new ListBox(true);
		lb.setWidth(width);
		lb.setVisibleItemCount(visibleItemCount);
		if (items != null) {
			for (String item : items) {
				lb.addItem(item);
			}
		}
		return lb;
	}
}
