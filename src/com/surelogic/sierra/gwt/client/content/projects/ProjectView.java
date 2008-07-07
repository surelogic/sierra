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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.table.TableSection;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ContentLink;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class ProjectView extends BlockPanel {
	private final VerticalPanel chart = new VerticalPanel();
	private final VerticalPanel diff = new VerticalPanel();
	private final ProjectTableSection scans = new ProjectTableSection();
	private final VerticalPanel scanFilters = new VerticalPanel();

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(chart);
		contentPanel.add(scans);

		scanFilters.setWidth("100%");
		contentPanel.add(scanFilters);
	}

	public void setSelection(final Project project) {
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

			final ScanFilterCache sfCache = ScanFilterCache.getInstance();
			scanFilters.clear();
			boolean filterMatch = false;
			for (final ScanFilter scanFilter : sfCache) {
				if (scanFilter.getProjects().contains(projectName)) {
					final ContentLink sfLink = new ContentLink(scanFilter
							.getName(), ScanFiltersContent.getInstance(),
							scanFilter.getUuid());
					scanFilters.add(sfLink);
					filterMatch = true;
				}
			}
			if (!filterMatch) {
				final ScanFilter global = sfCache.getGlobalFilter();
				if (global != null) {
					final ContentLink sfLink = new ContentLink(
							global.getName(), ScanFiltersContent.getInstance(),
							global.getUuid());
					scanFilters.add(sfLink);
				}
			}
		}
		scans.update(ContextManager.getContext());
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
							toCompare.add(entry.getKey());
						}
					}
					Collections.sort(toCompare, new Comparator<Scan>() {
						public int compare(final Scan o1, final Scan o2) {
							return o1.getScanTime().compareTo(o2.getScanTime());
						}
					});
					if (scans.size() == 2) {
						diff.add(ChartBuilder.name("CompareProjectScans").prop(
								"first", toCompare.get(0).getUuid()).prop(
								"second", toCompare.get(1).getUuid()).build());
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
									addColumn(s.getScanTimeDisplay());
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
