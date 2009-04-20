package com.surelogic.sierra.tool;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.HashGenerator;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.ArtifactGenerator.SourceLocationBuilder;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.IToolTarget;

/**
 * An abstract class designed to simplify implementation of IToolInstance
 * 
 * @author edwin
 */
public abstract class AbstractToolInstance extends AbstractSLJob implements IToolInstance {
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  protected static final String JAVA_SUFFIX = ".java";
  protected static final int JAVA_SUFFIX_LEN = JAVA_SUFFIX.length();

  /**
   * Gets the compilation unit name from a Java source file name
   * e.g. Baz from /foo/bar/Baz.java
   */
  public static String getCompUnitName(String file) {
	  int separator = file.lastIndexOf(File.separatorChar);
	  if (separator < 0) {
		  return file.substring(0, file.length() - JAVA_SUFFIX_LEN);
	  }
	  return file.substring(separator + 1, file.length()
			  - JAVA_SUFFIX_LEN);
  }
  
  protected final IToolFactory factory;
  protected final Config config;
  protected final ILazyArtifactGenerator genHandle;  
  private final List<IToolTarget> srcTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> binTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> auxTargets = new ArrayList<IToolTarget>();
  private final List<URI> paths = new ArrayList<URI>();
  private boolean done = false;
  private final boolean closeWhenDone;
  private final Map<String,String> options = new HashMap<String,String>();
  protected final boolean debug;
  private final SLStatus.Builder status = new SLStatus.Builder();
  private ArtifactGenerator generator = null;

  protected AbstractToolInstance(IToolFactory factory, Config config, ILazyArtifactGenerator gen, boolean close) {
	  super(factory.getName()); 
	  this.factory = factory;
	  this.config = config;
	  genHandle = gen;
	  closeWhenDone = close;
	  debug = LOG.isLoggable(Level.FINE);
  }

  public final String getHTMLInfo() {
	  return factory.getHTMLInfo();
  }

  public final String getId() {
	  return factory.getId();
  }

  public final String getName() {
	  return factory.getName();
  }

  public final String getVersion() {
	  return factory.getVersion();
  }
  
  public ArtifactGenerator getGenerator() {
	if (generator == null) {
		throw new UnsupportedOperationException("No generator right now");
	}
    return generator;
  }
  
  private void checkArgs(Object arg) {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    if (arg == null) {
      throw new IllegalArgumentException("Null argument");
    }
  }
  
  public void addTarget(IToolTarget target) {
    checkArgs(target);
    switch (target.getType()) {
      case SOURCE:
        srcTargets.add(target);
        break;
      case BINARY:
        binTargets.add(target);
        break;
      case AUX:
        auxTargets.add(target);
        break;
      default:
    	System.out.println("Ignoring target "+target.getLocation());
    }
  }
  
  public void addToClassPath(URI loc) {
    checkArgs(loc);
    paths.add(loc);
  }

  public final Iterable<IToolTarget> getSrcTargets() {
    return srcTargets;
  }
  
  public final Iterable<IToolTarget> getBinTargets() {
    return binTargets;
  }
  
  public final Iterable<IToolTarget> getAuxTargets() {
    return auxTargets;
  }
  
  protected final Iterable<URI> getPaths() {
    return paths;
  }
  
  /**
   * Encapsulates most of the details of using ILazyArtifactGenerator, ArtifactGenerator,
   * besides the actual creation of artifacts/errors
   */
  public SLStatus run(SLProgressMonitor monitor) {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    // monitor.setTaskName("Scanning with "+getName()+" v"+getVersion());
    if (debug) {
    	for(IToolTarget t : getSrcTargets()) {
    		System.out.println("Source: "+t.getLocation());
    	}
    	for(IToolTarget t : getBinTargets()) {
    		System.out.println("Binary: "+t.getLocation());
    	}
    	for(IToolTarget t : getAuxTargets()) {
    		System.out.println("Auxiliary: "+t.getLocation());
    	}
    }
    generator = genHandle.create(factory);
    
    try {
      execute(monitor);
    }
    catch(Exception e) {
      reportError("Exception during run()", e);
    }
    finally {
      done = true;
    }
    monitor.done();

    if (closeWhenDone) {
      generator.finished(monitor);
      genHandle.finished();
    }
    else if (genHandle.closeWhenDone()) {
      generator.finished(monitor);
    }
    generator = null;
    return status.build();
  }

  public final void reportWarning(String msg) {
	  if (msg == null) {
		  msg = "Null message";
	  }
	  generator.error().message(msg).tool(getName());
	  status.addChild(SLStatus.createWarningStatus(-1, msg));  
  }
  
  public void reportError(String msg) {
    generator.error().message(msg).tool(getName());
    status.addChild(SLStatus.createErrorStatus(-1, msg));  
  }
  
  public void reportError(String msg, Throwable e) {
    StringBuilder sb = new StringBuilder();
    sb.append(msg).append('\n');
    sb.append(e.getClass().getName()).append(' ').append(e.getMessage()).append('\n');
    for(StackTraceElement ste : e.getStackTrace()) {
      sb.append("\tat ").append(ste).append('\n');
    }
    generator.error().message(sb.toString()).tool(getName());
    status.addChild(SLStatus.createErrorStatus(-1, msg, e));
  }
  
  protected abstract void execute(SLProgressMonitor mon) throws Exception;    
  
  public void setOption(String key, String value) {
	if (key == null) {
	  throw new IllegalArgumentException("null key");
	}
	options.put(key, value);
  }
  protected String getOption(String key) {
	return options.get(key);
  }
  
  public interface TargetPrep {
	/**
	 * @param f the Java file location
	 */
	void prep(File f) throws Exception;	  
  }  
  
  /**
   * Iterates through all the Java source files, 
   * calling TargetPrep.prep() for each one
   */
  protected void prepJavaFiles(TargetPrep p) throws Exception {
	  for (IToolTarget t : getSrcTargets()) {
		  for (URI loc : t.getFiles()) {
			  File f = new File(loc);
			  if (f.exists() && f.getName().endsWith(JAVA_SUFFIX)) {
				  p.prep(f);
			  }
		  }
	  }  
  }
  
  /**
   * Iterates through all the Java binary files, 
   * calling TargetPrep.prep() for each one
   */
  protected void prepClassFiles(TargetPrep p) throws Exception {
	  for (IToolTarget t : getBinTargets()) {
		  final File location = new File(t.getLocation());
		  switch (t.getKind()) {
		  case FILE:
		  case JAR:
			  p.prep(location);
			  break;
		  case DIRECTORY:
			  for (URI loc : t.getFiles()) {
				  File f = new File(loc);
				  if (f.exists()) {
					  p.prep(f);
				  }
			  }
			  break;
		  default:
			  System.out.println("Ignoring target " + location);
		  }
	  }
  }
  
  /**
   * Iterates through all the Java libraries, 
   * calling TargetPrep.prep() for each one
   */
  protected void prepAuxFiles(TargetPrep p) throws Exception {
	  for (IToolTarget t : getAuxTargets()) {
		  final File location = new File(t.getLocation());
		  switch (t.getKind()) {
		  case DIRECTORY:
		  case JAR:
			  p.prep(location);
			  break;
		  case FILE:							
		  default:
			  System.out.println("Ignoring target " + location);
		  }
	  }
  }
  
  /**
   * A helper class that encapsulates the source directories that
   * any given Java source file was found under.  Mostly used to
   * compute the package names for each file
   * 
   * @author edwin
   */
  public static class SourceRoots {
	  private final Map<String, String> roots = new HashMap<String, String>();

	  void addRoot(String locName, URI uri) {
		  String root = new File(uri).getAbsolutePath();
		  roots.put(locName, root);
	  }
	  void addRoot(String path, String locName) {
		  roots.put(path, locName);
	  }
	  
	  public void initSourceInfo(final SourceInfo info, final String fileName) {
		  info.fileName = fileName;

		  final String root = roots.get(fileName);
		  if (root == null) {
			  throw new IllegalArgumentException(fileName
					  + " doesn't have a source root");
		  }
		  final String file;
		  if (fileName.startsWith(root)) {
			  file = fileName.substring(root.length() + 1);
		  } else {
			  throw new IllegalArgumentException(fileName
					  + " start with root " + root);
		  }

		  // Modified from AbstractPMDTool.getCompUnitName()
		  int separator = file.lastIndexOf(File.separatorChar);
		  if (separator < 0) {
			  // Default package
			  info.packageName = "";
			  info.cuName = file.substring(0, file.length()
					  - JAVA_SUFFIX_LEN);
		  } else {
			  info.packageName = file.substring(0, separator).replace(
					  File.separatorChar, '.');
			  info.cuName = file.substring(separator + 1, file.length()
					  - JAVA_SUFFIX_LEN);
		  }
	  }
  }
  
  /**
   * Iterates through all the Java source files, 
   * calling SourcePrep.prep() for each one
   * 
   * Similar to prepJavaFiles(), but also collects source root info
   */
  protected SourceRoots collectSourceRoots(TargetPrep p) throws Exception {
	  SourceRoots roots = new SourceRoots();
	  for (IToolTarget t : getSrcTargets()) {
		  File location = new File(t.getLocation());
		  String locName = location.getAbsolutePath();
		  if (t instanceof FileTarget) {
			  FileTarget ft = (FileTarget) t;
			  if (!location.exists() || !location.getName().endsWith(JAVA_SUFFIX)) {
				  continue;
			  }
			  if (ft.getRoot() != null) {
				  roots.addRoot(locName, ft.getRoot());
			  } else {
				  System.out.println("No root for " + locName);
			  }
			  p.prep(location);
		  } else {
			  for (URI loc : t.getFiles()) {
				  File file = new File(loc);
				  if (file.exists() && file.getName().endsWith(JAVA_SUFFIX)) {
					  roots.addRoot(file.getAbsolutePath(), locName);
					  p.prep(file);
				  }
			  }
		  }
	  }
	  return roots;
  }
  
  /**
   * Encapsulates the info needed to add a source location for an artifact
   * 
   * @author edwin
   */
  public static class SourceInfo {
	public String fileName;
	public String packageName;
	public String cuName;
	public int startLine;
	public int endLine;
	public IdentifierType type;
	public String identifier;
	
	public static SourceInfo get(SourceRoots roots, String file, int line) {
		SourceInfo info = new SourceInfo();
		roots.initSourceInfo(info, file);
		info.startLine = info.endLine = line;	
		return info;
	}
  }
  
  /**
   * Builds a source location from a SourceInfo object
   */
  protected static void setSourceLocation(
			SourceLocationBuilder sourceLocation, SourceInfo info) {
		sourceLocation.packageName(info.packageName);
		sourceLocation.compilation(info.cuName);
		sourceLocation.className(info.cuName);
		if (info.type != null && info.identifier != null) {
			sourceLocation.type(info.type);
			sourceLocation.identifier(info.identifier);
		} else {
			sourceLocation.type(IdentifierType.CLASS);
			sourceLocation.identifier(info.cuName);
		}
		HashGenerator hashGenerator = HashGenerator.getInstance();
		Long hashValue = hashGenerator.getHash(info.fileName,
				info.startLine);

		sourceLocation = sourceLocation.hash(hashValue).lineOfCode(
				info.startLine);
		sourceLocation = sourceLocation.endLine(info.endLine);

		//System.out.println("Building location for "+info.fileName+" : "
		// +info.startLine);
		sourceLocation.build();
	}
}
