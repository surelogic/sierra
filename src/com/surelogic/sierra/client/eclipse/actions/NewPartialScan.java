package com.surelogic.sierra.client.eclipse.actions;

import java.sql.SQLException;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jdt.JavaUtil;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
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
		return JavaUtil.compUnitsUpToDate(elements);
	}

	@Override
	boolean startScanJob(Collection<ICompilationUnit> selectedCUs) {
		// One per project that had a CU selected
		final List<ConfigCompilationUnit> configs = ConfigGenerator
				.getInstance().getCompilationUnitConfigs(selectedCUs);
		setupCUConfigs(configs);

		for (final ConfigCompilationUnit ccu : configs) {
			final Config config = ccu.getConfig();
			DatabaseJob importJob = new ImportPartialScanDocumentJob(ccu);
			importJob.addJobChangeListener(new ScanJobAdapter(config
					.getProject(), true));

			Job job = new NewScanJob("Running Sierra on comp units in "
					+ config.getProject(), config, importJob);
			job.schedule();
		}
		return true;
	}

	static class ImportPartialScanDocumentJob extends DatabaseJob {
		final ConfigCompilationUnit config;

		public ImportPartialScanDocumentJob(ConfigCompilationUnit ccu) {
			super("Loading partial scan document for "
					+ ccu.getConfig().getProject());
			config = ccu;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(
					monitor);
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
		}
	}
}
