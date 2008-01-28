package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.renderers.Renderer;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.HashGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.*;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractPMDTool extends AbstractTool {
  public AbstractPMDTool(String version) {
    super("PMD", version, "PMD", "");
  }

  protected final IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {      
        int cpus = Runtime.getRuntime().availableProcessors();
        String encoding = new InputStreamReader(System.in).getEncoding();
        String altEncoding = Charset.defaultCharset().name();
        if (!encoding.equals(altEncoding)) {
          System.out.println("Encoding '"+encoding+"' != "+altEncoding);
        }
        SourceType sourceType = SourceType.JAVA_15;
        String rulesets = "all.xml"; // location of the XML rule file 
        RuleContext ctx = new RuleContext(); // info about what's getting scanned
        RuleSetFactory ruleSetFactory = new RuleSetFactory(); // only the default rules
        
        String excludeMarker = PMD.EXCLUDE_MARKER;
        for(IToolTarget t : getSrcTargets()) {
          // root dir of where the files are
          final String inputPath = new File(t.getLocation()).getAbsolutePath();           
          List<DataSource> files = new ArrayList<DataSource>();
          for(URI loc : t.getFiles()) {
            File f = new File(loc);
            if (f.exists()) {              
              files.add(new FileDataSource(f));
            }
          }
          List<Renderer> renderers = new ArrayList<Renderer>(); // output
          renderers.add(new Output(generator, monitor, inputPath));
          
          monitor.beginTask("PMD", files.size() + 500);
          PMD.processFiles(cpus, ruleSetFactory, sourceType, files, ctx, renderers, rulesets, false, inputPath, encoding, excludeMarker);
        }
      }      
    };
  }
  
  private static final int SUFFIX_LEN = ".java".length();
  
  class Output implements Renderer {
    private final ArtifactGenerator generator;
    private final SLProgressMonitor monitor;
    private final String inputPath;
    private boolean first = true;
    
    public Output(ArtifactGenerator gen, SLProgressMonitor m, String in) {
      generator = gen;
      monitor = m;
      inputPath = in;
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
    }

    public void start() throws IOException {
    }

    public synchronized void startFileAnalysis(DataSource dataSource) {
      String msg = "Scanning "+dataSource.getNiceFileName(false, inputPath);
      monitor.subTask(msg); 
      if (first) {
        first = false;
      } else {
        monitor.worked(1);
      }
      // System.out.println(msg);
      LOG.info(msg);
    }
    
    private String getCompUnitName(String file) {
      int separator = file.lastIndexOf(File.separatorChar);
      if (separator < 0) {
        return file.substring(0, file.length() - SUFFIX_LEN);
      }
      return file.substring(separator+1, file.length() - SUFFIX_LEN);
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
        
        if ("".equals(v.getClassName())) {
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
        System.out.println(error.getFile()+": "+error.getMsg());
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
    }
  }
  
  private static Severity getPMDSeverity(int priority) {
    switch (priority) {
    case 1:
      return Severity.ERROR;
    case 2:
      return Severity.ERROR;
    case 3:
      return Severity.WARNING;
    case 4:
      return Severity.WARNING;
    case 5:
      return Severity.INFO;
    }
    return null;
  }

  private static Priority getPMDPriority(int priority) {
    switch (priority) {
    case 1:
      return Priority.HIGH;
    case 2:
      return Priority.MEDIUM;
    case 3:
      return Priority.HIGH;
    case 4:
      return Priority.MEDIUM;
    case 5:
      return Priority.LOW;
    }
    return null;
  }
}
