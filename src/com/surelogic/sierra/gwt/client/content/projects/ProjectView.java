package com.surelogic.sierra.gwt.client.content.projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.table.ReportTableSection;
import com.surelogic.sierra.gwt.client.table.TableSection;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ContentLink;
import com.surelogic.sierra.gwt.client.ui.ItalicLabel;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class ProjectView extends BlockPanel {
	private final StatusBox box = new StatusBox();
	private final VerticalPanel chart = new VerticalPanel();
	private final VerticalPanel diff = new VerticalPanel();
	private final ProjectTableSection scans = new ProjectTableSection();
	private final ScanFiltersSection scanFilters = new ScanFiltersSection();
	private Project selection;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(chart);
		contentPanel.add(scans);
		contentPanel.add(diff);
		contentPanel.add(box);

		scanFilters.initialize();

		contentPanel.add(scanFilters);
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
			chart.add(ChartBuilder.name("ProjectFindingsChart").width(800)
					.prop("projectName", project.getName()).build());
			chart.add(ChartBuilder.name("ProjectCompilationsChart").width(800)
					.prop("projectName", project.getName()).build());

			scanFilters.clear();
			scanFilters.addScanFilter(project.getScanFilter());
		}
		scans.update(ContextManager.getContext());
	}

	public void addScanFilterAction(final String title,
			final ClickListener action) {
		scanFilters.addAction(title, action);
	}

	private class ProjectTableSection extends TableSection {

		private Map<Scan, CheckBox> scans;

		ProjectTableSection() {
			setTitle("Scans");
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
					Collections.sort(toCompare, new Comparator<Scan>() {
						public int compare(final Scan o1, final Scan o2) {
							return o1.getScanTime().compareTo(o2.getScanTime());
						}
					});
					if (toCompare.size() >= 2) {
						final List<String> scans = new ArrayList<String>(
								toCompare.size());
						for (final Scan s : toCompare) {
							scans.add(s.getUuid());
						}
						final Report r = new Report();
						r.setName("CompareProjectScans");
						r.setTitle("Scan comparison");
						r.getParameters().add(new Parameter("scans", scans));
						diff.clear();
						diff.add(ChartBuilder.name("CompareProjectScans").prop(
								"scans", scans).build());
						diff.add(new ReportTableSection(r));
					} else {
						box.setStatus(Status.failure("You have selected "
								+ toCompare.size()
								+ " scans.  You should select at least two."));
					}
				}
			});
		}

		@Override
		protected ColumnData[] getHeaderDataTypes() {
			return new ColumnData[] { ColumnData.DATE, ColumnData.TEXT,
					ColumnData.TEXT, ColumnData.TEXT, ColumnData.WIDGET };
		}

		@Override
		protected String[] getHeaderTitles() {
			return new String[] { "Time", "User", "Vendor", "Version", "" };
		}

		@Override
		protected void updateTable(final Context context) {
			clearRows();
			setWaitStatus();
			if (context.getUuid() != null) {
				ServiceHelper.getFindingService().getScans(context.getUuid(),
						new AsyncCallback<List<Scan>>() {
							public void onFailure(final Throwable caught) {
								setErrorStatus(caught.getMessage());
							}

							public void onSuccess(final List<Scan> result) {
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

	private static class ScanFiltersSection extends BlockPanel {

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("Scan Filters");
			setSubsectionStyle(true);
		}

		public void clear() {
			getContentPanel().clear();
		}

		public void addScanFilter(final ScanFilter sf) {
			final ContentLink sfLink = new ContentLink(sf.getName(),
					ScanFiltersContent.getInstance(), sf.getUuid());
			getContentPanel().add(sfLink);
		}

		public void loadingScanFilters() {
			clear();
			getContentPanel()
					.add(new ItalicLabel("Retrieving Scan Filters..."));
		}

		public void noScanFilters() {
			clear();
			getContentPanel().add(new ItalicLabel("No Scan Filters"));
		}
	}
}
