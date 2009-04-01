package com.surelogic.sierra.client.eclipse;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.tool.message.Importance;

public final class Utility {

	private Utility() {
		// no instances
	}

	public static Image getImageFor(Importance importance) {
		final String imageName;
		if (importance == Importance.IRRELEVANT)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_0;
		else if (importance == Importance.LOW)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_25;
		else if (importance == Importance.MEDIUM)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_50;
		else if (importance == Importance.HIGH)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_75;
		else
			imageName = CommonImages.IMG_ASTERISK_ORANGE_100;
		return SLImages.getImage(imageName);
	}
}
