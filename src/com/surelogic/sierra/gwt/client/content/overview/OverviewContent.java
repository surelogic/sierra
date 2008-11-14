package com.surelogic.sierra.gwt.client.content.overview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.overview.DashboardBlock.DashboardListener;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.ReportWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.block.ChartBlock;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlockPanel;
import com.surelogic.sierra.gwt.client.ui.block.ReportTableBlock;
import com.surelogic.sierra.gwt.client.ui.panel.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ColumnPanel;
import com.surelogic.sierra.gwt.client.ui.type.Direction;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private static final String ACTION_CUSTOMIZE = "Customize Dashboard";
	private static final String ACTION_SAVE = "Save";
	private static final String ACTION_CANCEL = "Cancel";
	private final ActionPanel actionPanel = new ActionPanel();
	private DashboardSettings settings;
	private final VerticalPanel dashboard = new VerticalPanel();
	private final Map<ContentBlockPanel, DashboardBlock> dashboardWidgetUIs = new HashMap<ContentBlockPanel, DashboardBlock>();
	private boolean editMode;

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
		final Label caption = new Label("Welcome to Sierra Team Server!");
		caption.addStyleName(ContentComposite.CAPTION_STYLE);
		titlePanel.add(caption);
		actionPanel.addAction(ACTION_CUSTOMIZE, new ClickListener() {

			public void onClick(final Widget sender) {
				toggleEditMode(true);
			}
		});
		actionPanel.addAction(ACTION_SAVE, new ClickListener() {

			public void onClick(final Widget sender) {
				saveDashboard();
			}
		});
		actionPanel.addAction(ACTION_CANCEL, new ClickListener() {

			public void onClick(final Widget sender) {
				retrieveDashboard();
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
						editMode = false;
						result.cleanup();
						updateDashboardUI(result);
					}

				});
	}

	private void updateDashboardUI(final DashboardSettings result) {
		settings = result;
		dashboard.clear();
		dashboardWidgetUIs.clear();

		int lastColumnCount = 0;
		ColumnPanel currentColPanel = null;
		int rowIndex = 0;
		final List<DashboardRow> rows = settings.getRows();
		for (final DashboardRow row : rows) {
			final List<DashboardWidget> cols = row.getColumns();
			if (currentColPanel == null || lastColumnCount != cols.size()) {
				currentColPanel = new ColumnPanel();
				dashboard.add(currentColPanel);
			}
			for (int i = 0; i < cols.size(); i++) {
				final DashboardWidget dw = cols.get(i);
				if (dw != null) {
					final DashboardBlock db = createWidgetUI(dw);
					db.updateMovementActions(getAllowedMovements(settings,
							rowIndex, rows.size(), i, cols.size()));
					final ContentBlockPanel cbp = new ContentBlockPanel(db);
					cbp.initialize();
					cbp.setWidth("100%");
					currentColPanel.addWidget(i, cbp);
					dashboardWidgetUIs.put(cbp, db);
				}
			}
			lastColumnCount = cols.size();
			rowIndex++;
		}

		toggleEditMode(editMode);
	}

	private DashboardBlock createWidgetUI(final DashboardWidget dw) {
		if (dw instanceof ReportWidget) {
			final ReportWidget rw = (ReportWidget) dw;
			final OutputType outputType = rw.getOutputType();
			DashboardBlock db = null;
			if (outputType == OutputType.CHART) {
				db = new DashboardBlock(new ChartBlock(rw.getSettings()));
			} else if (outputType == OutputType.TABLE) {
				db = new DashboardBlock(new ReportTableBlock(rw.getSettings()));
			}
			if (db != null) {
				db.addDashboardListener(new EditModeListener(dw));
				return db;
			} else {
				throw new IllegalArgumentException("Unsupported output type: "
						+ rw.getOutputType());
			}
		}
		throw new IllegalArgumentException("Unsupported dashboard widget: "
				+ dw.getClass().getName());
	}

	private Set<Direction> getAllowedMovements(
			final DashboardSettings settings, final int rowIndex,
			final int rowCount, final int colIndex, final int colCount) {
		final Set<Direction> allowedMovements = new HashSet<Direction>();
		boolean horzMovementAllowed;
		if (colCount == 1) {
			// TODO temp remove - allowedMovements.add(Direction.GROW);
			horzMovementAllowed = false;
		} else {
			// TODO temp remove - allowedMovements.add(Direction.SHRINK);
			horzMovementAllowed = true;
		}
		if (horzMovementAllowed && colIndex > 0) {
			allowedMovements.add(Direction.LEFT);
		}
		if (horzMovementAllowed && colIndex == 0) {
			allowedMovements.add(Direction.RIGHT);
		}
		if (rowIndex > 0) {
			allowedMovements.add(Direction.UP);
		}
		if (rowIndex < rowCount - 1) {
			allowedMovements.add(Direction.DOWN);
		}
		return allowedMovements;
	}

	@Override
	protected void onUpdate(final Context context) {
		retrieveDashboard();
	}

	@Override
	protected void onDeactivate() {
		dashboard.clear();
	}

	private void toggleEditMode(final boolean editMode) {
		this.editMode = editMode;
		actionPanel.setActionVisible(ACTION_CUSTOMIZE, !editMode);
		actionPanel.setActionVisible(ACTION_SAVE, editMode);
		actionPanel.setActionVisible(ACTION_CANCEL, editMode);

		for (final DashboardBlock dashPanel : dashboardWidgetUIs.values()) {
			dashPanel.setEditMode(editMode);
		}
	}

	private void saveDashboard() {
		actionPanel.setWaitStatus();
		ServiceHelper.getSettingsService().saveDashboardSettings(settings,
				new StatusCallback() {

					@Override
					protected void doStatus(final Status status) {
						actionPanel.clearWaitStatus();
						if (status.isSuccess()) {
							toggleEditMode(false);
						} else {
							Window
									.alert("Dashboard Settings could not be saved: "
											+ status.getMessage());
						}
					}
				});
	}

	private final class EditModeListener implements DashboardListener {
		private final DashboardWidget dashboardWidget;

		public EditModeListener(final DashboardWidget dw) {
			super();
			this.dashboardWidget = dw;
		}

		public void onMove(final DashboardBlock block, final Direction direction) {
			if (settings.moveWidget(dashboardWidget, direction)) {
				settings.cleanup();
				updateDashboardUI(settings);
			}
		}

		public void onRemove(final DashboardBlock block) {
			settings.removeWidget(dashboardWidget);
			settings.cleanup();
			updateDashboardUI(settings);
		}

	}

}
