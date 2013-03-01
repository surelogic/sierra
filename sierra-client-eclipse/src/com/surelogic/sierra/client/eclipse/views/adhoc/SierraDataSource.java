package com.surelogic.sierra.client.eclipse.views.adhoc;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.IAdHocDataSource;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.adhoc.IQueryResultCustomDisplay;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public final class SierraDataSource extends AdHocManagerAdapter implements IAdHocDataSource {

  private static final SierraDataSource INSTANCE = new SierraDataSource();

  private SierraDataSource() {
    // Do nothing
  }

  static {
    INSTANCE.init();
  }

  public static SierraDataSource getInstance() {
    return INSTANCE;
  }

  public void init() {
    getManager().addObserver(this);
    getManager().setGlobalVariableValue(AdHocManager.DATABASE, "Sierra Client Database");
  }

  public void dispose() {
    getManager().removeObserver(this);
    AdHocManager.shutdown();
  }

  public DBConnection getDB() {
    return Data.getInstance();
  }

  public int getMaxRowsPerQuery() {
    return EclipseUtility.getIntPreference(SierraPreferencesUtility.FINDINGS_LIST_LIMIT);
  }

  public File getQuerySaveFile() {
    final IPath pluginState = Activator.getDefault().getStateLocation();
    return new File(pluginState.toOSString() + System.getProperty("file.separator") + "queries.xml");
  }

  public void badQuerySaveFileNotification(final Exception e) {
    SLLogger.getLogger().log(Level.SEVERE, I18N.err(4, getQuerySaveFile().getAbsolutePath()), e);
  }

  public URL getDefaultQueryUrl() {
    return Thread.currentThread().getContextClassLoader().getResource("/com/surelogic/sierra/schema/default-sierra-queries.xml");
  }

  public static AdHocManager getManager() {
    return AdHocManager.getInstance(INSTANCE);
  }

  public void notifySelectedResultChange(final AdHocQueryResult result) {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        final IViewPart view = EclipseUIUtility.showView(QueryResultsView.class.getName(), null, IWorkbenchPage.VIEW_VISIBLE);
        if (view instanceof QueryResultsView) {
          final QueryResultsView queryResultsView = (QueryResultsView) view;
          queryResultsView.displayResult(result);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  public String[] getCurrentAccessKeys() {
    return new String[] { "sierra" };
  }

  public boolean queryResultWillBeEmpty(AdHocQuery query) {
    return false;
  }

  public IQueryResultCustomDisplay getCustomDisplay(String className) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  public URL getQuerydocImageURL(String imageName) {
    return CommonImages.getImageURL(imageName);
  }

  @NonNull
  public String getQueryEditorViewId() {
    return QueryEditorView.class.getName();
  }

  @NonNull
  public String getQueryResultsViewId() {
    return QueryResultsView.class.getName();
  }

  @NonNull
  public String getQueryDocViewId() {
    return QuerydocView.class.getName();
  }
}
