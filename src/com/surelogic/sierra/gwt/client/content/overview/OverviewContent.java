package com.surelogic.sierra.gwt.client.content.overview;

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
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.ColumnPanel;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private static final String ACTION_CUSTOMIZE = "Customize";
	private static final String ACTION_SAVE = "Save";
	private static final String ACTION_CANCEL = "Cancel";
	private final ActionPanel actionPanel = new ActionPanel();
	private final VerticalPanel dashboard = new VerticalPanel();
	private DashboardSettings currentSettings;

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
		toggleDashboardEdit(false);

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
					}

				});
	}

	private void updateDashboardUI(final DashboardSettings result) {
		currentSettings = result;
		dashboard.clear();

		int lastColumnCount = 0;
		ColumnPanel currentColPanel = null;
		for (final DashboardRow row : result.getRows()) {
			if (currentColPanel == null
					|| lastColumnCount != row.getColumns().size()) {
				currentColPanel = new ColumnPanel();
				dashboard.add(currentColPanel);
			}
			for (final DashboardWidget dw : row.getColumns()) {
				currentColPanel.addWidget(createUI(dw));
			}
			lastColumnCount = row.getColumns().size();
		}
	}

	private Widget createUI(final DashboardWidget dw) {

		// TODO Auto-generated method stub

		// also add the widget to a list so toggleDashboardEdit can easily
		// iterate through them
		return null;
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

		// for (final SectionPanel dashPanel : sections) {
		// dashPanel.removeActions();
		// if (editMode) {
		// final HorizontalPanel movementActions = new HorizontalPanel();
		// movementActions.add(createArrowImage(dashPanel, Direction.UP));
		// movementActions
		// .add(createArrowImage(dashPanel, Direction.DOWN));
		// movementActions
		// .add(createArrowImage(dashPanel, Direction.LEFT));
		// movementActions
		// .add(createArrowImage(dashPanel, Direction.RIGHT));
		// dashPanel.addAction(movementActions);
		// dashPanel.addAction("Remove", new ClickListener() {
		//
		// public void onClick(final Widget sender) {
		// removeSection(dashPanel);
		// }
		// });
		// } else {
		// dashPanel.addAction("View Report", new ClickListener() {
		//
		// public void onClick(final Widget sender) {
		// viewReport(dashPanel);
		// }
		// });
		// }
		// }
	}

	// private Image createArrowImage(final SectionPanel dashPanel,
	// final Direction direction) {
	// return ImageHelper.getArrowImage(direction, new ClickListener() {
	//
	// public void onClick(final Widget sender) {
	// moveSection(dashPanel, direction);
	// }
	// });
	// }
	//
	// private void moveSection(final SectionPanel dashPanel,
	// final Direction direction) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// private void removeSection(final SectionPanel dashPanel) {
	// // TODO Auto-generated method stub
	//
	// }

	private void saveDashboard() {
		// TODO save the dashboard and leave edit mode

		toggleDashboardEdit(false);
	}

	// private void viewReport(final SectionPanel dashPanel) {
	// // TODO navigate to the proper Reports content based on report uuid and
	// // isTeamServer etc
	//
	// }
}
