package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.jdt.JavaUtil;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Config;

public class NewScan extends AbstractScan<IJavaProject> {
	NewScan() {
		super(false);
	}

	@Override
	boolean checkIfBuilt(Collection<IJavaProject> elements) {
		return JavaUtil.projectsUpToDate(elements);
	}

	/**
	 * Starts a job for each project
	 */
	@Override
	void startScanJob(Collection<IJavaProject> selectedProjects) {
		LOG.info("Starting new scan jobs");
		for (final IJavaProject p : selectedProjects) {
			final Config config = ConfigGenerator.getInstance()
					.getProjectConfig(p);
			if (config.hasNothingToScan()) {
				return;
			}
			final Runnable runAfterImport = new Runnable() {
				public void run() {
					/* Notify that scan was completed */
					DatabaseHub.getInstance().notifyScanLoaded();

					/* Rename the scan document */
					File scanDocument = config.getScanDocument();
					File newScanDocument = null;

					newScanDocument = new File(FileUtility
							.getSierraDataDirectory()
							+ File.separator
							+ config.getProject()
							+ SierraToolConstants.PARSED_FILE_SUFFIX);
					/*
					 * This approach assures that the scan document generation
					 * will not crash. The tool will simply override the
					 * existing scan document no matter how recent it is.
					 */
					if (newScanDocument.exists()) {
						newScanDocument.delete();
					}
					scanDocument.renameTo(newScanDocument);
				}
			};
			DatabaseJob importJob = new ImportScanDocumentJob(config
					.getScanDocument(), config.getProject(), runAfterImport);
			importJob.addJobChangeListener(new ScanJobAdapter(config
					.getProject()));

			Job job = new NewScanJob("Running Sierra on " + p.getElementName(),
					config, importJob);
			job.schedule();
		}
	}
}
