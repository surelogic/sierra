package com.surelogic.sierra.client.eclipse.actions;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;

import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.ConfigCompilationUnit;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.tool.message.Config;

public class NewPartialScan extends AbstractScan<ICompilationUnit> {
	NewPartialScan() {
		super(true);
	}

	@Override
	boolean checkIfBuilt(Collection<ICompilationUnit> elements) {
		return JDTUtility.compUnitsUpToDate(elements);
	}

	@Override
	boolean startScanJob(Collection<ICompilationUnit> selectedCUs) {
		// One per project that had a CU selected
		final List<ConfigCompilationUnit> configs = ConfigGenerator
				.getInstance().getCompilationUnitConfigs(selectedCUs);
		setupCUConfigs(configs);

		for (final ConfigCompilationUnit ccu : configs) {
			final Config config = ccu.getConfig();
			AbstractSierraDatabaseJob importJob = new ImportPartialScanDocumentJob(ccu);
			importJob.addJobChangeListener(new ScanJobAdapter(config
					.getProject(), true));

			Job job = new NewScanJob("Running Sierra on comp units in "
					+ config.getProject(), config, importJob);
			job.schedule();
		}
		return true;
	}

	static class ImportPartialScanDocumentJob extends AbstractSierraDatabaseJob {
		final ConfigCompilationUnit config;

		public ImportPartialScanDocumentJob(ConfigCompilationUnit ccu) {
			super("Loading partial scan document for "
					+ ccu.getConfig().getProject());
			setPriority(Job.DECORATE);			
			config = ccu;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(
					monitor, "Load scan document");
			try {
				loadScanDocument(wrapper);
			} catch (IllegalStateException e) {
				if (e.getCause() instanceof SQLException
						&& e.getMessage().contains("No current connection")) {
					// Try again and see if we can get through
					loadScanDocument(wrapper);
				}
			}
			/* Notify that scan was completed */
			DatabaseHub.getInstance().notifyScanLoaded();

			if (wrapper.isCanceled()) {
				return Status.CANCEL_STATUS;
			} else {
				return Status.OK_STATUS;
			}
		}

		private void loadScanDocument(final SLProgressMonitor wrapper) {
			ScanDocumentUtility.loadPartialScanDocument(config.getConfig()
					.getScanDocument(), wrapper, config.getConfig()
					.getProject(), config.getPackageCompilationUnitMap());
			// Delete partial scan when done
			config.getConfig().getScanDocument().delete();
		}
	}
}
