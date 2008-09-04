package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.ReportWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.Direction;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.panel.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ChartPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ColumnPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ReportTablePanel;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private static final String ACTION_CUSTOMIZE = "Customize";
	private static final String ACTION_SAVE = "Save";
	private static final String ACTION_CANCEL = "Cancel";
	private final ActionPanel actionPanel = new ActionPanel();
	private final VerticalPanel dashboard = new VerticalPanel();
	private final List<BlockPanel> dashboardWidgetUIs = new ArrayList<BlockPanel>();

	public static OverviewContent getInstance() {
		return instance;
	}

	private OverviewContent() {
		// singleton
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		final HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setWidth("100%");
		titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		titlePanel.add(new HTML("<p>Welcome to Sierra Team Server!</p>"));
		actionPanel.addAction(ACTION_CUSTOMIZE, new ClickListener() {

			public void onClick(final Widget sender) {
				toggleDashboardEdit(true);
			}
		});
		actionPanel.addAction(ACTION_SAVE, new ClickListener() {

			public void onClick(final Widget sender) {
				saveDashboard();
			}
		});
		actionPanel.addAction(ACTION_CANCEL, new ClickListener() {

			public void onClick(final Widget sender) {
				toggleDashboardEdit(false);
			}
		});
		titlePanel.add(actionPanel);
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		rootPanel.add(titlePanel, DockPanel.NORTH);

		rootPanel.add(dashboard, DockPanel.CENTER);
		dashboard.addStyleName("dashboard");
		dashboard.setWidth("100%");

		retrieveDashboard();
	}

	private void retrieveDashboard() {
		dashboard.clear();
		final Image waitImg = ImageHelper.getWaitImage(32);
		dashboard.add(waitImg);
		dashboard.setCellHorizontalAlignment(waitImg,
				HasHorizontalAlignment.ALIGN_CENTER);

		ServiceHelper.getSettingsService().getDashboardSettings(
				new StandardCallback<DashboardSettings>() {

					@Override
					protected void doSuccess(final DashboardSettings result) {
						updateDashboardUI(result);
						toggleDashboardEdit(false);
					}

				});
	}

	private void updateDashboardUI(final DashboardSettings result) {
		dashboard.clear();
		dashboardWidgetUIs.clear();

		int lastColumnCount = 0;
		ColumnPanel currentColPanel = null;
		for (final DashboardRow row : result.getRows()) {
			final List<DashboardWidget> cols = row.getColumns();
			if (currentColPanel == null || lastColumnCount != cols.size()) {
				currentColPanel = new ColumnPanel();
				dashboard.add(currentColPanel);
			}
			for (int i = 0; i < cols.size(); i++) {
				final DashboardWidget dw = cols.get(i);
				if (dw != null) {
					final BlockPanel widgetUI = createUI(dw);
					dashboardWidgetUIs.add(widgetUI);
					currentColPanel.addWidget(i, widgetUI);
				}
			}
			lastColumnCount = cols.size();
		}
	}

	private BlockPanel createUI(final DashboardWidget dw) {
		if (dw instanceof ReportWidget) {
			final ReportWidget rw = (ReportWidget) dw;
			final OutputType outputType = rw.getOutputType();
			if (outputType == OutputType.CHART) {
				return new ChartPanel(rw.getSettings());
			} else if (outputType == OutputType.TABLE) {
				return new ReportTablePanel(rw.getSettings());
			}
			throw new IllegalArgumentException("Unsupported output type: "
					+ rw.getOutputType());
		}
		throw new IllegalArgumentException("Unsupported dashboard widget: "
				+ dw.getClass().getName());
	}

	@Override
	protected void onUpdate(final Context context) {
		retrieveDashboard();
	}

	@Override
	protected void onDeactivate() {
		dashboard.clear();
	}

	private void toggleDashboardEdit(final boolean editMode) {
		actionPanel.setActionVisible(ACTION_CUSTOMIZE, !editMode);
		actionPanel.setActionVisible(ACTION_SAVE, editMode);
		actionPanel.setActionVisible(ACTION_CANCEL, editMode);

		// the actual supported actions should be inside a SectionPanel subclass
		// for instance, View Report must only show on report-sourced widgets
		for (final BlockPanel dashPanel : dashboardWidgetUIs) {
			dashPanel.removeActions();
			if (editMode) {
				final HorizontalPanel movementActions = new HorizontalPanel();
				movementActions.add(createArrowImage(dashPanel, Direction.UP));
				movementActions
						.add(createArrowImage(dashPanel, Direction.DOWN));
				movementActions
						.add(createArrowImage(dashPanel, Direction.LEFT));
				movementActions
						.add(createArrowImage(dashPanel, Direction.RIGHT));
				dashPanel.addAction(movementActions);
				dashPanel.addAction("Remove", new ClickListener() {

					public void onClick(final Widget sender) {
						removeSection(dashPanel);
					}
				});
			} else {
				dashPanel.addAction("View Report", new ClickListener() {

					public void onClick(final Widget sender) {
						viewReport(dashPanel);
					}
				});
			}
		}
	}

	private Image createArrowImage(final BlockPanel dashPanel,
			final Direction direction) {
		return ImageHelper.getArrowImage(direction, new ClickListener() {

			public void onClick(final Widget sender) {
				moveSection(dashPanel, direction);
			}
		});
	}

	private void moveSection(final BlockPanel dashPanel,
			final Direction direction) {
		// TODO Auto-generated method stub

	}

	private void removeSection(final BlockPanel dashPanel) {
		// TODO Auto-generated method stub

	}

	private void saveDashboard() {
		// TODO save the dashboard and leave edit mode

		toggleDashboardEdit(false);
	}

	private void viewReport(final BlockPanel dashPanel) {
		// TODO navigate to the proper Reports content based on report uuid and
		// isTeamServer etc

	}
}
