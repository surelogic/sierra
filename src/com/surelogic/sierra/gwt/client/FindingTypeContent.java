package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public final class FindingTypeContent extends ContentComposite {

	private static final FindingTypeContent instance = new FindingTypeContent();

	private final HTML name = new HTML();
	private final HTML summary = new HTML();
	private final HTML description = new HTML();
	private final VerticalPanel chart = new VerticalPanel();

	private FindingTypeContent() {
		// Do nothing
	}

	protected void onActivate(Context context) {
		final String arg = context.getArgs();
		if ((arg == null) || (arg.length() == 0)) {
			setEmpty();
		} else {
			ServiceHelper.getSettingsService().getFindingTypeInfo(arg,
					new Callback() {

						protected void onFailure(String message, Object result) {
							setEmpty();
						}

						protected void onSuccess(String message, Object result) {
							setFindingType((FindingTypeInfo) result);
						}

					});
		}

	}

	protected void setEmpty() {
		name.setHTML("None");
		summary.setHTML(null);
		description.setHTML(null);
		chart.clear();
	}

	protected void setFindingType(FindingTypeInfo result) {
		name.setHTML(result.getName());
		summary.setHTML(result.getShortMessage());
		description.setHTML(result.getInfo());
		chart.add(ChartBuilder.name("FindingTypeCounts").prop("uid",
				result.getUid()).build());
	}

	protected boolean onDeactivate() {
		setEmpty();
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.add(new HTML("<h3>Finding Type</h3>"));
		panel.add(name);
		panel.add(new HTML("<h3>Summary</h3>"));
		panel.add(summary);
		panel.add(new HTML("<h3>Description</h3>"));
		panel.add(description);
		panel.add(new HTML("<h3>Found In</h3>"));
		panel.add(chart);
		getRootPanel().add(panel, DockPanel.CENTER);

	}

	public static FindingTypeContent getInstance() {
		return instance;
	}

}
