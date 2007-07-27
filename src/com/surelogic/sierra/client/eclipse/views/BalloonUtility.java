package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.PlatformUI;

public final class BalloonUtility {

	private static ToolTip f_lastTip = null;

	private BalloonUtility() {
		// no instances
	}

	public static void showMessage(final String text, final String message) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null)
			// Get into a UI thread!
			display.asyncExec(new Runnable() {
				public void run() {

					synchronized (BalloonUtility.class) {
						if (f_lastTip != null) {
							if (!f_lastTip.isDisposed()) {
								f_lastTip.setVisible(false);
								f_lastTip.dispose();
							}
						}
						final Shell shell = display.getActiveShell();
						if (shell == null)
							return;

						/*
						 * We need to position the balloon relative to the main
						 * Eclipse window. This puts it at the bottom-left a few
						 * pixels in diagonal up from the bottom-left corner.
						 */
						final Rectangle r = shell.getBounds();
						final int x = r.x + 20;
						final int y = r.y + r.height - 3;

						final ToolTip tip = new ToolTip(shell, SWT.BALLOON
								| SWT.ICON_INFORMATION);
						tip.setMessage(message);
						tip.setText(text);
						tip.setLocation(x, y);
						System.out.println("autohide=" + tip.getAutoHide());
						tip.setVisible(true);
						f_lastTip = tip;
					}
				}
			});
	}
}
