package com.surelogic.sierra.tool.pmd40;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.renderers.Renderer;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class PMDTool extends AbstractTool {
  public PMDTool() {
    super("PMD", "4.0", "PMD", "");
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }

  public IToolInstance create(final SLProgressMonitor monitor) {
    return new AbstractToolInstance(this, monitor) {
      @Override
      protected void execute() throws Exception {      
        int cpus = Runtime.getRuntime().availableProcessors();
        String encoding = Charset.defaultCharset().name();
        SourceType sourceType = SourceType.JAVA_15;
        String rulesets = "rulesets/basic.xml"; // location of the XML rule file 
        RuleContext ctx = new RuleContext(); // info about what's getting scanned
        RuleSetFactory ruleSetFactory = new RuleSetFactory(); // only the default rules
        
        String excludeMarker = PMD.EXCLUDE_MARKER;
        for(IToolTarget t : getTargets()) {
          if (!t.isSource()) {
            continue;
          }
          // root dir of where the files are
          String inputPath = new File(t.getLocation()).getAbsolutePath();           
          List<DataSource> files = new ArrayList<DataSource>();
          for(URI loc : t.getFiles()) {
            File f = new File(loc);
            if (f.exists()) {              
              files.add(new FileDataSource(f));
            }
          }
          List<Renderer> renderers = new ArrayList<Renderer>(); // output
          renderers.add(new Output(monitor, inputPath, files.size()));
          PMD.processFiles(cpus, ruleSetFactory, sourceType, files, ctx, renderers, rulesets, false, inputPath, encoding, excludeMarker);
        }
      }      
    };
  }
  
  class Output implements Renderer {
    private final SLProgressMonitor monitor;
    private final int numFiles;
    private final String inputPath;
    private boolean first = true;
    
    public Output(SLProgressMonitor m, String in, int i) {
      monitor = m;
      inputPath = in;
      numFiles = i;
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
      monitor.beginTask("Starting PMD scanning", numFiles + 500);
    }

    public void startFileAnalysis(DataSource dataSource) {
      String msg = "Scanning "+dataSource.getNiceFileName(false, inputPath);
      monitor.subTask(msg); 
      if (first) {
        first = false;
      } else {
        monitor.internalWorked(1.0);
      }
      System.out.println(msg);
    }
    
    public void renderFileReport(Report report) throws IOException {
      Iterator<IRuleViolation> it = report.iterator();
      while (it.hasNext()) {
        IRuleViolation v = it.next();
        System.out.println(v.getFilename()+": "+v.getDescription());
      }
    }
    
    public void end() throws IOException {
      monitor.done();
    }
  }
}
