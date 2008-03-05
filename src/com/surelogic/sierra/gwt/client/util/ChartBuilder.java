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
	private final String name;
	private int height;
	private int width;

	private ChartBuilder(String name) {
		map = new HashMap();
		this.name = name;
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
		final Image image = new Image(GWT.getModuleBaseURL() + "chart/" + name
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
		return new ChartBuilder(name);
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
