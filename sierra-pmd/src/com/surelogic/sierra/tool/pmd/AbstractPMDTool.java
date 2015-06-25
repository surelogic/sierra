package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;

import com.surelogic.common.HashGenerator;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ErrorBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.SourceLocationBuilder;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class AbstractPMDTool extends AbstractToolInstance {
  public AbstractPMDTool(PMDToolFactory f, Config config, ILazyArtifactGenerator generator, boolean close) {
    super(f, config, generator, close);
  }

  @Override
  protected void execute(SLProgressMonitor monitor) throws Exception {
    final PMDConfiguration config = new PMDConfiguration();
    config.setThreads(Runtime.getRuntime().availableProcessors());
    String encoding = new InputStreamReader(System.in).getEncoding();
    String altEncoding = Charset.defaultCharset().name();
    if (!encoding.equals(altEncoding)) {
      System.out.println("Encoding '" + encoding + "' != " + altEncoding);
    }
    config.setSourceEncoding(encoding);

    final String sourceLevel = getOption(SOURCE_LEVEL);
    final Language java = new JavaLanguageModule();
    LanguageVersion sourceType = java.getVersion(sourceLevel);
    /*
     * if ("1.4".equals(sourceLevel)) { sourceType =
     * java.getVersion(sourceLevel); } else if ("1.5".equals(sourceLevel)) {
     * sourceType = LanguageVersion.JAVA_15; } else if
     * ("1.3".equals(sourceLevel)) { sourceType = LanguageVersion.JAVA_13; }
     * else if ("1.6".equals(sourceLevel)) { sourceType =
     * LanguageVersion.JAVA_16; } else if ("1.7".equals(sourceLevel)) {
     * sourceType = LanguageVersion.JAVA_17; } else if
     * ("1.8".equals(sourceLevel)) { sourceType = LanguageVersion.JAVA_18; }
     * else { sourceType = LanguageVersion.JAVA_16; }
     */
    if (sourceType == null) {
      sourceType = java.getDefaultVersion();
    }
    config.setDefaultLanguageVersion(sourceType);

    RuleContext ctx = new RuleContext(); // info about what's
    // getting scanned

    // String excludeMarker = PMD.EXCLUDE_MARKER;

    // Added for PMD 4.2
    final File auxPathFile = File.createTempFile("auxPath", ".txt");
    if (auxPathFile.exists()) {
      PrintWriter pw = new PrintWriter(auxPathFile);
      for (IToolTarget t : getAuxTargets()) {
        pw.println(new File(t.getLocation()).getAbsolutePath());
      }
      pw.close();
    }
    /*
     * final ClassLoader cl = PMD.
     * .createClasspathClassLoader(auxPathFile.toURI().toURL() .toString());
     */
    config.prependClasspath(auxPathFile.toURI().toURL().toString());

    final List<DataSource> files = new ArrayList<>();
    prepJavaFiles(new TargetPrep() {
      @Override
      public void prep(File f) {
        files.add(new FileDataSource(f));
      }
    });
    final List<Renderer> renderers = new ArrayList<>(); // output
    renderers.add(new Output(getGenerator(), monitor));

    config.setRuleSets("internal-all-java");
    RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(config);

    monitor.begin(files.size() + 25);
    /*
     * static void processFiles(final PMDConfiguration configuration, final
     * RuleSetFactory ruleSetFactory, final List<DataSource> files, final
     * RuleContext ctx, final List<Renderer> renderers) {
     */
    PMD.processFiles(config, ruleSetFactory, files, ctx, renderers);
    auxPathFile.delete();
  }

  class Output extends AbstractRenderer {
    private final ArtifactGenerator generator;
    private final SLProgressMonitor monitor;
    private boolean first = true;

    public Output(ArtifactGenerator gen, SLProgressMonitor m) {
      // Needs to be the tool name!
      super("PMD", "Glue code to translate PMD results to Sierra artifacts");
      generator = gen;
      monitor = m;
    }

    @Override
    public Writer getWriter() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setWriter(Writer writer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setShowSuppressedViolations(boolean show) {
      // Do nothing?
    }

    @Override
    public void start() throws IOException {
      // Do nothing
    }

    @Override
    public synchronized void startFileAnalysis(DataSource dataSource) {
      String msg = "Scanning " + dataSource.getNiceFileName(false, "");
      monitor.subTask(msg);
      if (first) {
        first = false;
      } else {
        monitor.worked(1);
      }
      if (SLLogger.getLogger().isLoggable(Level.FINE)) {
        SLLogger.getLogger().fine(msg);
      }
    }

    @Override
    public synchronized void renderFileReport(Report report) throws IOException {
      Iterator<RuleViolation> it = report.iterator();
      while (it.hasNext()) {
        RuleViolation v = it.next();
        if (SLLogger.getLogger().isLoggable(Level.FINE)) {
          System.out.println(v.getFilename() + ": " + v.getDescription());
        }
        ArtifactBuilder artifact = generator.artifact();
        SourceLocationBuilder sourceLocation = artifact.primarySourceLocation();

        String file = v.getFilename();
        sourceLocation.packageName(v.getPackageName());
        String cuName = getCompUnitName(file);
        sourceLocation.compilation(cuName);

        if (v.getClassName() == null || "".equals(v.getClassName())) {
          // No class name, so use the main class for the compilation
          // unit
          sourceLocation.className(cuName);
        } else {
          sourceLocation.className(v.getClassName());
        }

        String method = v.getMethodName();
        String field = v.getVariableName();
        if ("".equals(method)) {
          sourceLocation.type(IdentifierType.CLASS);
          sourceLocation.identifier(v.getClassName());
        } else if ("".equals(field)) {
          sourceLocation.type(IdentifierType.METHOD);
          sourceLocation.identifier(method);
        } else {
          sourceLocation.type(IdentifierType.FIELD);
          sourceLocation.identifier(field);
        }

        HashGenerator hashGenerator = HashGenerator.getInstance();
        Long hashValue = hashGenerator.getHash(v.getFilename(), v.getBeginLine());
        // FIX use v.getBeginColumn();
        // FIX use v.getEndColumn();
        sourceLocation = sourceLocation.hash(hashValue).lineOfCode(v.getBeginLine());
        sourceLocation = sourceLocation.endLine(v.getEndLine());

        artifact.findingType(getName(), getVersion(), v.getRule().getName());
        artifact.message(v.getDescription());

        int priority = v.getRule().getPriority().getPriority();
        Priority assignedPriority = getPMDPriority(priority);
        Severity assignedSeverity = getPMDSeverity(priority);
        artifact.priority(assignedPriority).severity(assignedSeverity);

        sourceLocation.build();
        artifact.build();
      }

      Iterator<ProcessingError> errors = report.errors();
      while (errors.hasNext()) {
        ProcessingError error = errors.next();
        SLLogger.getLogger().warning(error.getFile() + ": " + error.getMsg());
        ErrorBuilder eb = generator.error();
        eb.message(error.getMsg());
        eb.tool(getName() + " v." + getVersion());
        eb.build();
      }
      // System.out.println("Done with report");
    }

    @Override
    public void end() throws IOException {
      // Do nothing
    }

    @Override
    public String defaultFileExtension() {
      // TODO Auto-generated method stub
      return null;
    }
  }

  static Severity getPMDSeverity(int priority) {
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

  static Priority getPMDPriority(int priority) {
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
