package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.serviceability.scan.SierraScanCrashReport;
import com.surelogic.common.ui.DialogTouchNotificationUI;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.actions.MarkersHandler;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.DeleteUnfinishedScans;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.client.eclipse.views.SierraServersAutoSync;
import com.surelogic.sierra.client.eclipse.views.adhoc.SierraDataSource;

/**
 * The activator class controls the plug-in life cycle
 */
public final class Activator extends AbstractUIPlugin implements IRunnableWithProgress {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.surelogic.sierra.client.eclipse";

  public static final String XML_ENCODING = "UTF-8";

  // The shared instance
  private static Activator f_plugin;

  /**
   * Indicates that the schema versions in the code and in the database are in
   * sync.
   */
  private static final AtomicBoolean f_databaseInSync = new AtomicBoolean(true);

  /**
   * The constructor
   */
  public Activator() {
    if (f_plugin != null) {
      throw new IllegalStateException(I18N.err(1, PLUGIN_ID, Activator.class.getName()));
    }
    f_plugin = this;
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);

    EclipseUIUtility.startup(this);
  }

  // Used for startup
  @Override
  public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    monitor.beginTask("Initializing the Sierra tool", 11);

    /*
     * "Touch" common-core-eclipse so the logging gets Eclipse-ified.
     */
    SLEclipseStatusUtility.touch(new DialogTouchNotificationUI());
    monitor.worked(1);

    /*
     * "Touch" the JSure preference initialization.
     */
    SierraPreferencesUtility.initializeDefaultScope();
    monitor.worked(1);

    /*
     * Set the scan crash reporter to an Eclipse implementation.
     */
    SierraScanCrashReport.getInstance().setReporter(EclipseScanCrashReporter.getInstance());
    monitor.worked(1);

    /*
     * Setup the tool directories.
     */
    Tools.initializeToolDirectories();
    monitor.worked(1);

    try {
      // startup the database and ensure its schema is up to date
      Data.getInstance().bootAndCheckSchema();
      monitor.worked(1);

      // load up persisted sierra servers
      ConnectedServerManager.getInstance().init();
      monitor.worked(1);

      // load up persisted sierra selections
      SelectionManager.getInstance().load(getSelectionSaveFile());
      monitor.worked(1);

      // start observing data changes

      // listen changes to the active editor and preference listener
      final MarkersHandler handler = MarkersHandler.getInstance();
      handler.addMarkerListener();
      EclipseUIUtility.getPreferences().addPropertyChangeListener(handler);
      monitor.worked(1);

      new AbstractSierraDatabaseJob("Initializing model") {

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
          Projects.getInstance().init();
          BuglinkData.getInstance().init();
          return Status.OK_STATUS;
        }
      }.schedule();

      new AbstractSierraDatabaseJob("Checking for new artifact types.") {

        @Override
        protected IStatus run(final IProgressMonitor monitor) {

          Tools.checkForNewArtifactTypes();
          return Status.OK_STATUS;
        }
      }.schedule();

      EclipseUtility.getProductReleaseDateJob(SLLicenseProduct.SIERRA, this).schedule();
      monitor.worked(1);

      new DeleteUnfinishedScans().schedule();
      monitor.worked(1);

      SierraServersAutoSync.start();
      monitor.worked(1);

    } catch (final FutureDatabaseException e) {
      /*
       * The database schema version is too new, we need to delete it to run
       * with this version of the code.
       * 
       * This could occur if the user reverted to a previously installed version
       * of Sierra. (RFR requirement 3.1.15)
       */
      f_databaseInSync.set(false);
      EclipseUtility.setBooleanPreference(SierraPreferencesUtility.DELETE_DB_ON_STARTUP, true);
      final int errNo = 37;
      final String msg = I18N.err(errNo, e.getSchemaVersion(), e.getCodeVersion());
      final IStatus reason = SLEclipseStatusUtility.createWarningStatus(errNo, msg, e);
      ErrorDialogUtility.open(null, null, reason);
      final UIJob restartEclipseJob = new SLUIJob() {
        @Override
        public IStatus runInUIThread(final IProgressMonitor monitor) {
          PlatformUI.getWorkbench().restart();
          return Status.OK_STATUS;
        }
      };
      restartEclipseJob.schedule();
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, "Failure to boot and check schema.", e);
    }

    if (SystemUtils.IS_JAVA_1_6) {
      final int errNo = 61;
      final String msg = I18N.err(errNo, System.getProperty("java.version"), System.getProperty("java.vendor"));
      final IStatus reason = SLEclipseStatusUtility.createWarningStatus(errNo, msg);
      ErrorDialogUtility.open(null, null, reason);
    }
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    try {
      SierraServersAutoSync.stop();
      SierraDataSource.getInstance().dispose();
      Projects.getInstance().dispose();
      BuglinkData.getInstance().dispose();
      if (f_databaseInSync.get()) {
        ConnectedServerManager.getInstance().dispose();
        SelectionManager.getInstance().save(getSelectionSaveFile());
      }
      f_plugin = null;
    } finally {
      super.stop(context);
    }
  }

  /**
   * Returns the shared instance.
   * 
   * @return the shared instance.
   */
  public static Activator getDefault() {
    return f_plugin;
  }

  public File getSelectionSaveFile() {
    return new File(SierraPreferencesUtility.getSierraDataDirectory(), "selections.xml");
  }

  public File getFindingDetailsViewSaveFile() {
    return new File(SierraPreferencesUtility.getSierraDataDirectory(), "finding-details-view.xml");
  }
}
