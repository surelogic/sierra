package com.surelogic.sierra.gwt.client.content.projects;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.table.TableSection;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class ProjectView extends BlockPanel {

	private final VerticalPanel chart = new VerticalPanel();
	private final ProjectTableSection scans = new ProjectTableSection();

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(chart);
		contentPanel.add(scans);
	}

	public void setSelection(final Project project) {
		if (project == null) {
			setSummary("Select a project");
		} else {
			setSummary(project.getName());
			chart.clear();
			chart.add(ChartBuilder.name("ProjectFindingsChart").width(800)
					.prop("projectName", project.getName()).build());
			chart.add(ChartBuilder.name("ProjectCompilationsChart").width(800)
					.prop("projectName", project.getName()).build());

			// TODO find the scan filters by looping through ScanFilterCache
			// make sure ScanFilterCache is populated
		}
		scans.update(ContextManager.getContext());
	}

	private static class ProjectTableSection extends TableSection {

		private List<Scan> scans;

		@Override
		protected ColumnData[] getHeaderDataTypes() {
			return new ColumnData[] { ColumnData.DATE, ColumnData.TEXT,
					ColumnData.TEXT, ColumnData.TEXT };
		}

		@Override
		protected String[] getHeaderTitles() {
			return new String[] { "Time", "User", "Vendor", "Version" };
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
								scans = result;
								setSuccessStatus(null);
								clearRows();
								for (final Scan s : result) {
									addRow();
									addColumn(s.getScanTime());
									addColumn(s.getUser());
									addColumn(s.getJavaVendor());
									addColumn(s.getJavaVersion());
								}
							}
						});
			}
		}

	}
}
