package com.surelogic.sierra.gwt.client.content.scans;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.ChartBuilder;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.MultipleImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlockPanel;
import com.surelogic.sierra.gwt.client.ui.block.ReportTableBlock;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class ScanContent extends ContentComposite {

	ScanView view = new ScanView();

	@Override
	protected void onDeactivate() {
		// Do nothing
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		rootPanel.add(view, DockPanel.CENTER);
		view.initialize();
	}

	@Override
	protected void onUpdate(final Context context) {
		view.setScan(context.getUuid());
	}

	private static class ScanView extends BlockPanel {

		private VerticalPanel optionsPanel;
		private VerticalPanel chartPanel;
		private VerticalPanel detailPanel;

		private MultipleImportanceChoice imp;
		private PackageChoice pak;
		private CategoryChoice cat;
		private String uuid;

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

		public void setScan(final String uuid) {
			this.uuid = uuid;
			optionsPanel.clear();
			chartPanel.clear();
			detailPanel.clear();
			ServiceHelper.getFindingService().getScanDetail(uuid,
					new StandardCallback<ScanDetail>() {

						@Override
						protected void doSuccess(final ScanDetail result) {
							if (result != null) {
								setTitle(result.getProject() + " - "
										+ result.getDate());
								detailPanel.add(new Label(result.getClasses()));
								detailPanel
										.add(new Label(result.getPackages()));
								detailPanel
										.add(new Label(result.getFindings()));
								detailPanel.add(new Label(result
										.getLinesOfCode()));
								detailPanel.add(new Label(result.getDensity()));
								pak = new PackageChoice(result
										.getCompilations().keySet(), true);
								imp = new MultipleImportanceChoice();
								cat = new CategoryChoice();
								optionsPanel.add(pak);
								optionsPanel.add(imp);
								optionsPanel.add(cat);
								optionsPanel.add(new Button("Show",
										new ClickListener() {
											public void onClick(
													final Widget sender) {
												showFindings();
											}
										}));
								showFindings();
							}
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
			final Set<String> categories = cat.getSelectedCategories();
			if (categories.isEmpty()) {
				chartPanel.add(ChartBuilder.report(
						ReportCache.scanImportances()).width(800).prop("scan",
						uuid).prop("importance", importances).prop("package",
						packages).build());
				final ReportSettings settings = new ReportSettings(ReportCache
						.scanFindings());
				settings.setSettingValue("scan", uuid);
				settings.setSettingValue("importance", importances);
				settings.setSettingValue("package", packages);
				chartPanel.add(new ContentBlockPanel(new ReportTableBlock(
						settings)));
			} else {
				chartPanel.add(ChartBuilder.report(
						ReportCache.scanImportancesByCategory()).width(800)
						.prop("scan", uuid).prop("importance", importances)
						.prop("package", packages).prop("category", categories)
						.build());
				final ReportSettings settings = new ReportSettings(ReportCache
						.scanFindingsByCategory());
				settings.setSettingValue("scan", uuid);
				settings.setSettingValue("importance", importances);
				settings.setSettingValue("package", packages);
				settings.setSettingValue("category", categories);
				chartPanel.add(new ReportTableBlock(settings));
			}

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
