package com.surelogic.sierra.tool.findbugs;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.HashGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.SourceLocationBuilder;
import com.surelogic.sierra.tool.targets.*;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.config.UserPreferences;

public abstract class AbstractFindBugsTool extends AbstractTool {
  protected AbstractFindBugsTool(String version) {
    super("FindBugs", version, "FindBugs (TM)", "");
  }
  
  public IToolInstance create(final ArtifactGenerator generator, final SLProgressMonitor monitor) {
    System.setProperty("findbugs.home", "C:/work/workspace/sierra-tool/Tools/FB");
    
    return new AbstractToolInstance(this, generator, monitor) {     
      final IFindBugsEngine engine = createEngine();
      
      @Override
      protected void execute() throws Exception { 
        final Project p = createProject();
        engine.setProject(p);
        engine.setUserPreferences(UserPreferences.getUserPreferences()); 
        //engine.setAnalysisFeatureSettings(arg0);
        engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        //engine.setClassScreener(arg0);
        
        Monitor mon = new Monitor(this); 
        //engine.addClassObserver(mon);
        engine.setBugReporter(mon);
        engine.setProgressCallback(mon);        
        
        engine.execute();
      }
            
      protected Project createProject() {
        final Project p = new Project();
        for(IToolTarget t : getBinTargets()) {
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
        for(IToolTarget t : getAuxTargets()) {
          final String path = new File(t.getLocation()).getAbsolutePath(); 
          switch (t.getKind()) {
            case DIRECTORY:
            case JAR:
              p.addAuxClasspathEntry(path);             
              break;
            case FILE:
              System.out.println("Ignored: "+path);
              break;
          }
        }
        for(IToolTarget t : getSrcTargets()) {
          final String path = new File(t.getLocation()).getAbsolutePath(); 
          switch (t.getKind()) {
          case DIRECTORY:
            p.addSourceDir(path);             
            break;
          case JAR:
          case FILE:
            System.out.println("Ignored: "+path);
            break;
          }
        }
        return p;
      }
    };
  }
  
  protected abstract IFindBugsEngine createEngine();
  
  class Monitor implements FindBugsProgress, BugReporter {
    final AbstractToolInstance tool;
    final ArtifactGenerator generator;
    final SLProgressMonitor monitor;
    final ProjectStats stats = new ProjectStats();
    final Set<String> missingClasses = new HashSet<String>();

    public Monitor(AbstractToolInstance ti) {
      tool = ti;
      generator = ti.getGenerator();
      monitor = ti.getProgressMonitor();
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
      monitor.worked(1);
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
      return stats;
    }

    public BugReporter getRealBugReporter() {
      return this;
    }

    public void reportBug(BugInstance bug) {
      System.out.println("Bug reported: "+bug.getAbridgedMessage());
      stats.addBug(bug);
      
      final SourceLineAnnotation line = bug.getPrimarySourceLineAnnotation();
      final String path = computeSourceFilePath(line.getPackageName(), line.getSourceFile());
      if (path == null) {
        // No identifiable source location
        logError("Couldn't find source file for "+line.getClassName());
        return;
      }
      
      ArtifactBuilder artifact = generator.artifact();
      SourceLocationBuilder sourceLocation = artifact.primarySourceLocation();  

      sourceLocation.packageName(line.getPackageName());
      sourceLocation.compilation(line.getSourceFile());
      sourceLocation.className(line.getClassName());
      
      FieldAnnotation field = bug.getPrimaryField();
      if (field != null && field.getSourceLines() == line) {
        sourceLocation.type(IdentifierType.FIELD);
        sourceLocation.identifier(field.getFieldName());
      } else {
        MethodAnnotation method = bug.getPrimaryMethod();    
        if (method != null && method.getSourceLines() == line) {
          sourceLocation.type(IdentifierType.METHOD);
          sourceLocation.identifier(method.getMethodSignature());
        } else {
          sourceLocation.type(IdentifierType.CLASS);
          sourceLocation.identifier(line.getClassName());
        }
      }
      
      HashGenerator hashGenerator = HashGenerator.getInstance();
      Long hashValue = hashGenerator.getHash(path, line.getStartLine());
      sourceLocation = sourceLocation.hash(hashValue).lineOfCode(line.getStartLine());            
      sourceLocation = sourceLocation.endLine(line.getEndLine());
      
      artifact.findingType(getName(), getVersion(), bug.getType());
      artifact.message(bug.getMessageWithoutPrefix());      
      
      int priority = bug.getPriority();
      Priority assignedPriority = getFindBugsPriority(priority);
      Severity assignedSeverity = getFindBugsSeverity(priority);
      artifact.priority(assignedPriority).severity(assignedSeverity);
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
      tool.reportError(message);
    }

    public void logError(String message, Throwable e) {
      LOG.log(Level.SEVERE, message, e);
      tool.reportError(message, e);
    }

    public void reportMissingClass(ClassNotFoundException ex) {      
      if (missingClasses.contains(ex.getMessage())) {
        LOG.warning("Missing class: "+ex.getMessage());
        return;
      }
      missingClasses.add(ex.getMessage());
      LOG.log(Level.WARNING, "Missing class", ex);
      tool.reportError("Missing class", ex);
    }

    public void reportMissingClass(ClassDescriptor desc) {
      if ("package-info".equals(desc.getSimpleName()) ||
          desc.getClassName().charAt(0) == '[') {
        return;
      }
      String msg = "Class "+desc.getClassName()+" cannot be resolved";
      LOG.warning(msg);
      
      if (missingClasses.contains(msg)) {        
        return;
      }
      tool.reportError(msg);
    }

    public void reportSkippedAnalysis(MethodDescriptor method) {
      LOG.info("Skipped analysis: "+method.getSignature());
    }

    /* ******************** For IClassObserver ********************* */
    
    public void observeClass(ClassDescriptor desc) {
      monitor.subTask("Scanning "+desc.getDottedClassName());
    }
    
    private String computeSourceFilePath(String pkg, String srcFile) {
      String pkgPath = pkg.replace('.', '/');
      
      for(IToolTarget t : tool.getSrcTargets()) {
        final File root = new File(t.getLocation()); 
        switch (t.getKind()) {
        case DIRECTORY:
          File candidate = new File(root, pkgPath+'/'+srcFile);  
          if (candidate.exists() && candidate.isFile()) {
            return candidate.getAbsolutePath();
          }
          break;
        case JAR:
        case FILE:
          System.out.println("Ignored: "+root);
          break;
        }
      }
      return null;
    }
  }
  
  private static Severity getFindBugsSeverity(int priority) {
    switch (priority) {
    case 1:
      return Severity.ERROR;
    case 2:
      return Severity.WARNING;
    case 3:
      return Severity.WARNING;
    case 4:
      return Severity.ERROR;
    case 5:
      return Severity.INFO;
    }
    return null;

  }

  private static Priority getFindBugsPriority(int priority) {
    switch (priority) {
    case 1:
      return Priority.HIGH;
    case 2:
      return Priority.MEDIUM;
    case 3:
      return Priority.LOW;
    case 4:
      return Priority.EXPERIMENTAL;
    case 5:
      return Priority.IGNORE;
    }
    return null;
  }
}
