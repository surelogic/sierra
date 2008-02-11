package com.surelogic.sierra.tool.findbugs;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.HashGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.*;
import com.surelogic.sierra.tool.targets.*;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.config.UserPreferences;

public abstract class AbstractFindBugsTool extends AbstractTool {
  final String fbDir;
  protected AbstractFindBugsTool(String version, String fbDir) {
    super("FindBugs", version, "FindBugs (TM)", "");
    this.fbDir = fbDir;
  }
  
  protected IToolInstance create(final ArtifactGenerator generator, 
                                 final SLProgressMonitor monitor, boolean close) {
    System.setProperty("findbugs.home", fbDir);
    monitor.subTask("Set FB home to "+fbDir);
    
    return new AbstractToolInstance(this, generator, monitor, close) {     
      final IFindBugsEngine engine = createEngine();
      
      @Override
      protected void execute() throws Exception { 
        final Project p = createProject();
        engine.setProject(p);
        engine.setUserPreferences(UserPreferences.getUserPreferences()); 
        //engine.setAnalysisFeatureSettings(arg0);
        engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        //engine.setClassScreener(new Screener());
        
        Monitor mon = new Monitor(this); 
        //engine.addClassObserver(mon);
        engine.setBugReporter(mon);
        engine.setProgressCallback(mon);        
        try {
          engine.execute();
        } catch(IOException e) {
          if (!e.getMessage().startsWith("No classes found to analyze")) {
            throw e;
          } else {
            // Ignored
          }
        }
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
            default:
              System.out.println("Ignoring target "+t.getLocation());              
          }
        }
        for(IToolTarget t : getAuxTargets()) {
          final String path = new File(t.getLocation()).getAbsolutePath(); 
          switch (t.getKind()) {
            case DIRECTORY:
            case JAR:
              p.addAuxClasspathEntry(path);             
              
              IToolTarget auxSrc = t.getAuxSources();
              if (auxSrc != null && auxSrc.getKind() == IToolTarget.Kind.DIRECTORY) {                             
                p.addSourceDir(new File(t.getLocation()).getAbsolutePath());
              }              
              // FIX how to deal w/ jars?
              break;
            case FILE:
              System.out.println("FB ignored AUX file: "+path);
              break;
            default:
            	System.out.println("Ignoring target "+t.getLocation());              
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
            // System.out.println("Ignored: "+path);
            FileTarget ft = (FileTarget) t;
            URI root = ft.getRoot();
            //System.out.println(path+" : "+root);
            String rootPath = new File(root).getAbsolutePath();
            p.addSourceDir(rootPath);
            break;
          default:
          	System.out.println("Ignoring target "+t.getLocation());            
          }
        }
        return p;
      }
    };
  }
  
  protected abstract IFindBugsEngine createEngine();
  
  static class Screener implements IClassScreener {
    public boolean matches(String fileName) {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean vacuous() {
      return true;
    }  
  }
  
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
    
    public void reportNumberOfArchives(int numArchives) {
      System.out.println("# Archives: "+numArchives);
      monitor.beginTask("FindBugs setup", numArchives);
    }
    
    public void finishArchive() {
      //System.out.println("Finished an archive");
      monitor.worked(1);
    }

    public void predictPassCount(int[] classesPerPass) {
      int i = 1;
      int total = 0;
      for(int count : classesPerPass) {
        System.out.println("Pass\t"+i+": "+count);
        i++;
        total += count;
      }
      monitor.beginTask("FindBugs", total);
    }

    public void startAnalysis(int numClasses) {
      System.out.println("startAnalysis: "+numClasses);
    }
    
    public void finishClass() {
      //System.out.println("Finished a class");
      monitor.worked(1);
    }
    
    public void finishPerClassAnalysis() {
      System.out.println("finishPerClassAnalysis");
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
      if (LOG.isLoggable(Level.FINE)) {
        System.out.println("Bug reported: "+bug.getAbridgedMessage());
      }
      stats.addBug(bug);
      
      ArtifactBuilder artifact = generator.artifact();
      SourceLocations locations = computeSourceLocations(bug);
      final SourceLineAnnotation primary = locations.getPrimary();
      if (primary != null) {
        final SourceLocationBuilder sourceLocation = artifact.primarySourceLocation();
        MethodAnnotation method = locations.getPrimaryMethod();    
        if (method != null && method.getSourceLines() == primary) {
          sourceLocation.type(IdentifierType.METHOD);
          sourceLocation.identifier(method.getMethodSignature());
        } else {
          FieldAnnotation field = locations.getPrimaryField();
          if (field != null && field.getSourceLines() == primary) {
            sourceLocation.type(IdentifierType.FIELD);
            sourceLocation.identifier(field.getFieldName());
          } else {
            ClassAnnotation clazz = locations.getPrimaryClass();
            if (clazz != null && clazz.getSourceLines() == primary) {
              sourceLocation.type(IdentifierType.CLASS);
              sourceLocation.identifier(clazz.getClassName());
            } else {
              // No match for primary location
              sourceLocation.type(IdentifierType.CLASS);
              sourceLocation.identifier(primary.getSimpleClassName());
            }
          }
        } 
        SourceLocationBuilder sourceLocation2 = setupSourceLocation(primary, sourceLocation);     
        if (sourceLocation2 != null) {
          sourceLocation2.build();
        } else {
          return;
        }
      } else {
        throw new IllegalArgumentException("No primary location for "+bug);
      }
      
      for(final SourceLineAnnotation line : locations.getRest()) {      
        SourceLocationBuilder sourceLocation = artifact.sourceLocation();                                                      
        sourceLocation = setupSourceLocation(line, sourceLocation);        
        sourceLocation.build();
      }
      artifact.findingType(getName(), getVersion(), bug.getType());
      artifact.message(bug.getMessageWithoutPrefix());      
      
      int priority = bug.getPriority();
      Priority assignedPriority = getFindBugsPriority(priority);
      Severity assignedSeverity = getFindBugsSeverity(priority);
      artifact.priority(assignedPriority).severity(assignedSeverity);
      artifact.build();
    }
    
    private String getCompUnitName(String file) {
      if (file.endsWith(".java")) {
        return file.substring(0, file.length()-5);
      }
      return file;
    }
    
    private SourceLocationBuilder setupSourceLocation(final SourceLineAnnotation line, 
                                                      SourceLocationBuilder sourceLocation) {
      sourceLocation.packageName(line.getPackageName());      
      sourceLocation.compilation(getCompUnitName(line.getSourceFile()));
      sourceLocation.className(line.getSimpleClassName());
      
      final String path = computeSourceFilePath(line.getPackageName(), line.getSourceFile());
      if (path == null) {
        // No identifiable source location
        logError("Couldn't find source file for "+line.getClassName());
        return null;
      }  
      HashGenerator hashGenerator = HashGenerator.getInstance();
      final int start = line.getStartLine() < 0 ? 0 : line.getStartLine();
      Long hashValue = hashGenerator.getHash(path, start);
      sourceLocation = sourceLocation.hash(hashValue).lineOfCode(start); 
      
      final int end = line.getEndLine() < start ? start : line.getEndLine();
      sourceLocation = sourceLocation.endLine(end);
      return sourceLocation;
    }

    public void reportQueuedErrors() {
      // Do nothing
    }

    public void setErrorVerbosity(int level) {
      // Do nothing
    }

    public void setPriorityThreshold(int threshold) {
      // Do nothing
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
      LOG.info("Skipped analysis: "+method.getName()+method.getSignature());
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
          FileTarget ft = (FileTarget) t;
          if (ft.getRoot() != null) {
            File candidate2 = new File(new File(ft.getRoot()), pkgPath+'/'+srcFile);
            if (candidate2.exists() && candidate2.isFile()) {
              String path2 = candidate2.getAbsolutePath();
              if (path2.equals(root.getAbsolutePath())) {
                return path2;
              }
            }
          } else {
            System.out.println("Ignored FB source file: "+root);
          }
          break;
        default:
        	System.out.println("Ignoring target "+t.getLocation());
        }
      }
      return null;
    }
  }
  
  /**
   * Originally from BugInstance.getPrimarySourceLineAnnotation
   */
  private SourceLocations computeSourceLocations(BugInstance bug) {
    SourceLineAnnotation first = null;
    List<SourceLineAnnotation> rest = null;
    MethodAnnotation method = null;
    FieldAnnotation field = null;
    ClassAnnotation clazz = null;

    Iterator<BugAnnotation> annos = bug.annotationIterator();
    while (annos.hasNext()) {
      BugAnnotation annotation = annos.next();
      if (annotation instanceof SourceLineAnnotation) {
        SourceLineAnnotation anno = (SourceLineAnnotation) annotation;
        if (first == null) {
          first = anno;
        } else {
          /*
          if (!anno.getDescription().equals("SOURCE_LINE_ANOTHER_INSTANCE") &&
              !anno.getDescription().equals("SOURCE_LINE_DEFAULT") &&
              !anno.getDescription().contains("_NULL")) {
            LOG.severe(anno.getDescription());
          }
          */
          if (rest == null) {
            rest = new ArrayList<SourceLineAnnotation>();
          } 
          rest.add(anno);
        }
      }
      else if (annotation instanceof PackageMemberAnnotation) {
        if (annotation instanceof MethodAnnotation) {   
          if (method != null) {
            //LOG.info("Got another "+annotation);
          } else {
            method = (MethodAnnotation) annotation;
          }
        }
        else if (annotation instanceof FieldAnnotation) {
          if (field != null) {
            //LOG.info("Got another "+annotation);
          } else {
            field = (FieldAnnotation) annotation;
          }
        }
        else if (annotation instanceof ClassAnnotation) {
          if (clazz != null) {
            //LOG.info("Got another "+annotation);
          } else {
            clazz = (ClassAnnotation) annotation;
          }
        }
      }
      else {
        //LOG.info("Got a "+annotation);
      }
    }
    return new SourceLocations(first, rest, method, field, clazz);
  }
  
  private static class SourceLocations {
    final SourceLineAnnotation primary;
    final Iterable<SourceLineAnnotation> rest;
    final MethodAnnotation method;
    final FieldAnnotation field;
    final ClassAnnotation clazz;
    
    SourceLocations(SourceLineAnnotation first,
                           List<SourceLineAnnotation> r, MethodAnnotation m,
                           FieldAnnotation f, ClassAnnotation c) {
      if (first == null) {
        if (m != null) {
          first = m.getSourceLines();
        } else if (f != null) {
          first = f.getSourceLines();
        } else if (c != null) {
          first = c.getSourceLines();
        } else {
          throw new IllegalArgumentException("No primary location");
        }
      }
      if (r == null) {
        r = Collections.emptyList();
      }
      primary = first;
      rest = r;
      method = m;
      field = f;
      clazz = c;
    }

    public SourceLineAnnotation getPrimary() {
      return primary;
    }
    
    public Iterable<SourceLineAnnotation> getRest() {
      return rest;
    }
    
    public FieldAnnotation getPrimaryField() {
      return field;
    }
    
    public MethodAnnotation getPrimaryMethod() {
      return method;
    }
    
    public ClassAnnotation getPrimaryClass() {
      return clazz;
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
    default:
      return Severity.INFO;
    }
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
    default:    	
      return Priority.IGNORE;
    }
  }
}
