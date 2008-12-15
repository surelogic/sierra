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
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ProjectCache;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.ui.TableBuilder;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlock;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlockPanel;
import com.surelogic.sierra.gwt.client.ui.block.ReportTableBlock;
import com.surelogic.sierra.gwt.client.ui.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;

public class ProjectView extends BasicPanel {
	private final StatusBox box = new StatusBox();
	private ScanDetailView latestScan;
	private final VerticalPanel chart = new VerticalPanel();
	private final VerticalPanel diff = new VerticalPanel();
	private final ProjectTableBlock scans = new ProjectTableBlock();
	private Project selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		latestScan = new ScanDetailView(new ClickListener() {

			public void onClick(final Widget sender) {
				promptForScanFilter();
			}
		});
		latestScan.setWidth("50%");
		contentPanel.add(latestScan);
		contentPanel.add(chart);
		final ContentBlockPanel scansPanel = new ContentBlockPanel(scans);
		scansPanel.initialize();
		contentPanel.add(scansPanel);
		contentPanel.add(diff);
		contentPanel.add(box);
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
			chart.add(ChartBuilder.report(ReportCache.projectFindings()).width(
					800).prop("projectName", project.getName()).prop("kLoC",
					"true").build());
			chart.add(ChartBuilder.report(ReportCache.projectCompilations())
					.width(800).prop("projectName", project.getName()).prop(
							"kLoC", "true").build());

			ServiceHelper.getFindingService().getLatestScanDetail(
					project.getUuid(), new StandardCallback<ScanDetail>() {
						@Override
						protected void doSuccess(final ScanDetail result) {
							latestScan.setScan(project, result);
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

	private class ProjectTableBlock extends ContentBlock<FlexTable> {
		private Map<Scan, CheckBox> scans;

		public ProjectTableBlock() {
			super(new FlexTable());
			setupTable();
		}

		@Override
		public String getName() {
			return "Scans";
		}

		@Override
		public String getSummary() {
			return null;
		}

		private void setupTable() {
			final TableBuilder tb = new TableBuilder(getRoot());
			tb.setHeaderTitles(new String[] { "Time", "User", "Vendor",
					"Version", "" });
			tb.setColumnTypes(new ColumnDataType[] { ColumnDataType.DATE,
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
						r1.setReport(ReportCache.scanFindings());
						r1.setTitle("New Findings");
						r1.setSettingValue("scans", newF);
						final ReportSettings r2 = new ReportSettings();
						r2.setReport(ReportCache.scanFindings());
						r2.setTitle("Fixed Findings");
						r2.setSettingValue("scans", fixed);
						diff.clear();
						diff
								.add(new ContentBlockPanel(
										new ReportTableBlock(r1)));
						diff
								.add(new ContentBlockPanel(
										new ReportTableBlock(r2)));
						diff.add(ChartBuilder.report(
								ReportCache.compareProjectScans()).prop(
								"scans", fixed).build());
					} else {
						box.setStatus(Status.failure("You have selected "
								+ toCompare.size()
								+ " scans.  You should select at least two."));
					}
				}
			});
		}

		public void setSelection(final Project project) {
			final TableBuilder tb = new TableBuilder(getRoot());
			tb.clearRows();
			setWaitStatus();
			if (project != null) {
				ServiceHelper.getFindingService().getScans(project.getUuid(),
						new StandardCallback<List<Scan>>() {

							@Override
							protected void doSuccess(final List<Scan> result) {
								scans = new HashMap<Scan, CheckBox>();
								setSuccessStatus(null);
								tb.clearRows();
								for (final Scan s : result) {
									tb.addRow();
									final Context scanCtx = new Context(
											ScanContent.getInstance(), s);
									final Hyperlink h = new Hyperlink(s
											.getScanTimeDisplay(), scanCtx
											.toString());
									tb.addColumn(h);
									tb.addColumn(s.getUser());
									tb.addColumn(s.getJavaVendor());
									tb.addColumn(s.getJavaVersion());
									final CheckBox box = new CheckBox();
									scans.put(s, box);
									tb.addColumn(box);
								}
							}
						});
			}
		}

	}

}
