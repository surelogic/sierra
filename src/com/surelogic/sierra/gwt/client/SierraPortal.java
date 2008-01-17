package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// build project panel
		final VerticalPanel projectPanel = new VerticalPanel();
		final ListBox projectList = new ListBox(true);
		final Button loadButton = new Button("Graph Projects");
		projectPanel.add(projectList);
		projectPanel.add(loadButton);

		// build the chart panel
		final Image chartImage = new Image();

		// populate the project list
		projectList.addItem("sierra-builder");
		projectList.addItem("sierra-client-eclipse");
		projectList.addItem("sierra-jdbc");
		projectList.addItem("sierra-tool");

		// hook events
		loadButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				String url = buildChartUrl(projectList);
				chartImage.setTitle("Loading Chart...");
				chartImage.setUrl(url);
			}
		});

		chartImage.addLoadListener(new LoadListener() {
			public void onError(Widget sender) {
				// TODO show an error image
			}

			public void onLoad(Widget sender) {
				chartImage.setTitle("Project Chart");
			}
		});

		// add our UI to the root panel
		RootPanel.get("slot1").add(projectPanel);
		RootPanel.get("slot2").add(chartImage);
	}

	private String buildChartUrl(ListBox projectList) {
		// TODO build the chart URL
		return "http://chart.apis.google.com/chart?cht=p3&chd=s:hW&chs=250x100&chl=Hello|World";
	}
}
