package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class ImportSierraScanAction implements IWorkbenchWindowActionDelegate {

    @Override
    public void run(IAction action) {
        Shell shell = EclipseUIUtility.getShell();
        FileDialog fd = new FileDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        fd.setText("Import Scan");
        fd.setFilterExtensions(new String[] { "*.sierra.gz", "*.sierra", "*.*" });
        fd.setFilterNames(new String[] { "Scan Documents (*.sierra)",
                "Compressed Scan Documents (*.sierra.gz)", "All Files (*.*)" });
        String fileName = fd.open();
        if (fileName != null) {
            File f = new File(fileName);
            String projectName = getProjectName(f);
            if (projectName != null && f.exists() && !f.isDirectory()) {
                File to = new File(
                        SierraPreferencesUtility.getSierraScanDirectory(),
                        f.getName());
                if (!f.equals(to)) {
                    FileUtility.copy(f, to);
                }
                final Runnable runAfterImport = new Runnable() {
                    @Override
                    public void run() {
                        /* Notify that scan was completed */
                        DatabaseHub.getInstance().notifyScanLoaded();
                    }
                };
                ImportScanDocumentJob job = new ImportScanDocumentJob(f,
                        projectName, runAfterImport);
                job.schedule();
            } else {
                MessageDialog.openError(
                        shell,
                        I18N.msg("sierra.dialog.importScan.error.title"),
                        I18N.msg("sierra.dialog.importScan.error.msg",
                                f.getAbsolutePath()));
            }
        }
    }

    private String getProjectName(File f) {
        StringBuilder projectName = new StringBuilder();
        try {
            InputStream in = new FileInputStream(f);
            if (f.getName().endsWith(FileUtility.GZIP_SUFFIX)) {
                in = new GZIPInputStream(in);
            }
            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(in);
            while (reader.getEventType() != XMLStreamConstants.START_ELEMENT
                    || !reader.getLocalName().equals("project")) {
                reader.next();
            }
            reader.next();
            while (reader.getEventType() == XMLStreamConstants.CHARACTERS) {
                projectName.append(reader.getText());
                reader.next();
            }
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (XMLStreamException e) {
            SLLogger.getLoggerFor(ImportSierraScanAction.class).log(
                    Level.WARNING, "Error reading scan document", e);
        } catch (FactoryConfigurationError e) {
            SLLogger.getLoggerFor(ImportSierraScanAction.class).log(
                    Level.WARNING, "Error reading scan document", e);
        } catch (IOException e) {
            SLLogger.getLoggerFor(ImportSierraScanAction.class).log(
                    Level.WARNING, "Error reading scan document", e);
        }
        if (projectName.length() == 0) {
            return null;
        } else {
            return projectName.toString();
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // Nothing to do
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // Nothing to do
    }
}
