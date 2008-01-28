package com.surelogic.sierra.client.eclipse.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.sierra.client.eclipse.dialogs.QualifierSelectionDialog;

/**
 * Used to prompt the user from a running job for a set of qualifiers.
 */
public final class QualifierPromptFromJob {

	private final Set<String> f_qualifiers;
	private final Set<String> f_selectedQualifiers = new HashSet<String>();
	private final String f_projectName;
	private final String f_serverLabel;

	/**
	 * Constructs this object.
	 * 
	 * @param server
	 *            the mutable server configuration to be fixed.
	 */
	public QualifierPromptFromJob(Set<String> qualifiers, String projectName,
			String serverLabel) {
		if (qualifiers == null)
			throw new IllegalArgumentException("Qualifier set must be non-null");
		f_qualifiers = new HashSet<String>(qualifiers);
		if (projectName == null)
			throw new IllegalArgumentException("Project name must be non-null");
		f_projectName = projectName;
		if (serverLabel == null)
			throw new IllegalArgumentException("Server label must be non-null");
		f_serverLabel = serverLabel;
	}

	public void open() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (f_qualifiers.isEmpty()) {
					final StringBuilder b = new StringBuilder();
					b.append("The requested action failed because");
					b.append(" the Sierra team server '");
					b.append(f_serverLabel);
					b.append("' has no qualifiers defined.\n\n");
					b.append("Possible resolutions for this");
					b.append(" problem include:\n");
					b.append(" \u25CF Login to the Sierra team server '");
					b.append(" \u25CF Login to the Sierra team server '");
					b.append(f_serverLabel);
					b.append("' and create a qualifier.");
					final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							"No Qualifiers on Sierra Team Server", null, b
									.toString(), null, Activator.getDefault());
					report.open();
					f_canceled = true; // bail out
				} else {
					QualifierSelectionDialog dialog = new QualifierSelectionDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							f_qualifiers, f_projectName, f_serverLabel);
					if (dialog.open() != Window.CANCEL) {
						f_selectedQualifiers.addAll(dialog
								.getSelectedQualifiers());
						f_useForAllOnSameServer = dialog
								.useForAllOnSameServer();
					} else {
						f_canceled = true;
					}
				}
			}
		});
	}

	/**
	 * Gets the set of qualifiers selected by the user.
	 * <p>
	 * The results of this method are only valid after a call to {@link #open()}
	 * when {@link #isCanceled()} is <code>false</code>.
	 * 
	 * @return
	 */
	public Set<String> getSelectedQualifiers() {
		return new HashSet<String>(f_selectedQualifiers);
	}

	private boolean f_canceled = false;

	/**
	 * Indicates that the user didn't try to fix the server location and
	 * authentication data.
	 * 
	 * @return <code>false</code> if the user fixed the server location and
	 *         authentication data, <code>false</code> otherwise.
	 */
	public boolean isCanceled() {
		return f_canceled;
	}

	private boolean f_useForAllOnSameServer = false;

	/**
	 * Indicates that the resulting set of qualifiers should be used for all
	 * other projects sharing runs to the same Sierra server.
	 * <p>
	 * The results of this method are only valid after a call to {@link #open()}
	 * when {@link #isCanceled()} is <code>false</code>.
	 * 
	 * @return <code>true</code> if the qualifiers should be used for all
	 *         other projects sharing runs to the same Sierra server,
	 *         <code>false</code> otherwise.
	 */
	public boolean useForAllOnSameServer() {
		return f_useForAllOnSameServer;
	}
}
