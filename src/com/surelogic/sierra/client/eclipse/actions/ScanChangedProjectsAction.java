package com.surelogic.sierra.client.eclipse.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.*;

import com.surelogic.common.eclipse.jdt.JavaUtil;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.scan.*;

/**
 * Scan the changes in the selected projects
 * @author Edwin.Chan
 */
public class ScanChangedProjectsAction extends AbstractProjectSelectedMenuAction {
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  
  @Override
  protected void run(final List<IJavaProject> projects,
                     final List<String> projectNames) {
    if (projects.size() <= 0) {
      return;
    }
    new DatabaseJob("Checking last scan times") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          Connection conn = Data.readOnlyConnection();
          Map<IJavaProject,Date> times = new HashMap<IJavaProject,Date>(projects.size()); 
          for(IJavaProject p : projects) {
            ScanInfo info = ScanManager.getInstance(conn).getLatestScanInfo(p.getElementName());
            times.put(p, info.getScanTime());
          }
          List<ICompilationUnit> selectedCompilationUnits = JavaUtil.modifiedCompUnits(times);
          if (selectedCompilationUnits.size() > 0) {
            new NewPartialScan().scan(selectedCompilationUnits);
          }
        } catch (SQLException ex) {
          LOG.log(Level.SEVERE, ex.getMessage(), ex);
          return SLStatus.createErrorStatus("Failed "+getName(), ex);
        }
        return Status.OK_STATUS;
      }      
    }.schedule();
  }
}
