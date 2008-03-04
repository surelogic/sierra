package com.surelogic.sierra.gwt.client.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Image;

public class ChartBuilder {

	private final Map map;
	private int height;
	private int width;

	private ChartBuilder() {
		map = new HashMap();
	}

	public ChartBuilder width(int width) {
		this.width = width;
		map.put("width", Integer.toString(width));
		return this;
	}

	public ChartBuilder height(int height) {
		this.height = height;
		map.put("height", Integer.toString(height));
		return this;
	}

	public ChartBuilder prop(String prop, String value) {
		map.put(URL.encode(prop), URL.encode(value));
		return this;
	}

	public Image build() {
		final Image image = new Image(GWT.getModuleBaseURL() + "chart/use"
				+ getArgs());
		if (height > 0) {
			image.setHeight(height + "px");
		}
		if (width > 0) {
			image.setWidth(width + "px");
		}
		return image;
	}

	public static ChartBuilder name(String name) {
		return new ChartBuilder().prop("name", name);
	}

	private String getArgs() {
		StringBuffer b = new StringBuffer();
		b.append("?");
		for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
			final Entry e = (Entry) i.next();
			final String prop = (String) e.getKey();
			final String val = (String) e.getValue();
			b.append(prop);
			b.append("=");
			b.append(val);
			b.append("&");
		}
		return b.toString();
	}
}
