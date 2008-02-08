package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

public final class ImageHelper {
	public static final String IMAGE_BASE_URL = GWT.getModuleBaseURL()
			+ "images/";

	public static Image getWaitImage(int size) {
		int adjustedSize;
		if (size <= 16) {
			adjustedSize = 16;
		} else if (size > 16 && size <= 24) {
			adjustedSize = 24;
		} else {
			adjustedSize = 32;
		}
		final StringBuffer filename = new StringBuffer();
		filename.append("wait-");
		filename.append(adjustedSize).append("x").append(adjustedSize);
		filename.append(".gif");
		return new Image(IMAGE_BASE_URL + filename.toString());
	}

	private ImageHelper() {
		// Not instantiable
	}
}
