package com.surelogic.sierra.gwt.client.ui.chart;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;

public final class MappedImage extends Image {

	public MappedImage() {
		super();
	}

	public MappedImage(String url, String map) {
		super(url);
		setMap(map);
	}

	public MappedImage(String url, String map, int left, int top, int width,
			int height) {
		super(url, left, top, width, height);
		setMap(map);
	}

	public String getMap() {
		return DOM.getElementAttribute(getElement(), "usemap");
	}

	public void setMap(String map) {
		DOM.setElementAttribute(getElement(), "usemap", map);
	}
}
