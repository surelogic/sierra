package com.surelogic.sierra.gwt.client.content.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
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
import com.surelogic.sierra.gwt.client.content.scans.PackageChoice;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.data.Report.Parameter.Type;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.data.cache.FindingTypeCache;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.choice.MultipleImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.choice.ProjectChoice;
import com.surelogic.sierra.gwt.client.ui.choice.ScanChoice;
import com.surelogic.sierra.gwt.client.ui.choice.SingleImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.panel.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportParametersView extends BasicPanel {
	private final Label description = new Label("", true);
	private final FlexTable parametersTable = new FlexTable();
	private final ActionPanel reportActions = new ActionPanel();
	private BasicPanel settingsPanel;
	private final Map<Report.Parameter, Widget> paramUIMap = new HashMap<Report.Parameter, Widget>();
	private final Map<OutputType, Label> actionOutputMap = new HashMap<OutputType, Label>();
	private Report selection;
	private ReportSettings selectionSettings;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		final VerticalPanel paramPanel = new VerticalPanel();
		paramPanel.setWidth("100%");
		description.addStyleName("padded");
		paramPanel.add(description);

		parametersTable.setWidth("100%");
		paramPanel.add(parametersTable);

		paramPanel.add(reportActions);
		paramPanel.setCellHorizontalAlignment(reportActions,
				HasHorizontalAlignment.ALIGN_CENTER);

		final HorizontalPanel h = new HorizontalPanel();
		h.add(paramPanel);
		h.setCellWidth(paramPanel, "70%");
		final Label placeholder = new Label("");
		h.add(placeholder);
		h.setCellWidth(placeholder, "5%");
		settingsPanel = new BasicPanel() {

			@Override
			protected void onInitialize(final VerticalPanel contentPanel) {
				setTitle("Saved Reports");
				setSubsectionStyle(true);
			}
		};
		settingsPanel.initialize();
		h.add(settingsPanel);
		h.setCellWidth(settingsPanel, "25%");
		h.setWidth("100%");
		contentPanel.add(h);
	}

	public void setSelection(final Report report, final ReportSettings settings) {
		selection = report;
		selectionSettings = settings;
		if (report != null) {
			setSummary(report.getTitle());
		} else {
			setSummary("Select a report");
		}

		final String desc = report == null ? "" : report.getLongDescription();
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
			rowIndex = addParameterUI(param, settings, rowIndex);
		}
		wireParameterUpdates();
		parametersTable.getColumnFormatter().setWidth(0, "33%");
		parametersTable.getColumnFormatter().setWidth(1, "67%");

		for (final Map.Entry<OutputType, Label> actionEntry : actionOutputMap
				.entrySet()) {
			actionEntry.getValue().setVisible(
					report.hasOutputType(actionEntry.getKey()));
		}

		final List<ReportSettings> savedReports = report.getSavedReports();
		final VerticalPanel settingsContent = settingsPanel.getContentPanel();
		settingsContent.clear();
		if (savedReports.isEmpty()) {
			settingsContent.add(StyleHelper.add(new Label("No reports saved"),
					Style.ITALICS));
		} else {
			for (final ReportSettings rs : savedReports) {
				final Context reportContext = Context.current().setUuid(report);
				settingsContent.add(new Hyperlink(rs.getTitle(), reportContext
						.setParameter("reportSettingsUuid", rs.getUuid())
						.toString()));
			}
		}
	}

	// Set up ui triggers between widgets.
	private void wireParameterUpdates() {
		for (final Entry<Parameter, Widget> entry : paramUIMap.entrySet()) {
			final Parameter param = entry.getKey();
			for (final Parameter child : param.getChildren()) {
				switch (param.getType()) {
				case PROJECT:
					if (child.getType() == Type.SCANS
							|| child.getType() == Type.SCAN) {
						final ProjectChoice pc = (ProjectChoice) entry
								.getValue();
						final ScanChoice sc = (ScanChoice) paramUIMap
								.get(child);
						pc.addChangeListener(new ChangeListener() {
							public void onChange(final Widget sender) {
								sc.displayProjectScans(pc.getValue(pc
										.getSelectedIndex()));
							}
						});
					}
					break;
				case SCAN:
					if (child.getType() == Type.PACKAGES) {
						final ScanChoice sc = (ScanChoice) entry.getValue();
						final PackageChoice pc = (PackageChoice) paramUIMap
								.get(child);
						sc.addChangeListener(new ChangeListener() {
							public void onChange(final Widget sender) {
								pc.displayScanPackages(sc.getValue(sc
										.getSelectedIndex()));
							}
						});
					}
					break;
				default:
					break;
				}
			}
		}
	}

	private int addParameterUI(final Report.Parameter param,
			final ReportSettings settings, int rowIndex) {
		parametersTable.setText(rowIndex, 0, param.getTitle() + ":");
		parametersTable.getCellFormatter().setVerticalAlignment(rowIndex, 0,
				HasVerticalAlignment.ALIGN_TOP);
		final Widget paramUI = getParameterUI(param, settings == null ? null
				: settings.getSettingValue(param.getName()));
		parametersTable.setWidget(rowIndex, 1, paramUI);
		paramUIMap.put(param, paramUI);
		rowIndex++;
		for (final Parameter child : param.getChildren()) {
			rowIndex = addParameterUI(child, settings, rowIndex);
		}
		return rowIndex;
	}

	public Report getSelection() {
		return selection;
	}

	public ReportSettings getReportSettings() {
		final ReportSettings settings = new ReportSettings(selection);
		if (selectionSettings != null) {
			settings.setUuid(selectionSettings.getUuid());
		}
		for (final Map.Entry<Report.Parameter, Widget> paramEntry : paramUIMap
				.entrySet()) {
			final String paramName = paramEntry.getKey().getName();
			final Widget paramUI = paramEntry.getValue();
			if (paramUI instanceof TextBox) {
				settings.setSettingValue(paramName, ((TextBox) paramUI)
						.getText());
			} else if (paramUI instanceof MultipleImportanceChoice) {
				final List<String> values = new ArrayList<String>();
				for (final ImportanceView imp : ((MultipleImportanceChoice) paramUI)
						.getSelectedImportances()) {
					values.add(imp.getName());
				}
				settings.setSettingValue(paramName, values);
			} else if (paramUI instanceof ListBox) {
				final List<String> values = new ArrayList<String>();
				final ListBox lb = (ListBox) paramUI;
				for (int i = 0; i < lb.getItemCount(); i++) {
					if (lb.isItemSelected(i)) {
						values.add(lb.getValue(i));
					}
				}
				settings.setSettingValue(paramName, values);
			} else if (paramUI instanceof CheckBox) {
				final CheckBox cb = (CheckBox) paramUI;
				if (cb.isChecked()) {
					settings.setSettingValue(paramName, "true");
				}
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

	private Widget getParameterUI(final Parameter param, List<String> values) {
		if (values == null) {
			values = Collections.emptyList();
		}
		// List declaration for the callback
		final List<String> lbValues = values;
		switch (param.getType()) {
		case PROJECT:
			return new ProjectChoice(lbValues, false);
		case PROJECTS:
			return new ProjectChoice(lbValues, true);
		case IMPORTANCE:
			return new SingleImportanceChoice();
		case IMPORTANCES:
			final MultipleImportanceChoice choice = new MultipleImportanceChoice();
			final List<ImportanceView> imps = new ArrayList<ImportanceView>(
					values.size());
			for (final String imp : values) {
				imps.add(ImportanceView.fromString(imp));
			}
			choice.setSelectedImportances(imps);
			choice.setWidth("50%");
			return choice;
		case BOOLEAN:
			return new CheckBox();
		case CATEGORIES:
			final ListBox catBs = new ListBox(true);
			catBs.setWidth("100%");
			CategoryCache.getInstance().refresh(false,
					new CacheListenerAdapter<Category>() {
						@Override
						public void onRefresh(final Cache<Category> cache,
								final Throwable failure) {
							for (final Category cat : cache) {
								catBs.addItem(cat.getName(), cat.getUuid());
							}
						}
					});
			return catBs;
		case CATEGORY:
			final ListBox catB = new ListBox(false);
			catB.setWidth("100%");
			CategoryCache.getInstance().refresh(false,
					new CacheListenerAdapter<Category>() {
						@Override
						public void onRefresh(final Cache<Category> cache,
								final Throwable failure) {
							for (final Category cat : cache) {
								catB.addItem(cat.getName(), cat.getUuid());
							}
						}
					});
			return catB;
		case FINDING_TYPES:
			final ListBox ftBs = new ListBox(true);
			ftBs.setWidth("100%");
			FindingTypeCache.getInstance().refresh(false,
					new CacheListenerAdapter<FindingType>() {
						@Override
						public void onRefresh(final Cache<FindingType> cache,
								final Throwable failure) {
							for (final FindingType type : cache) {
								ftBs.addItem(type.getName(), type.getUuid());
							}
						}
					});
			return ftBs;
		case FINDING_TYPE:
			final ListBox ftB = new ListBox(false);
			ftB.setWidth("100%");
			FindingTypeCache.getInstance().refresh(false,
					new CacheListenerAdapter<FindingType>() {
						@Override
						public void onRefresh(final Cache<FindingType> cache,
								final Throwable failure) {
							for (final FindingType type : cache) {
								ftB.addItem(type.getName(), type.getUuid());
							}
						}
					});
			return ftB;
		case PACKAGES:
			return new PackageChoice();
		case SCAN:
			return new ScanChoice(false);
		case SCANS:
			return new ScanChoice(true);
		default:
			final TextBox tb = new TextBox();
			tb.setWidth("100%");
			if (!values.isEmpty()) {
				tb.setText(values.get(0));
			}
			return tb;
		}
	}

}
