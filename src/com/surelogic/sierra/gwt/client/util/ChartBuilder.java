package com.surelogic.sierra.gwt.client.util;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

public class ChartBuilder {

	private final Map parameters;
	private final String name;
	private int height;
	private int width;

	private ChartBuilder(String name) {
		parameters = new HashMap();
		this.name = name;
	}

	public ChartBuilder width(int width) {
		this.width = width;
		parameters.put("width", Integer.toString(width));
		return this;
	}

	public ChartBuilder height(int height) {
		this.height = height;
		parameters.put("height", Integer.toString(height));
		return this;
	}

	public ChartBuilder prop(String prop, String value) {
		parameters.put(prop, value);
		return this;
	}

	public Image build() {
		UrlHelper.setUniqueRequestId(parameters);
		final String url = UrlHelper.appendParameters(GWT.getModuleBaseURL()
				+ "chart/" + name, parameters);
		final Image image = new Image(url);
		if (height > 0) {
			image.setHeight(height + "px");
		}
		if (width > 0) {
			image.setWidth(width + "px");
		}
		return image;
	}

	public static ChartBuilder name(String name) {
		return new ChartBuilder(name);
	}

}
