package com.surelogic.sierra.gwt.client.content.scans;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.table.ReportTableSection;
import com.surelogic.sierra.gwt.client.ui.HtmlHelper;
import com.surelogic.sierra.gwt.client.ui.ImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class ScanContent extends ContentComposite {

	ScanView view = new ScanView();

	@Override
	protected void onDeactivate() {
		// Do nothing
		view.onDeactivate();
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		rootPanel.add(view, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(final Context context) {
		view.update(context);
	}

	private static class ScanView extends SectionPanel {

		private VerticalPanel optionsPanel;
		private VerticalPanel chartPanel;
		private VerticalPanel detailPanel;

		private ImportanceChoice imp;
		private PackageChoice pak;
		private String uuid;

		@Override
		protected void onDeactivate() {
			setTitle("No Scan Selected");
			optionsPanel.clear();
		}

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("No Scan Selected");
			optionsPanel = new VerticalPanel();
			chartPanel = new VerticalPanel();
			detailPanel = new VerticalPanel();
			final HorizontalPanel panel = new HorizontalPanel();
			panel.add(optionsPanel);
			final VerticalPanel vPanel = new VerticalPanel();
			vPanel.add(detailPanel);
			vPanel.add(chartPanel);
			panel.add(vPanel);
			contentPanel.add(panel);
		}

		@Override
		protected void onUpdate(final Context context) {
			final String uuid = context.getUuid();
			if ((uuid != null) && !(uuid.length() == 0)) {
				setScan(uuid);
			}
		}

		private void setScan(final String uuid) {
			this.uuid = uuid;
			optionsPanel.clear();
			chartPanel.clear();
			detailPanel.clear();
			ServiceHelper.getFindingService().getScanDetail(uuid,
					new StandardCallback<ScanDetail>() {

						protected void doSuccess(final ScanDetail result) {
							setTitle(result.getProject() + " - "
									+ result.getDate());
							detailPanel.add(HtmlHelper.p(result.getClasses()));
							detailPanel.add(HtmlHelper.p(result.getPackages()));
							detailPanel.add(HtmlHelper.p(result.getFindings()));
							detailPanel.add(HtmlHelper.p(result
									.getLinesOfCode()));
							detailPanel.add(HtmlHelper.p(result.getDensity()));
							pak = new PackageChoice(result.getCompilations()
									.keySet(), true);
							imp = new ImportanceChoice(true);
							optionsPanel.add(pak);
							optionsPanel.add(imp);
							optionsPanel.add(new Button("Show",
									new ClickListener() {
										public void onClick(final Widget sender) {
											showFindings();
										}
									}));
							showFindings();
						}
					});
		}

		private void showFindings() {
			chartPanel.clear();
			final List<String> importances = new ArrayList<String>();
			for (final ImportanceView i : imp.getSelectedImportances()) {
				importances.add(i.getName());
			}
			final Set<String> packages = pak.getSelectedPackages();
			chartPanel.add(ChartBuilder.name("ScanImportances").width(800)
					.prop("scan", uuid).prop("importance", importances).prop(
							"package", packages).build());
			final Report report = new Report();
			report.setName("ScanFindings");
			report.getParameters().add(new Parameter("scan", uuid));
			report.getParameters()
					.add(new Parameter("importance", importances));
			report.getParameters().add(new Parameter("package", packages));
			chartPanel.add(new ReportTableSection(report));
		}
	}

	private ScanContent() {
		// Do nothing
	}

	private static final ScanContent INSTANCE = new ScanContent();

	public static ScanContent getInstance() {
		return INSTANCE;
	}
}
