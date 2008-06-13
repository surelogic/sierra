package com.surelogic.sierra.client.eclipse;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.tool.message.AssuranceType;
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

	public static Image getImageFor(AssuranceType type) {
		if (type == null) {
			return null;
		}
		switch (type) {
		case CONSISTENT:
			return SLImages.getImage(CommonImages.IMG_PLUS);			
		case INCONSISTENT:
			return SLImages.getImage(CommonImages.IMG_REDX);
		default:
			return null;
		}
	}
}
