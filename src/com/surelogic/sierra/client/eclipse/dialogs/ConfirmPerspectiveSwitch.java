package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.dialogs.AbstractConfirmPerspectiveSwitch;
import com.surelogic.sierra.client.eclipse.perspectives.CodeReviewPerspective;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class ConfirmPerspectiveSwitch extends
		AbstractConfirmPerspectiveSwitch {

	public static final ConfirmPerspectiveSwitch prototype = new ConfirmPerspectiveSwitch();

	private ConfirmPerspectiveSwitch() {
		super(CodeReviewPerspective.class.getName(),
				PreferenceConstants.prototype);
	}

	@Override
	protected String getLogo() {
		return CommonImages.IMG_SIERRA_LOGO;
	}

	@Override
	protected String getShortPrefix() {
		return "sierra.";
	}

	/**
	 * Checks if the Code Review perspective should be opened.
	 * 
	 * @param shell
	 *            a shell.
	 * @return {@code true} if the Code Review perspective should be opened,
	 *         {@code false} otherwise.
	 */
	public static boolean toCodeReview(Shell shell) {
		return prototype.toPerspective(shell);
	}
}
