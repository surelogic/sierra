package com.surelogic.sierra.tool.findbugs;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.targets.*;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

public abstract class AbstractFindBugsTool extends AbstractTool {
  protected AbstractFindBugsTool(String version) {
    super("FindBugs", version, "FindBugs (TM)", "");
  }
  
  public IToolInstance create(final SLProgressMonitor monitor) {
    System.setProperty("findbugs.home", "C:/work/workspace/sierra-tool/Tools/FB");
    
    return new AbstractToolInstance(this, monitor) {     
      final IFindBugsEngine engine = createEngine();
      
      @Override
      protected void execute() throws Exception { 
        final Project p = createProject();
        engine.setProject(p);
        //engine.setUserPreferences(arg0); 
        //engine.setAnalysisFeatureSettings(arg0);
        engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        //engine.setClassScreener(arg0);
        
        Monitor mon = new Monitor(getProgressMonitor()); 
        //engine.addClassObserver(mon);
        engine.setBugReporter(mon);
        engine.setProgressCallback(mon);        
      }
            
      protected Project createProject() {
        final Project p = new Project();
        for(IToolTarget t : getTargets()) {
          if (t.isSource()) {
            continue;
          }
          // Only scanning binaries
          final String path = new File(t.getLocation()).getAbsolutePath(); 
          switch (t.getKind()) {
            case FILE:
            case JAR:
              p.addFile(path);
              break;
            case DIRECTORY:
              for(URI loc : t.getFiles()) {
                File f = new File(loc);
                if (f.exists()) {              
                  p.addFile(f.getAbsolutePath());
                }
              }
              break;
          }
        }
        return p;
      }
    };
  }
  
  protected abstract IFindBugsEngine createEngine();
  
  class Monitor implements FindBugsProgress, BugReporter {
    final SLProgressMonitor monitor;

    public Monitor(SLProgressMonitor mon) {
      monitor = mon;
    }

    /* ******************** For FindBugsProgress ********************* */
    public void startAnalysis(int numClasses) {
      System.out.println("startAnalysis: "+numClasses);
      monitor.beginTask("FindBugs scanning ...", numClasses);
    }
    
    public void finishArchive() {
      System.out.println("Finished an archive");
    }

    public void finishClass() {
      System.out.println("Finished a class");
      monitor.internalWorked(1.0);
    }

    public void finishPerClassAnalysis() {
      System.out.println("finishPerClassAnalysis");
    }

    public void predictPassCount(int[] classesPerPass) {
      int i = 1;
      for(int count : classesPerPass) {
        System.out.println("Pass\t"+i+": "+count);
        i++;
      }
    }

    public void reportNumberOfArchives(int numArchives) {
      System.out.println("# Archives: "+numArchives);
    }

    /* ******************** For BugReporter ********************* */
    
    public void finish() {
      System.out.println("Finished");
    }    
    
    public void addObserver(BugReporterObserver observer) {
      // FIX what to do?
      // throw new UnsupportedOperationException();
    }

    public ProjectStats getProjectStats() {
      throw new UnsupportedOperationException();
    }

    public BugReporter getRealBugReporter() {
      throw new UnsupportedOperationException();
    }

    public void reportBug(BugInstance bug) {
      System.out.println("Bug reported: "+bug.getAbridgedMessage());
    }

    public void reportQueuedErrors() {
    }

    public void setErrorVerbosity(int level) {
    }

    public void setPriorityThreshold(int threshold) {
    }

    /* ******************** For IErrorLogger ********************* */
    
    public void logError(String message) {
      LOG.severe(message);
    }

    public void logError(String message, Throwable e) {
      LOG.log(Level.SEVERE, message, e);
    }

    public void reportMissingClass(ClassNotFoundException ex) {
      LOG.log(Level.WARNING, "Missing class", ex);
    }

    public void reportMissingClass(ClassDescriptor desc) {
      LOG.warning("Missing class: "+desc.getClassName());
    }

    public void reportSkippedAnalysis(MethodDescriptor method) {
      LOG.info("Skipped analysis: "+method.getSignature());
    }

    /* ******************** For IClassObserver ********************* */
    
    public void observeClass(ClassDescriptor desc) {
      monitor.subTask("Scanning "+desc.getDottedClassName());
    }
  }
}
