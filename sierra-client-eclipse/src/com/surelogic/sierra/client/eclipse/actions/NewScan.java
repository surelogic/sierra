package com.surelogic.sierra.client.eclipse.actions;

import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_ZIP_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.USE_ZIP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.ui.BalloonUtility;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Config;

public class NewScan extends AbstractScan<IJavaProject> {
    public NewScan() {
        super(false);
    }

    @Override
    boolean checkIfBuilt(Collection<IJavaProject> elements) {
        return JDTUtility.projectsUpToDate(elements);
    }

    /**
     * Starts a job for each project
     */
    @Override
    boolean startScanJob(Collection<IJavaProject> selectedProjects) {
        boolean started = false;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Starting new scan jobs");
        }

        List<Config> configs = new ArrayList<Config>();
        for (final IJavaProject p : selectedProjects) {
            final Config config = ConfigGenerator.getInstance()
                    .getProjectConfig(p);
            configs.add(config);
        }
        boolean continueScan = setupConfigs(configs);
        if (!continueScan) {
            return false;
        }

        for (final Config config : configs) {
            if (config.hasNothingToScan()) {
                BalloonUtility.showMessage(
                        "Nothing to scan",
                        "There are no source files to scan in "
                                + config.getProject());
            } else {
                started = true;
            }
            final Runnable runAfterImport = new Runnable() {
                @Override
                public void run() {
                    /* Notify that scan was completed */
                    DatabaseHub.getInstance().notifyScanLoaded();

                    /* Rename the scan document */
                    File scanDocument = config.getScanDocument();
                    File newScanDocument = generateScanDocumentFile(
                            config.getProject(), scanDocument.getName());
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
            AbstractSierraDatabaseJob importJob = new ImportScanDocumentJob(
                    config.getScanDocument(), config.getProject(),
                    runAfterImport);
            importJob.addJobChangeListener(new ScanJobAdapter(config
                    .getProject()));

            Job job = new NewScanJob(
                    "Running Sierra on " + config.getProject(), config,
                    importJob);
            job.schedule();
        }
        return started;
    }

    private static File generateScanDocumentFile(String project, String name) {
        for (String suffix : SierraToolConstants.PARSED_FILE_SUFFIXES) {
            if (name.endsWith(suffix)) {
                return new File(
                        SierraPreferencesUtility.getSierraScanDirectory(),
                        project + suffix);
            }
        }
        return new File(SierraPreferencesUtility.getSierraScanDirectory(),
                project
                        + (USE_ZIP ? PARSED_ZIP_FILE_SUFFIX
                                : PARSED_FILE_SUFFIX));
    }

    public static File findScanDocumentFile(String projectName) {
        for (String suffix : SierraToolConstants.PARSED_FILE_SUFFIXES) {
            String scanFileName = projectName + suffix;
            File scanFile = new File(
                    SierraPreferencesUtility.getSierraScanDirectory(),
                    scanFileName);
            if (scanFile.exists()) {
                return scanFile;
            }
        }
        return null;
    }
}
