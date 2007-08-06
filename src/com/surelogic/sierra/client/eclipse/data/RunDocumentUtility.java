package com.surelogic.sierra.client.eclipse.data;

import java.io.File;

import com.surelogic.common.SLProgressMonitor;

public final class RunDocumentUtility {

	private RunDocumentUtility() {
		// no instances
	}

	/**
	 * Parses a run document into the database and generates findings. When this
	 * method is completed the run document has been fully loaded into the
	 * Sierra client and is ready to be examined via the user interface.
	 * 
	 * @param runDocument
	 *            the run document.
	 * @param monitor
	 *            a progress monitor, may be <code>null</code> if progress is
	 *            not tracked.
	 */
	public static void loadRunDocument(final File runDocument,
			final SLProgressMonitor monitor) {
	}
}
