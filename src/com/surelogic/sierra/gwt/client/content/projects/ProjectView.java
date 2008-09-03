package com.surelogic.sierra.gwt.client.content.projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ProjectCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.table.ReportTablePanel;
import com.surelogic.sierra.gwt.client.ui.LabelHelper;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.panel.TablePanel;

public class ProjectView extends BlockPanel {
	private final StatusBox box = new StatusBox();
	private final ScanDetailView latestScan = new ScanDetailView();
	private final VerticalPanel chart = new VerticalPanel();
	private final VerticalPanel diff = new VerticalPanel();
	private final ProjectTableSection scans = new ProjectTableSection();
	private final FlexTable scanFilterTable = new FlexTable();
	private final HorizontalPanel scanFilterField = new HorizontalPanel();
	private Project selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(latestScan);
		contentPanel.add(chart);
		contentPanel.add(scans);
		contentPanel.add(diff);
		contentPanel.add(box);

		scanFilterTable.setWidth("50%");
		scanFilterTable.setWidget(0, 0, scanFilterField);
		scanFilterField.add(new Label("Scan Filter:"));
		final Label changeScanFilter = LabelHelper.clickable(new Label(
				"Change Scan Filter"), new ClickListener() {

			public void onClick(final Widget sender) {
				promptForScanFilter();
			}
		});
		scanFilterTable.setWidget(0, 1, changeScanFilter);
		scanFilterTable.getCellFormatter().setHorizontalAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_RIGHT);

		contentPanel.add(scanFilterTable);
	}

	public Project getSelection() {
		return selection;
	}

	public void setSelection(final Project project) {
		selection = project;
		if (project == null) {
			setSummary("Select a project");
		} else {
			final String projectName = project.getName();
			setSummary(projectName);

			diff.clear();
			chart.clear();
			chart.add(ChartBuilder.report("ProjectFindingsChart", "???", "???")
					.width(800).prop("projectName", project.getName()).prop(
							"kLoC", "true").build());
			chart.add(ChartBuilder.report("ProjectCompilationsChart", "???",
					"???").width(800).prop("projectName", project.getName())
					.prop("kLoC", "true").build());

			final ScanFilter sf = project.getScanFilter();
			final ContentLink sfLink = new ContentLink(sf.getName(),
					ScanFiltersContent.getInstance(), sf.getUuid());
			sfLink.setWidth("100%");
			if (scanFilterField.getWidgetCount() > 1) {
				scanFilterField.remove(1);
			}
			scanFilterField.add(sfLink);
			ServiceHelper.getFindingService().getLatestScanDetail(
					project.getUuid(), new StandardCallback<ScanDetail>() {
						@Override
						protected void doSuccess(final ScanDetail result) {
							latestScan.setScan(result);
						}
					});
		}
		scans.setSelection(project);
	}

	private void promptForScanFilter() {
		if (selection != null) {
			final ScanFilterDialog dialog = new ScanFilterDialog();
			dialog.addPopupListener(new PopupListener() {

				public void onPopupClosed(final PopupPanel sender,
						final boolean autoClosed) {
					final Status s = dialog.getStatus();
					if ((s != null) && s.isSuccess()) {
						selection.setScanFilter(dialog.getSelectedFilter());
						ProjectCache.getInstance().save(selection);
						ProjectCache.getInstance().refresh();
					}
				}

			});
			dialog.center();
			dialog.update(selection.getScanFilter());
		}
	}

	private class ProjectTableSection extends TablePanel {

		private Map<Scan, CheckBox> scans;

		@Override
		protected void doInitialize(final FlexTable grid) {
			setTitle("Scans");
			setHeaderTitles(new String[] { "Time", "User", "Vendor", "Version",
					"" });
			setColumnTypes(new ColumnDataType[] { ColumnDataType.DATE,
					ColumnDataType.TEXT, ColumnDataType.TEXT,
					ColumnDataType.TEXT, ColumnDataType.WIDGET });
			addAction("Compare", new ClickListener() {
				public void onClick(final Widget sender) {
					final List<Scan> toCompare = new ArrayList<Scan>();
					if (scans != null) {
						for (final Entry<Scan, CheckBox> entry : scans
								.entrySet()) {
							if (entry.getValue().isChecked()) {
								toCompare.add(entry.getKey());
							}
						}
					}
					Collections.sort(toCompare);
					if (toCompare.size() >= 2) {
						final ArrayList<String> fixed = new ArrayList<String>(
								toCompare.size());
						final ArrayList<String> newF = new ArrayList<String>(
								toCompare.size());
						for (final Scan s : toCompare) {
							fixed.add(0, s.getUuid());
							newF.add(s.getUuid());
						}
						final ReportSettings r1 = new ReportSettings();
						r1.setReportUuid("ScanFindingsComparison");
						r1.setTitle("New Findings");
						r1.setSettingValue("scans", newF);
						final ReportSettings r2 = new ReportSettings();
						r2.setReportUuid("ScanFindingsComparison");
						r2.setTitle("Fixed Findings");
						r2.setSettingValue("scans", fixed);
						diff.clear();
						diff.add(new ReportTablePanel(r1));
						diff.add(new ReportTablePanel(r2));
						diff.add(ChartBuilder.report("CompareProjectScans",
								"???", "???").prop("scans", fixed).build());
					} else {
						box.setStatus(Status.failure("You have selected "
								+ toCompare.size()
								+ " scans.  You should select at least two."));
					}
				}
			});
		}

		public void setSelection(final Project project) {
			clearRows();
			setWaitStatus();
			if (project != null) {
				ServiceHelper.getFindingService().getScans(project.getUuid(),
						new StandardCallback<List<Scan>>() {

							@Override
							protected void doSuccess(final List<Scan> result) {
								scans = new HashMap<Scan, CheckBox>();
								setSuccessStatus(null);
								clearRows();
								for (final Scan s : result) {
									addRow();
									final Context scanCtx = Context
											.createWithUuid(ScanContent
													.getInstance(), s.getUuid());
									final Hyperlink h = new Hyperlink(s
											.getScanTimeDisplay(), scanCtx
											.toString());
									addColumn(h);
									addColumn(s.getUser());
									addColumn(s.getJavaVendor());
									addColumn(s.getJavaVersion());
									final CheckBox box = new CheckBox();
									scans.put(s, box);
									addColumn(box);
								}
							}
						});
			}
		}

	}

}
