package com.surelogic.sierra.gwt.client.content.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportParametersView extends BlockPanel {
	private final Label description = new Label("", true);
	private final FlexTable parametersTable = new FlexTable();
	private final ActionPanel reportActions = new ActionPanel();
	private final VerticalPanel settingsPanel = new VerticalPanel();
	private final Map<Report.Parameter, Widget> paramUIMap = new HashMap<Report.Parameter, Widget>();
	private final Map<OutputType, Label> actionOutputMap = new HashMap<OutputType, Label>();
	private Report selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		final VerticalPanel paramPanel = new VerticalPanel();
		paramPanel.setWidth("100%");
		description.addStyleName("padded");
		paramPanel.add(description);

		parametersTable.setWidth("50%");
		paramPanel.add(parametersTable);

		paramPanel.add(reportActions);
		paramPanel.setCellHorizontalAlignment(reportActions,
				HasHorizontalAlignment.ALIGN_CENTER);
		final HorizontalPanel h = new HorizontalPanel();
		h.add(paramPanel);
		h.add(settingsPanel);
		h.setWidth("100%");
		contentPanel.add(h);
	}

	public void setSelection(final Report report) {
		selection = report;

		if (report != null) {
			setSummary(report.getTitle());
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
		paramUIMap.clear();

		int rowIndex = 0;
		for (final Report.Parameter param : report.getParameters()) {
			parametersTable.setText(rowIndex, 0, param.getName() + ":");
			parametersTable.getCellFormatter().setVerticalAlignment(rowIndex,
					0, HasVerticalAlignment.ALIGN_TOP);
			final Widget paramUI = getParameterUI(param);
			parametersTable.setWidget(rowIndex, 1, paramUI);
			paramUIMap.put(param, paramUI);
			rowIndex++;
		}
		parametersTable.getColumnFormatter().setWidth(0, "10%");
		parametersTable.getColumnFormatter().setWidth(1, "90%");

		for (final Map.Entry<OutputType, Label> actionEntry : actionOutputMap
				.entrySet()) {
			actionEntry.getValue().setVisible(
					report.hasOutputType(actionEntry.getKey()));
		}

		settingsPanel.clear();
		for (final ReportSettings rs : report.getSavedReports()) {
			final Context reportContext = Context.createWithUuid(report);
			settingsPanel.add(new Hyperlink(rs.getTitle(), reportContext
					.withParam("reportSettingsUuid", rs.getUuid()).toString()));
		}
	}

	public Report getSelection() {
		return selection;
	}

	public ReportSettings getReportSettings() {
		final ReportSettings settings = new ReportSettings(selection);

		for (final Map.Entry<Report.Parameter, Widget> paramEntry : paramUIMap
				.entrySet()) {
			final String paramName = paramEntry.getKey().getName();
			final Widget paramUI = paramEntry.getValue();
			if (paramUI instanceof TextBox) {
				settings.setSettingValue(paramName, ((TextBox) paramUI)
						.getText());
			} else if (paramUI instanceof ListBox) {
				final List<String> values = new ArrayList<String>();
				final ListBox lb = (ListBox) paramUI;
				for (int i = 0; i < lb.getItemCount(); i++) {
					if (lb.isItemSelected(i)) {
						values.add(lb.getItemText(i));
					}
				}
				settings.setSettingValue(paramName, values);
			}
		}
		return settings;
	}

	public void addReportAction(final String text, final OutputType outputType,
			final ClickListener clickListener) {
		final Label action = reportActions.addAction(text, clickListener);
		if (outputType != null) {
			actionOutputMap.put(outputType, action);
		}
	}

	private Widget getParameterUI(final Parameter param) {
		if (param.getType() == Parameter.Type.PROJECTS) {
			final ListBox lb = createListBox(4, "100%");
			ServiceHelper.getSettingsService().searchProjects("*", -1,
					new StandardCallback<List<String>>() {

						@Override
						protected void doSuccess(final List<String> result) {
							if (result.isEmpty()) {
								lb.setEnabled(false);
								lb.addItem("No Projects");
							} else {
								lb.setEnabled(true);
								for (final String project : result) {
									lb.addItem(project);
								}
							}
						}
					});
			return lb;
		} else if (param.getType() == Parameter.Type.IMPORTANCE) {
			return createListBox(5, "50%", "Critical", "High", "Medium", "Low",
					"Irrelevant");
		}

		final TextBox tb = new TextBox();
		tb.setWidth("100%");
		return tb;
	}

	private ListBox createListBox(final int visibleItemCount,
			final String width, final String... items) {
		final ListBox lb = new ListBox(true);
		lb.setWidth(width);
		lb.setVisibleItemCount(visibleItemCount);
		if (items != null) {
			for (final String item : items) {
				lb.addItem(item);
			}
		}
		return lb;
	}

}
