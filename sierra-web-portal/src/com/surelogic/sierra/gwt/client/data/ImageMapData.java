package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ImageMapData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1403278001490657118L;
	private String data;

	public ImageMapData() {
		// Do nothing
	}

	public ImageMapData(String string) {
		this.data = string;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
