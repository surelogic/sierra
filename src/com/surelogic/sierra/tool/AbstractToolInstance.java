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
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractToolInstance extends AbstractSLJob implements IToolInstance {
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  protected static final int JAVA_SUFFIX_LEN = ".java".length();

  public static String getCompUnitName(String file) {
	  int separator = file.lastIndexOf(File.separatorChar);
	  if (separator < 0) {
		  return file.substring(0, file.length() - JAVA_SUFFIX_LEN);
	  }
	  return file.substring(separator + 1, file.length()
			  - JAVA_SUFFIX_LEN);
  }
  
  public static String getPackageName(String file) {
	  return null; // TODO fix to use code from CPD
  }
  
  protected final IToolFactory factory;
  protected final Config config;
  protected final ILazyArtifactGenerator genHandle;  
  private final List<IToolTarget> srcTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> binTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> auxTargets = new ArrayList<IToolTarget>();
  private final List<URI> paths = new ArrayList<URI>();
  private boolean done = false;
  protected final boolean closeWhenDone;
  private final Map<String,String> options = new HashMap<String,String>();
  protected final boolean debug;
  protected final SLStatus.Builder status = new SLStatus.Builder();
  private ArtifactGenerator generator = null;

  protected AbstractToolInstance(IToolFactory factory, Config config, ILazyArtifactGenerator gen, boolean close) {
	  super(factory.getName()); // FIX is this the right name?
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
      status.addChild(execute(monitor));
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
  
  protected abstract SLStatus execute(SLProgressMonitor mon) throws Exception;    

  /**************** ITool **********************/  
  public Set<ArtifactType> getArtifactTypes() {
    return factory.getArtifactTypes();
  }
  
  public List<File> getRequiredJars(Config config) {
	return factory.getRequiredJars(config);
  }
  
  public final IToolInstance create() {
    throw new UnsupportedOperationException("Instances can't create other instances");
  }
  
  public final IToolInstance create(String name, ILazyArtifactGenerator generator) {
    throw new UnsupportedOperationException("Instances can't create other instances");
  }
  
  public void setOption(String key, String value) {
	if (key == null) {
	  throw new IllegalArgumentException("null key");
	}
	options.put(key, value);
  }
  protected String getOption(String key) {
	return options.get(key);
  }
  
  public interface SourcePrep {
	void prep(File f);	  
  }  
  protected void prepJavaFiles(SourcePrep p) {
	  for (IToolTarget t : getSrcTargets()) {
		  for (URI loc : t.getFiles()) {
			  File f = new File(loc);
			  if (f.exists() && f.getName().endsWith(".java")) {
				  p.prep(f);
			  }
		  }
	  }  
  }
  
  public static class SourceInfo {
	public String fileName;
	public String packageName;
	public String cuName;
	public int startLine;
	public int endLine;
	public IdentifierType type;
	public String identifier;
	
	public static SourceInfo get(String file, int line) {
		SourceInfo info = new SourceInfo();
		info.fileName = file;
		info.cuName = getCompUnitName(file);					
		info.packageName = getPackageName(file);
		info.startLine = info.endLine = line;	
		return info;
	}
  }
  
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
