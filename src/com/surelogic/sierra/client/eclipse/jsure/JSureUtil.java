package com.surelogic.sierra.client.eclipse.jsure;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.tool.message.AssuranceType;

public class JSureUtil {
	public static final String MODEL_CATEGORY_ID = "00000006-ef51-4f9c-92f6-351d214f46e7";
	public static final String PROMISE_CATEGORY_ID = "00000005-ef51-4f9c-92f6-351d214f46e7";
	
	public static boolean isModelType(String id) {
        return BuglinkData.getInstance().getFindingTypes(MODEL_CATEGORY_ID).contains(id);				
	}
	
	public static boolean isPromiseType(String id) {
        return BuglinkData.getInstance().getFindingTypes(PROMISE_CATEGORY_ID).contains(id);				
	}
	
	public static Image getImageFor(AssuranceType type) {
		if (type == null) {
			return null;
		}
		switch (type) {
		case CONSISTENT:
			return SLImages.getImage(CommonImages.IMG_PLUS);			
		case INCONSISTENT:
			return SLImages.getImage(CommonImages.IMG_RED_X);
		default:
			return null;
		}
	}
	
	public static Image getImageFor(String findingTypeId, AssuranceType type) {
		if (type == null) {
			return null;
		}
		final boolean isPromise = isPromiseType(findingTypeId);
		switch (type) {
		case CONSISTENT:
			return SLImages.getImage(isPromise ? CommonImages.IMG_PROMISE_CONSISTENT :
				                                 CommonImages.IMG_PLUS);			
		case INCONSISTENT:
			return SLImages.getImage(isPromise ? CommonImages.IMG_PROMISE_INCONSISTENT :
				                                 CommonImages.IMG_RED_X);
		default:
			return null;
		}
	}
}
