package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.dialogs.ConfirmPerspectiveSwitchDialog;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.perspectives.CodeReviewPerspective;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class ConfirmPerspectiveSwitch {

	private static final AtomicBoolean f_dialogOpen = new AtomicBoolean(false);

	public static void submitUIJob() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				/*
				 * Ensure that we don't already have this dialog up.
				 */
				if (f_dialogOpen.compareAndSet(false, true)) {
					try {
						/*
						 * Now prompt the user to change to the Code Review
						 * perspective, if we are not already in it.
						 */
						final boolean inCodeReviewPerspective = ViewUtility
								.isPerspectiveOpen(CodeReviewPerspective.class
										.getName());
						if (!inCodeReviewPerspective) {
							final boolean change = ConfirmPerspectiveSwitch
									.toCodeReview(SWTUtility.getShell());
							if (change) {
								ViewUtility
										.showPerspective(CodeReviewPerspective.class
												.getName());
							}
						}
					} finally {
						/*
						 * The dialog is no longer being shown.
						 */
						f_dialogOpen.set(false);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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
		if (PreferenceConstants.getPromptForPerspectiveSwitch()) {
			ConfirmPerspectiveSwitchDialog dialog = new ConfirmPerspectiveSwitchDialog(
					shell, SLImages.getImage(CommonImages.IMG_SIERRA_LOGO),
					I18N.msg("sierra.dialog.confirm.perspective.switch"));
			final boolean result = dialog.open() == Window.OK;
			final boolean rememberMyDecision = dialog.getRememberMyDecision();
			if (rememberMyDecision) {
				PreferenceConstants
						.setPromptForPerspectiveSwitch(!rememberMyDecision);
				PreferenceConstants.setAutoPerspectiveSwitch(result);
			}
			return result;
		} else {
			return PreferenceConstants.getAutoPerspectiveSwitch();
		}
	}
}
