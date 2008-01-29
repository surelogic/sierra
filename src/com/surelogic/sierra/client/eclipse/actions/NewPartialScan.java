package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.ConfigCompilationUnit;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public class NewPartialScan extends AbstractScan<ICompilationUnit> {
  NewPartialScan() {
    super(true);
  }

  @Override
  boolean checkIfBuilt(List<ICompilationUnit> elements) {
    // TODO Auto-generated method stub
    return true;
  }
  
  void scan(List<ICompilationUnit> selectedCompilationUnits) {
    List<String> cuNames = new ArrayList<String>(selectedCompilationUnits.size());
    for(ICompilationUnit cu : selectedCompilationUnits) {
      cuNames.add(cu.getElementName());
    }
    scan(selectedCompilationUnits, cuNames);
  }

  @Override
  void startScanJob(List<ICompilationUnit> selectedCUs) {
    // One per project that had a CU selected
    final List<ConfigCompilationUnit> configs = 
      ConfigGenerator.getInstance().getCompilationUnitConfigs(selectedCUs);
        
    for(final ConfigCompilationUnit ccu : configs) {
      DatabaseJob importJob = new ImportPartialScanDocumentJob(ccu);      
      Job job = new NewScanJob("Running Sierra on comp units in " + ccu.getConfig().getProject(),
                               ccu.getConfig(), importJob);
      job.schedule();
    }
  }
  
  static class ImportPartialScanDocumentJob extends DatabaseJob {
    final ConfigCompilationUnit config;
    public ImportPartialScanDocumentJob(ConfigCompilationUnit ccu) {
      super("Loading partial scan document for "+ccu.getConfig().getProject());
      config = ccu;
    }
    @Override
    protected IStatus run(IProgressMonitor monitor) {
      final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
      ScanDocumentUtility.loadPartialScanDocument(config.getConfig().getScanDocument(),
                                                  wrapper, config.getConfig().getProject(),
                                                  config.getPackageCompilationUnitMap());
      
      /* Notify that scan was completed */
      DatabaseHub.getInstance().notifyScanLoaded();  
      
      if (wrapper.isCanceled()) {
        return Status.CANCEL_STATUS;
      } else {
        return Status.OK_STATUS;
      }
    }   
  }
}
