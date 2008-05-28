package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.renderers.Renderer;

import com.surelogic.common.*;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.*;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractPMDTool extends AbstractTool {
  public AbstractPMDTool(String version, boolean debug) {
    super("PMD", version, "PMD", "", debug);
  }

  protected final IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(debug, this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {      
        int cpus = Runtime.getRuntime().availableProcessors();
        String encoding = new InputStreamReader(System.in).getEncoding();
        String altEncoding = Charset.defaultCharset().name();
        if (!encoding.equals(altEncoding)) {
          System.out.println("Encoding '"+encoding+"' != "+altEncoding);
        }
        final String sourceLevel = getOption(SOURCE_LEVEL);
        final SourceType sourceType;
        if ("1.4".equals(sourceLevel)) {
        	sourceType = SourceType.JAVA_14;
        }
        else if ("1.5".equals(sourceLevel)) {
        	sourceType = SourceType.JAVA_15;
        }
        else if ("1.3".equals(sourceLevel)) {
        	sourceType = SourceType.JAVA_13;
        }
        else if ("1.6".equals(sourceLevel) ||
        		 "1.7".equals(sourceLevel)) {
        	sourceType = SourceType.JAVA_16;
        } 
        else {
        	sourceType = SourceType.JAVA_14;
        }
        String rulesets = "all.xml"; // location of the XML rule file 
        RuleContext ctx = new RuleContext(); // info about what's getting scanned
        RuleSetFactory ruleSetFactory = new RuleSetFactory(); // only the default rules
        
        String excludeMarker = PMD.EXCLUDE_MARKER;
        
        // Added for PMD 4.2
        final File auxPathFile = File.createTempFile("auxPath", ".txt");
        if (auxPathFile.exists()) {
        	PrintWriter pw = new PrintWriter(auxPathFile);
        	for(IToolTarget t : getAuxTargets()) {
        		pw.println(new File(t.getLocation()).getAbsolutePath());
        	}
        	pw.close();        
        }
        final ClassLoader cl = PMD.createClasspathClassLoader(auxPathFile.toURI().toURL().toString());
        
        final List<DataSource> files = new ArrayList<DataSource>();
        for(IToolTarget t : getSrcTargets()) {       
          for(URI loc : t.getFiles()) {        	
            File f = new File(loc);            
            if (f.exists() && f.getName().endsWith(".java")) {                    
              files.add(new FileDataSource(f));
            }
          }          
        }
        final List<Renderer> renderers = new ArrayList<Renderer>(); // output
        renderers.add(new Output(generator, monitor));
          
        monitor.beginTask("PMD", files.size() + 25);
        PMD.processFiles(cpus, ruleSetFactory, sourceType, files, ctx, renderers, rulesets, 
        		         false, "", encoding, excludeMarker, cl);
        auxPathFile.delete();
      }      
    };
  }
  
  class Output implements Renderer {
    private final ArtifactGenerator generator;
    private final SLProgressMonitor monitor;
    private boolean first = true;
    
    public Output(ArtifactGenerator gen, SLProgressMonitor m) {
      generator = gen;
      monitor = m;
    }

    public Writer getWriter() {
      throw new UnsupportedOperationException();
    }

    public String render(Report report) {
      throw new UnsupportedOperationException();
    }

    public void render(Writer writer, Report report) throws IOException {
      throw new UnsupportedOperationException();
    }

    public void setWriter(Writer writer) {
      throw new UnsupportedOperationException();
    }

    public void showSuppressedViolations(boolean show) {
      // Do nothing
    }

    public void start() throws IOException {
      // Do nothing
    }

    public synchronized void startFileAnalysis(DataSource dataSource) {
      String msg = "Scanning "+dataSource.getNiceFileName(false, "");
      monitor.subTask(msg); 
      if (first) {
        first = false;
      } else {
        monitor.worked(1);
      }
      // System.out.println(msg);
      LOG.fine(msg);
    }
    
    private String getCompUnitName(String file) {
      int separator = file.lastIndexOf(File.separatorChar);
      if (separator < 0) {
        return file.substring(0, file.length() - JAVA_SUFFIX_LEN);
      }
      return file.substring(separator+1, file.length() - JAVA_SUFFIX_LEN);
    }
    
    public synchronized void renderFileReport(Report report) throws IOException {      
      Iterator<IRuleViolation> it = report.iterator();
      while (it.hasNext()) {
        IRuleViolation v = it.next();
        if (LOG.isLoggable(Level.FINE)) {
          System.out.println(v.getFilename()+": "+v.getDescription());
        }        
        ArtifactBuilder artifact = generator.artifact();
        SourceLocationBuilder sourceLocation = artifact.primarySourceLocation();
 
        String file = v.getFilename();
        sourceLocation.packageName(v.getPackageName());
        String cuName = getCompUnitName(file);
        sourceLocation.compilation(cuName);
        
        if (v.getClassName() == null || "".equals(v.getClassName())) {
          // No class name, so use the main class for the compilation unit
          sourceLocation.className(cuName);
        } else {
          sourceLocation.className(v.getClassName());
        }
        
        String method = v.getMethodName();
        String field  = v.getVariableName();
        if ("".equals(method)) {
          sourceLocation.type(IdentifierType.CLASS);
          sourceLocation.identifier(v.getClassName());
        } 
        else if ("".equals(field)) {
          sourceLocation.type(IdentifierType.METHOD);
          sourceLocation.identifier(method);
        }
        else {
          sourceLocation.type(IdentifierType.FIELD);
          sourceLocation.identifier(field);
        }
        
        HashGenerator hashGenerator = HashGenerator.getInstance();
        Long hashValue = hashGenerator.getHash(v.getFilename(), v.getBeginLine());
        //FIX use v.getBeginColumn();
        //FIX use v.getEndColumn();
        sourceLocation = sourceLocation.hash(hashValue).lineOfCode(v.getBeginLine());            
        sourceLocation = sourceLocation.endLine(v.getEndLine());
        
        artifact.findingType(getName(), getVersion(), v.getRule().getName());
        artifact.message(v.getDescription());
        
        int priority = v.getRule().getPriority();        
        Priority assignedPriority = getPMDPriority(priority);
        Severity assignedSeverity = getPMDSeverity(priority);
        artifact.priority(assignedPriority).severity(assignedSeverity);

        sourceLocation.build();
        artifact.build();
      }
      
      Iterator<ProcessingError> errors = report.errors();
      while (errors.hasNext()) {
        ProcessingError error = errors.next();
        LOG.warning(error.getFile()+": "+error.getMsg());
        ErrorBuilder eb = generator.error();
        eb.message(error.getMsg());
        eb.tool(getName()+" v."+getVersion());        
        eb.build();
      }

      /*
      Iterator<Metric> metrics = report.metrics();
      while (metrics.hasNext()) {
        Metric m = metrics.next();
        //System.out.println(m.getMetricName()+"(total) : "+m.getTotal());
        if ("NcssTypeCount".equals(m.getMetricName())) {
          MetricBuilder mb = generator.metric();
          String fileName = report.getSource().getNiceFileName(true, inputPath);
          int lastSeparator = fileName.lastIndexOf(File.separatorChar);
          if (lastSeparator > 0) {
            mb.packageName(fileName.substring(0, lastSeparator).replace(File.separatorChar, '.'));
            mb.compilation(fileName.substring(lastSeparator+1));
          } else {
            mb.packageName("");
            mb.compilation(fileName);
          }
          mb.linesOfCode((int) m.getTotal());
          System.out.println(fileName+" : "+m.getTotal()+" LOC");
          // mb.build();
        }
      }
      */
      //System.out.println("Done with report");
    }
    
    public void end() throws IOException {
      // Do nothing
    }
  }
  
  private static Severity getPMDSeverity(int priority) {
    switch (priority) {
    case 1:
    case 2:
      return Severity.ERROR;
    case 3:
    case 4:
      return Severity.WARNING;
    case 5:
    default:
      return Severity.INFO;
    }
  }

  private static Priority getPMDPriority(int priority) {
    switch (priority) {
    case 1:
    case 3:
      return Priority.HIGH;
    case 4:
    case 2:
      return Priority.MEDIUM;
    case 5:
    default:
      return Priority.LOW;
    }
  }
}
